package com.springboot.controller;

import com.springboot.model.*;
import com.springboot.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/sitter-job")
public class SitterJobController {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private ApplyJobRepository applyJobRepository;
    @Autowired private AssignJobRepository assignJobRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private PetSitterRepository petSitterRepository;
    @Autowired private CarePetRepository carePetRepository;
    @Autowired private PetCareMediaRepository petCareMediaRepository;
    @Autowired private NotificationController notificationController;

    @Value("${file.upload-dir.reports}")
    private String uploadDirReports;

    @Value("${file.upload-dir.slips}")
    private String uploadDirSlips;

    // =================== ค้นหาประกาศใกล้เคียง ===================
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchAnnouncements(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) String petType,
            @RequestParam(defaultValue = "5.0") Double radius) {

        List<Announcement> all = announcementRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Announcement ann : all) {
            // แสดงเฉพาะสถานะที่รับสมัครหรือรอพิจารณา
            if (!"รับสมัคร".equals(ann.getStatus()) && !"รอพิจารณา".equals(ann.getStatus())) continue;
            if (ann.getLatitude() == null || ann.getLongitude() == null) continue;

            double distance = calculateDistance(lat, lng, ann.getLatitude(), ann.getLongitude());
            if (distance > radius) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("announceID", ann.getAnnounceID());
            map.put("postdate", ann.getPostdate());
            map.put("startdate", ann.getStartdate());
            map.put("enddate", ann.getEnddate());
            map.put("status", ann.getStatus());
            map.put("subdistrict", ann.getSubdistrict());
            map.put("district", ann.getDistrict());
            map.put("province", ann.getProvince());
            map.put("currentweight", ann.getCurrentweight());
            map.put("distance", Math.round(distance * 10.0) / 10.0);

            // ข้อมูลสัตว์เลี้ยง — Phase 6: ใช้ relationship แทน petRepository.findById()
            Pet pet = ann.getPet();
            if (pet != null) {
                map.put("petName", pet.getPetName());
                map.put("petImage", pet.getPetImage());
                map.put("petType", pet.getPetType() != null ? pet.getPetType().getPetTypeName() : "");
                map.put("breed", pet.getBreed());
                map.put("gender", pet.getGender());
                map.put("birthDate", pet.getBirthDate());
            }

            // กรองประเภทสัตว์
            if (petType != null && !petType.equals("ทั้งหมด") && !petType.isEmpty()) {
                String pType = (String) map.get("petType");
                if (pType == null || !pType.contains(petType)) continue;
            }

            // ดึงช่วงเวลา — Phase 6: ใช้ relationship แทน carePetRepository.findById()
            CarePet carePet = ann.getCarePet();
            if (carePet != null) {
                List<String> slots = new ArrayList<>();
                if (Boolean.TRUE.equals(carePet.getCareMorning())) slots.add("ช่วงเช้า");
                if (Boolean.TRUE.equals(carePet.getCareAfternoon())) slots.add("ช่วงกลางวัน");
                if (Boolean.TRUE.equals(carePet.getCareEvening())) slots.add("ช่วงเย็น");
                if (Boolean.TRUE.equals(carePet.getCareNight())) slots.add("ช่วงดึก");
                map.put("careSlots", slots);
            }

            result.add(map);
        }

        // เรียงตามวันที่เริ่มดูแล (startdate) จากใกล้ถึงที่สุดไปไกลที่สุด — ประกาศที่ต้องไปดูแลเร็วสุดขึ้นก่อน
        result.sort(Comparator.comparing(m -> (LocalDate) m.get("startdate"), Comparator.nullsLast(Comparator.naturalOrder())));
        return ResponseEntity.ok(result);
    }

    // =================== ดึงรายละเอียดประกาศ (ฝั่งผู้ดูแล) ===================
    @GetMapping("/detail/{announceID}/{sitterID}")
    public ResponseEntity<Map<String, Object>> getAnnouncementDetail(
            @PathVariable Integer announceID,
            @PathVariable Integer sitterID) {

        Optional<Announcement> optional = announcementRepository.findById(announceID);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Announcement ann = optional.get();
        Map<String, Object> response = new HashMap<>();

        response.put("announceID", ann.getAnnounceID());
        response.put("postdate", ann.getPostdate());
        response.put("startdate", ann.getStartdate());
        response.put("enddate", ann.getEnddate());
        response.put("currentweight", ann.getCurrentweight());
        response.put("addressNo", ann.getAddressNo());
        response.put("street", ann.getStreet());
        response.put("subdistrict", ann.getSubdistrict());
        response.put("district", ann.getDistrict());
        response.put("province", ann.getProvince());
        response.put("zipcode", ann.getZipcode());
        response.put("latitude", ann.getLatitude());
        response.put("longitude", ann.getLongitude());
        response.put("status", ann.getStatus());
        response.put("totalAmount", ann.getTotalAmount());

        // ข้อมูลเจ้าของ — Phase 6: ใช้ relationship แทน petOwnerRepository.findById()
        PetOwner owner = ann.getOwner();
        if (owner != null) {
            Map<String, Object> ownerMap = new HashMap<>();
            ownerMap.put("ownerID", owner.getOwnetID());
            ownerMap.put("firstname", owner.getFirstname());
            ownerMap.put("lastname", owner.getLastname());
            ownerMap.put("gender", owner.getGender());
            ownerMap.put("birthdate", owner.getBirtdate());
            ownerMap.put("phoneNumber", owner.getPhoneNumber());
            ownerMap.put("email", owner.getEmail());
            ownerMap.put("profileImage", owner.getProfileImage());
            response.put("owner", ownerMap);
        }

        // ข้อมูลสัตว์เลี้ยง — Phase 6: ใช้ relationship แทน petRepository.findById()
        Pet pet = ann.getPet();
        if (pet != null) {
            Map<String, Object> petMap = new HashMap<>();
            petMap.put("petName", pet.getPetName());
            petMap.put("petImage", pet.getPetImage());
            petMap.put("petType", pet.getPetType() != null ? pet.getPetType().getPetTypeName() : "");
            petMap.put("breed", pet.getBreed());
            petMap.put("gender", pet.getGender());
            petMap.put("birthDate", pet.getBirthDate());
            petMap.put("vacRabiesDate", pet.getVacRabiesDate());
            petMap.put("vacDhppiDate", pet.getVacDhppiDate());
            petMap.put("vacFvrcpDate", pet.getVacFvrcpDate());
            petMap.put("hasCongenitalDisease", pet.getHasCongenitalDisease());
            petMap.put("congenitalDiseaseDetail", pet.getCongenitalDiseaseDetail());
            petMap.put("behaviorFriendly", pet.getBehaviorFriendly());
            petMap.put("behaviorFearStranger", pet.getBehaviorFearStranger());
            petMap.put("behaviorFearLoudSound", pet.getBehaviorFearLoudSound());
            petMap.put("behaviorBarkLoud", pet.getBehaviorBarkLoud());
            petMap.put("behaviorDislikeTouch", pet.getBehaviorDislikeTouch());
            petMap.put("behaviorBiteScrath", pet.getBehaviorBiteScrath());
            petMap.put("behaviorEscapeExpert", pet.getBehaviorEscapeExpert());
            petMap.put("behaviorHighEnergy", pet.getBehaviorHighEnergy());
            petMap.put("behaviorStressAlone", pet.getBehaviorStressAlone());
            petMap.put("behaviorJumpOnPeople", pet.getBehaviorJumpOnPeople());
            petMap.put("behaviorHardControl", pet.getBehaviorHardControl());
            response.put("pet", petMap);
        }

        // ข้อมูล CarePet — Phase 6: ใช้ relationship แทน carePetRepository.findById()
        CarePet carePet = ann.getCarePet();
        if (carePet != null) {
            List<String> slots = new ArrayList<>();
            if (Boolean.TRUE.equals(carePet.getCareMorning())) slots.add("ช่วงเช้า");
            if (Boolean.TRUE.equals(carePet.getCareAfternoon())) slots.add("ช่วงกลางวัน");
            if (Boolean.TRUE.equals(carePet.getCareEvening())) slots.add("ช่วงเย็น");
            if (Boolean.TRUE.equals(carePet.getCareNight())) slots.add("ช่วงดึก");
            response.put("careSlots", slots);
            response.put("carePet", carePet);
        }

        // รายชื่อผู้สมัคร (เฉพาะคำขอล่าสุดของแต่ละคน)
        List<ApplyJob> applicants = applyJobRepository.findByAnnounceID(announceID);
        Map<Integer, ApplyJob> latestBySitter = new LinkedHashMap<>();
        for (ApplyJob apply : applicants) {
            latestBySitter.put(apply.getSitterID(), apply);
        }
        List<Map<String, Object>> applicantList = new ArrayList<>();
        for (ApplyJob apply : latestBySitter.values()) {
            Map<String, Object> applyMap = new HashMap<>();
            applyMap.put("applyJobID", apply.getApplyJobID());
            applyMap.put("appStatus", apply.getAppStatus());
            applyMap.put("sitterID", apply.getSitterID());
            // Phase 6: ใช้ relationship แทน petSitterRepository.findById()
            PetSitter applicantSitter = apply.getSitter();
            if (applicantSitter != null) {
                applyMap.put("sitterName", applicantSitter.getFirstname() + " " + applicantSitter.getLastname());
                applyMap.put("sitterImage", applicantSitter.getSitterImage());
                applyMap.put("pricePerDay", applicantSitter.getPricePerDay());
                applyMap.put("subdistrict", applicantSitter.getSubdistrict());
                applyMap.put("province", applicantSitter.getProvince());
                applyMap.put("petAlowPet", applicantSitter.getPetAllowType());
                if (ann.getLatitude() != null && applicantSitter.getLatitude() != null) {
                    double dist = calculateDistance(ann.getLatitude(), ann.getLongitude(), applicantSitter.getLatitude(), applicantSitter.getLongitude());
                    applyMap.put("distance", Math.round(dist * 10.0) / 10.0);
                }
            }
            applicantList.add(applyMap);
        }
        response.put("applicants", applicantList);
        response.put("applicantCount", applicantList.size());

        // สถานะของผู้ดูแลคนนี้
        String myStatus = latestBySitter.containsKey(sitterID)
            ? latestBySitter.get(sitterID).getAppStatus()
            : null;
        response.put("myStatus", myStatus);

        // ถ้าได้รับเลือก ดึง AssignJob + รายงาน — Phase 6: ใช้ relationship แทน assignJobRepository.findById()
        if ("ได้รับเลือก".equals(myStatus) && ann.getAssignJob() != null) {
            AssignJob assignJob = ann.getAssignJob();
            response.put("detailID", assignJob.getDetailID());
            response.put("workStatus", assignJob.getWorkStatus());
            response.put("slipImage", assignJob.getSlipImage());
            response.put("isPaymentConfirmed", assignJob.getIsPaymentConfirmed());

            // รายงานที่ส่งแล้ว
            List<PetCareMedia> reports = petCareMediaRepository.findByDetailID(assignJob.getDetailID());
            List<Map<String, Object>> reportList = new ArrayList<>();
            for (PetCareMedia media : reports) {
                Map<String, Object> rm = new HashMap<>();
                rm.put("petCaremeID", media.getPetCaremeID());
                rm.put("timecare", media.getTimecare());
                rm.put("careDate", media.getCareDate());
                rm.put("image", media.getImage());
                rm.put("video", media.getVideo());
                rm.put("createdAt", media.getCreatedAt());
                reportList.add(rm);
            }
            response.put("reports", reportList);
            // ช่วงเวลาที่ส่งไปแล้ว "ของวันนี้" เท่านั้น — ใช้เช็คใน dropdown ว่าช่วงไหนส่งไปแล้ว
            LocalDate todayForReport = LocalDate.now();
            response.put("reportedSlotsToday", reports.stream()
                .filter(m -> todayForReport.equals(m.getCareDate()))
                .map(PetCareMedia::getTimecare).toList());
        }

        return ResponseEntity.ok(response);
    }

    // =================== ส่งคำขอรับงาน ===================
    @Transactional
    @PostMapping("/apply")
    public ResponseEntity<Map<String, String>> applyJob(@RequestBody Map<String, Object> data) {
        Map<String, String> response = new HashMap<>();
        try {
            Integer sitterID = Integer.parseInt(data.get("sitterID").toString());
            Integer announceID = Integer.parseInt(data.get("announceID").toString());

            // เช็คว่าสมัครแล้วหรือยัง
            List<ApplyJob> existing = applyJobRepository.findByAnnounceID(announceID);
            boolean alreadyApplied = existing.stream()
            	    .anyMatch(a -> a.getSitterID().equals(sitterID) 
            	        && !"ถอนคำขอแล้ว".equals(a.getAppStatus()));
            if (alreadyApplied) {
                response.put("message", "คุณได้ส่งคำขอรับงานนี้แล้ว");
                return ResponseEntity.badRequest().body(response);
            }

            // เช็คงานซ้อนทับ
            Optional<Announcement> targetOpt = announcementRepository.findById(announceID);
            if (targetOpt.isEmpty()) {
                response.put("message", "ไม่พบประกาศ");
                return ResponseEntity.badRequest().body(response);
            }
            Announcement target = targetOpt.get();

            // ดึงช่วงเวลาของประกาศนี้
            List<String> targetSlots = getSlots(target.getCarepetID());

            // ดึงงานทั้งหมดที่ผู้ดูแลส่งคำขอหรือได้รับเลือก
            List<ApplyJob> myJobs = applyJobRepository.findBySitterID(sitterID);
            for (ApplyJob myJob : myJobs) {
                if ("ถอนคำขอแล้ว".equals(myJob.getAppStatus()) || "ไม่ได้รับเลือก".equals(myJob.getAppStatus())) continue;
                Optional<Announcement> otherAnn = announcementRepository.findById(myJob.getAnnounceID());
                if (otherAnn.isEmpty()) continue;
                Announcement other = otherAnn.get();

                // เช็ควันซ้อนทับ
                if (datesOverlap(target.getStartdate(), target.getEnddate(), other.getStartdate(), other.getEnddate())) {
                    List<String> otherSlots = getSlots(other.getCarepetID());
                    // เช็คช่วงเวลาซ้อน
                    for (String slot : targetSlots) {
                        if (otherSlots.contains(slot)) {
                            response.put("message", "ไม่สามารถส่งคำขอรับงานได้ เนื่องจากมีงานในวันและช่วงเวลานี้แล้ว");
                            return ResponseEntity.badRequest().body(response);
                        }
                    }
                }
            }

            // สร้าง ApplyJob
            ApplyJob applyJob = new ApplyJob();
            applyJob.setAnnounceID(announceID);
            applyJob.setSitterID(sitterID);
            applyJob.setAppStatus("รอพิจารณา");
            applyJob.setDetailID(target.getAssignJobID());
            applyJobRepository.save(applyJob);

            // อัพเดทสถานะประกาศ
            target.setStatus("รอพิจารณา");
            announcementRepository.save(target);
            
         // แจ้งเตือนเจ้าของว่ามีคนสมัครงาน
            notificationController.saveNotif(
                target.getOwnerID(), "OWNER",
                "📋 มีผู้ดูแลส่งคำขอรับงานประกาศของคุณใหม่",
                announceID
            );

            response.put("message", "ส่งคำขอรับงานเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =================== ยกเลิกคำขอรับงาน ===================
    @PutMapping("/cancel-apply/{applyJobID}")
    public ResponseEntity<Map<String, String>> cancelApply(@PathVariable Integer applyJobID) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<ApplyJob> optional = applyJobRepository.findById(applyJobID);
            if (optional.isEmpty()) {
                response.put("message", "ไม่พบข้อมูลคำขอ");
                return ResponseEntity.badRequest().body(response);
            }
            ApplyJob applyJob = optional.get();
            applyJob.setAppStatus("ถอนคำขอแล้ว");
            applyJobRepository.save(applyJob);

            // เช็คว่ายังมีคนสมัครอยู่ไหม
            List<ApplyJob> remaining = applyJobRepository.findByAnnounceID(applyJob.getAnnounceID());
            boolean hasActive = remaining.stream().anyMatch(a ->
                !"ถอนคำขอแล้ว".equals(a.getAppStatus()) && !"ไม่ได้รับเลือก".equals(a.getAppStatus()));

            announcementRepository.findById(applyJob.getAnnounceID()).ifPresent(ann -> {
                ann.setStatus(hasActive ? "รอพิจารณา" : "รับสมัคร");
                announcementRepository.save(ann);
            });

            response.put("message", "ยกเลิกคำขอเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =================== ส่งรายงานการดูแล ===================
    @PostMapping("/submit-report")
    public ResponseEntity<Map<String, String>> submitReport(
            @RequestParam Integer detailID,
            @RequestParam String timecare,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "video", required = false) MultipartFile video) {

        Map<String, String> response = new HashMap<>();
        try {
        	// =================== เช็ควันที่และเวลา ===================
        	// ดึง announceID จาก detailID
        	Announcement targetAnn = null;
        	List<Announcement> allAnns = announcementRepository.findAll();
        	for (Announcement a : allAnns) {
        	    if (a.getAssignJobID() != null && a.getAssignJobID().equals(detailID)) {
        	        targetAnn = a;
        	        break;
        	    }
        	}

        	if (targetAnn != null) {
        	    LocalDate today = LocalDate.now();
        	    LocalDate startDate = targetAnn.getStartdate();
        	    LocalDate endDate = targetAnn.getEnddate();

        	    // เช็คว่าวันนี้อยู่ในช่วงวันที่ประกาศไหม
        	    if (today.isBefore(startDate) || today.isAfter(endDate)) {
        	        response.put("message", "ไม่สามารถส่งรายงานได้ เนื่องจากไม่อยู่ในช่วงวันที่ดูแล (" 
        	            + startDate + " ถึง " + endDate + ")");
        	        return ResponseEntity.badRequest().body(response);
        	    }
        	}
        	
            // เช็คว่าส่งซ้ำไหม (แยกตามวัน — วันนี้เท่านั้น)
            LocalDate today = LocalDate.now();
            if (petCareMediaRepository.existsByDetailIDAndTimecareAndCareDate(detailID, timecare, today)) {
                response.put("message", "คุณส่งรายงานช่วงเวลานี้ของวันนี้ไปแล้ว");
                return ResponseEntity.badRequest().body(response);
            }

            if ((images == null || images.length == 0) && video == null) {
                response.put("message", "กรุณาอัปโหลดรูปภาพและวิดีโอการดูแล");
                return ResponseEntity.badRequest().body(response);
            }

            PetCareMedia media = new PetCareMedia();
            media.setDetailID(detailID);
            media.setTimecare(timecare);
            media.setCareDate(today);

            // บันทึกรูปหลายรูป
            if (images != null && images.length > 0) {
                List<String> imageNames = new ArrayList<>();
                for (MultipartFile img : images) {
                    String name = saveFile(img, uploadDirReports);
                    if (name != null) imageNames.add(name);
                }
                ObjectMapper mapper = new ObjectMapper();
                media.setImage(mapper.writeValueAsString(imageNames));
            }

            // บันทึกวิดีโอ 1 ไฟล์
            if (video != null && !video.isEmpty()) {
                String videoName = saveFile(video, uploadDirReports);
                media.setVideo(videoName);
            }

            petCareMediaRepository.save(media);
            
            if (targetAnn != null) {
                notificationController.saveNotif(
                    targetAnn.getOwnerID(), "OWNER",
                    "📸 ผู้ดูแลส่งรายงานการดูแล " + timecare + " แล้ว กรุณาตรวจสอบ",
                    targetAnn.getAnnounceID()
                );
            }
            response.put("message", "ส่งรายงานเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =================== ยืนยันการชำระเงิน ===================
    @PutMapping("/confirm-payment/{detailID}")
    public ResponseEntity<Map<String, String>> confirmPayment(@PathVariable Integer detailID) {
        Map<String, String> response = new HashMap<>();
        assignJobRepository.findById(detailID).ifPresent(aj -> {
            aj.setIsPaymentConfirmed("ชำระเงินเรียบร้อยแล้ว");
            assignJobRepository.save(aj);
            
            announcementRepository.findAll().stream()
            .filter(a -> a.getAssignJobID() != null && a.getAssignJobID().equals(detailID))
            .findFirst()
            .ifPresent(a -> {
                a.setStatus("งานเสร็จสิ้น");
                announcementRepository.save(a);
                notificationController.saveNotif(
                    a.getOwnerID(), "OWNER",
                    "⭐ ผู้ดูแลยืนยันรับเงินแล้ว กรุณารีวิวผู้ดูแลสัตว์เลี้ยง",
                    a.getAnnounceID()
                );
            });
        });
        response.put("message", "ได้รับเงินเรียบร้อยแล้ว");
        return ResponseEntity.ok(response);
    }

    // =================== ดึงรายการคำขอของผู้ดูแล ===================
    @GetMapping("/my-applications/{sitterID}")
    public ResponseEntity<List<Map<String, Object>>> getMyApplications(@PathVariable Integer sitterID) {
    	List<ApplyJob> myJobs = applyJobRepository.findBySitterID(sitterID);
    	List<Map<String, Object>> result = new ArrayList<>();

    	// เก็บเฉพาะคำขอล่าสุดของแต่ละประกาศ
    	Map<Integer, ApplyJob> latestByAnnounce = new LinkedHashMap<>();
    	for (ApplyJob apply : myJobs) {
    	    latestByAnnounce.put(apply.getAnnounceID(), apply);
    	}

    	for (ApplyJob apply : latestByAnnounce.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("applyJobID", apply.getApplyJobID());
            map.put("appStatus", apply.getAppStatus());
            map.put("announceID", apply.getAnnounceID());

            // Phase 6: ใช้ relationship แทน announcementRepository.findById()
            Announcement ann = apply.getAnnouncement();
            if (ann != null) {
                map.put("postdate", ann.getPostdate());
                map.put("startdate", ann.getStartdate());
                map.put("enddate", ann.getEnddate());
                map.put("status", ann.getStatus());
                map.put("subdistrict", ann.getSubdistrict());
                map.put("district", ann.getDistrict());
                map.put("province", ann.getProvince());
                map.put("currentweight", ann.getCurrentweight());

                // Phase 6: ใช้ relationship แทน petRepository.findById()
                Pet pet = ann.getPet();
                if (pet != null) {
                    map.put("petName", pet.getPetName());
                    map.put("petImage", pet.getPetImage());
                    map.put("petType", pet.getPetType() != null ? pet.getPetType().getPetTypeName() : "");
                    map.put("breed", pet.getBreed());
                    map.put("gender", pet.getGender());
                    map.put("birthDate", pet.getBirthDate());
                }
            }
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // =================== Helper ===================
    private List<String> getSlots(Integer carepetID) {
        List<String> slots = new ArrayList<>();
        if (carepetID == null) return slots;
        carePetRepository.findById(carepetID).ifPresent(cp -> {
            if (Boolean.TRUE.equals(cp.getCareMorning())) slots.add("ช่วงเช้า");
            if (Boolean.TRUE.equals(cp.getCareAfternoon())) slots.add("ช่วงกลางวัน");
            if (Boolean.TRUE.equals(cp.getCareEvening())) slots.add("ช่วงเย็น");
            if (Boolean.TRUE.equals(cp.getCareNight())) slots.add("ช่วงดึก");
        });
        return slots;
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) return false;
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private String saveFile(MultipartFile file, String dirPath) {
        try {
            Path uploadPath = Paths.get(dirPath);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            long count = Files.list(uploadPath).filter(p -> !Files.isDirectory(p)).count();
            long nextNum = count + 1;
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : ".png";
            String fileName = String.format("%03d%s", nextNum, ext);
            while (Files.exists(uploadPath.resolve(fileName))) {
                nextNum++; fileName = String.format("%03d%s", nextNum, ext);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            return fileName;
        } catch (IOException e) { return null; }
    }
}