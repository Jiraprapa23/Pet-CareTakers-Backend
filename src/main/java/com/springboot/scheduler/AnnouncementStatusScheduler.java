package com.springboot.scheduler;

import com.springboot.model.Announcement;
import com.springboot.model.ApplyJob;
import com.springboot.model.CarePet;
import com.springboot.repository.AnnouncementRepository;
import com.springboot.repository.ApplyJobRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * เช็คทุก ๆ 15 นาที — ประกาศที่สถานะ "ได้รับผู้ดูแลแล้ว" และถึงวันเริ่มดูแล (startdate) แล้ว
 * จะเปลี่ยนสถานะเป็น "กำลังดูแล" ให้อัตโนมัติ ไม่ต้องมีใครกด
 *
 * และ — ประกาศที่สถานะยัง "รับสมัคร"/"รอพิจารณา" แต่ไม่มีผู้สมัครที่ยัง active อยู่เลย
 * (ไม่มีคนสมัครเลย หรือสมัครแล้วถอน/ถูกปฏิเสธหมด) และถึงเวลา "เริ่มต้น" ของช่วงเวลาดูแล
 * แรกสุดที่เจ้าของเลือกไว้ (เช้า/กลางวัน/เย็น/ดึก) ในวันเริ่มดูแลแล้ว
 * จะเปลี่ยนสถานะเป็น "หมดอายุ" ให้อัตโนมัติ
 *
 * ตัวอย่าง: เลือกแค่ "ช่วงกลางวัน" (เริ่ม 11:00) ในวันที่ 18/09/2026 → หมดอายุตอน
 * 11:00 ของวันที่ 18/09/2026 ถ้าตอนนั้นยังไม่มีผู้สมัคร active เลย
 * ถ้าเลือกหลายช่วง (เช่น เช้า+เย็น) ใช้เวลาเริ่มของช่วง "แรกสุด" เป็นตัวตัดสิน (เช้า = 06:00)
 */
@Component
public class AnnouncementStatusScheduler {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private ApplyJobRepository applyJobRepository;

    // รันทุก 15 นาที — ต้องถี่ขึ้นจากเดิม (วันละครั้ง) เพราะตอนนี้เช็คถึงระดับ "เวลา" ไม่ใช่แค่ "วัน"
    // ถ้ายังรันแค่วันละครั้งตอนเที่ยงคืน สถานะจะไม่เปลี่ยนจนกว่าจะถึงเที่ยงคืนถัดไปอยู่ดี
    // (เช่น หมดอายุจริงตอน 15:00 แต่ระบบจะไปเปลี่ยนสถานะให้ตอนเที่ยงคืนแทน)
    @Scheduled(cron = "0 */15 * * * *")
    public void updateToInProgress() {
        runUpdate();
    }

    // รันครั้งเดียวตอน backend เพิ่งสตาร์ท — เผื่อกรณีปิดเซิร์ฟเวอร์ไว้ช่วงหนึ่ง จะได้ตามให้ทัน
    @PostConstruct
    public void runOnStartup() {
        runUpdate();
    }

    private void runUpdate() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        List<Announcement> announcements = announcementRepository.findAll();
        for (Announcement ann : announcements) {
            boolean isAssigned = "ได้รับผู้ดูแลแล้ว".equals(ann.getStatus());
            boolean hasStartedByDate = ann.getStartdate() != null && !ann.getStartdate().isAfter(today);
            if (isAssigned && hasStartedByDate) {
                ann.setStatus("กำลังดูแล");
                announcementRepository.save(ann);
                continue;
            }

            // ประกาศที่ยังเปิดรับสมัคร/รอพิจารณา แต่เลยเวลาสิ้นสุดของช่วงดูแลล่าสุดในวันเริ่มดูแลแล้ว
            // โดยไม่มีผู้สมัคร active เลย → หมดอายุ
            boolean isOpenForApply = "รับสมัคร".equals(ann.getStatus()) || "รอพิจารณา".equals(ann.getStatus());
            if (isOpenForApply && ann.getStartdate() != null) {
                LocalDateTime expiryThreshold = getExpiryThreshold(ann);
                if (now.isAfter(expiryThreshold)) {
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

    /**
     * หาเวลาที่ถือว่าประกาศ "หมดอายุได้" — คือเวลา "เริ่มต้น" ของช่วงเวลาดูแลแรกสุด
     * (เช้า/กลางวัน/เย็น/ดึก) ที่เจ้าของเลือกไว้ ในวันที่เริ่มดูแล (startdate)
     * ใช้เวลาเริ่มต้น ไม่ใช่เวลาสิ้นสุด เพราะถ้าถึงเวลาที่ต้องการให้เริ่มดูแลแล้วยังไม่มีใครรับงาน
     * ก็ไม่มีทางดูแลได้ทันตามที่ต้องการอยู่ดี — ถ้าเลือกหลายช่วง ใช้ช่วงที่ "เริ่มเร็วที่สุด"
     * เพราะงานต้องเริ่มตั้งแต่ช่วงแรกที่เลือกไว้ ถ้าไม่มีคนรับตั้งแต่ตอนนั้นก็ถือว่าไม่สำเร็จตามที่ตั้งใจ
     * ถ้าไม่มีข้อมูล CarePet หรือไม่ได้เลือกช่วงไหนเลย ใช้ค่า fallback เป็นเริ่มต้นวัน (เที่ยงคืน)
     */
    private LocalDateTime getExpiryThreshold(Announcement ann) {
        LocalDate startdate = ann.getStartdate();
        CarePet care = ann.getCarePet();

        if (care == null) {
            return startdate.atStartOfDay();
        }

        LocalTime earliestStart = null;
        if (Boolean.TRUE.equals(care.getCareMorning())) {
            earliestStart = LocalTime.of(6, 0);
        } else if (Boolean.TRUE.equals(care.getCareAfternoon())) {
            earliestStart = LocalTime.of(11, 0);
        } else if (Boolean.TRUE.equals(care.getCareEvening())) {
            earliestStart = LocalTime.of(16, 0);
        } else if (Boolean.TRUE.equals(care.getCareNight())) {
            earliestStart = LocalTime.of(21, 0);
        }

        if (earliestStart == null) {
            return startdate.atStartOfDay();
        }

        return LocalDateTime.of(startdate, earliestStart);
    }
}