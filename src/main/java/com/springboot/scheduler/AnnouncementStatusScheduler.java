package com.springboot.scheduler;

import com.springboot.model.Announcement;
import com.springboot.model.ApplyJob;
import com.springboot.repository.AnnouncementRepository;
import com.springboot.repository.ApplyJobRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * เช็คทุกวัน — ประกาศที่สถานะ "ได้รับผู้ดูแลแล้ว" และถึงวันเริ่มดูแล (startdate) แล้ว
 * จะเปลี่ยนสถานะเป็น "กำลังดูแล" ให้อัตโนมัติ ไม่ต้องมีใครกด
 *
 * และ — ประกาศที่สถานะยัง "รับสมัคร"/"รอพิจารณา" แต่ไม่มีผู้สมัครที่ยัง active อยู่เลย
 * (ไม่มีคนสมัครเลย หรือสมัครแล้วถอน/ถูกปฏิเสธหมด) จนถึงวันเริ่มดูแลแล้ว
 * จะเปลี่ยนสถานะเป็น "หมดอายุ" ให้อัตโนมัติ
 */
@Component
public class AnnouncementStatusScheduler {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private ApplyJobRepository applyJobRepository;

    // รันทุกวันตอนตี 00:05
    @Scheduled(cron = "0 5 0 * * *")
    public void updateToInProgress() {
        runUpdate();
    }

    // รันครั้งเดียวตอน backend เพิ่งสตาร์ท — เผื่อกรณีปิดเซิร์ฟเวอร์ไว้ข้ามวัน จะได้ตามให้ทัน
    @PostConstruct
    public void runOnStartup() {
        runUpdate();
    }

    private void runUpdate() {
        LocalDate today = LocalDate.now();
        List<Announcement> announcements = announcementRepository.findAll();
        for (Announcement ann : announcements) {
            boolean isAssigned = "ได้รับผู้ดูแลแล้ว".equals(ann.getStatus());
            boolean hasStarted = ann.getStartdate() != null && !ann.getStartdate().isAfter(today);
            if (isAssigned && hasStarted) {
                ann.setStatus("กำลังดูแล");
                announcementRepository.save(ann);
                continue;
            }

            // ประกาศที่ยังเปิดรับสมัคร/รอพิจารณา แต่ถึงวันเริ่มดูแลแล้วโดยไม่มีผู้สมัคร active เลย → หมดอายุ
            boolean isOpenForApply = "รับสมัคร".equals(ann.getStatus()) || "รอพิจารณา".equals(ann.getStatus());
            if (isOpenForApply && hasStarted) {
                List<ApplyJob> applicants = applyJobRepository.findByAnnounceID(ann.getAnnounceID());
                boolean hasActiveApplicant = applicants.stream().anyMatch(a ->
                        "รอพิจารณา".equals(a.getAppStatus()) || "ได้รับเลือก".equals(a.getAppStatus()));
                if (!hasActiveApplicant) {
                    ann.setStatus("หมดอายุ");
                    announcementRepository.save(ann);
                }
            }
        }
    }
}