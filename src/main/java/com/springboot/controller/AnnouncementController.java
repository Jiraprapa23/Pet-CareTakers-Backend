package com.springboot.controller;


import com.springboot.model.*;
import com.springboot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/announcement")

public class AnnouncementController {

    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private CarePetRepository carePetRepository;
    @Autowired private AssignJobRepository assignJobRepository;
    @Autowired private ApplyJobRepository applyJobRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private PetSitterRepository petSitterRepository;
    @Autowired private PetCareMediaRepository petCareMediaRepository;
    @Autowired private NotificationController notificationController;

    @Value("${file.upload-dir.slips}")
    private String uploadDirSlips;

    // =================== สร้างประกาศ ===================
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            // =================== ตรวจสอบว่าวัน+เวลาเริ่มดูแลยังไม่ผ่านไปแล้ว ===================
            // ป้องกันกรณีสร้างประกาศที่ "เกิดมาก็หมดอายุทันที" เช่น โพสต์ 09:00 แต่ต้องการ
            // คนมาดูแลวันนี้ตั้งแต่ช่วงเช้า (06:00) ซึ่งผ่านไปแล้ว — เช็คแค่เวลาเริ่มของ
            // ช่วงดูแล "แรกสุด" ที่เลือกไว้เทียบกับ startdate เท่านั้น (ตรงกับ logic หมดอายุ
            // ใน AnnouncementStatusScheduler)
            LocalDate reqStartdate = LocalDate.parse(getString(data, "startdate"));
            LocalDate reqEnddate = LocalDate.parse(getString(data, "enddate"));
            boolean reqCareMorning = getBool(data, "careMorning");
            boolean reqCareAfternoon = getBool(data, "careAfternoon");
            boolean reqCareEvening = getBool(data, "careEvening");
            boolean reqCareNight = getBool(data, "careNight");

            LocalTime earliestStart = null;
            if (reqCareMorning) earliestStart = LocalTime.of(6, 0);
            else if (reqCareAfternoon) earliestStart = LocalTime.of(11, 0);
            else if (reqCareEvening) earliestStart = LocalTime.of(16, 0);
            else if (reqCareNight) earliestStart = LocalTime.of(21, 0);

            if (earliestStart != null) {
                LocalDateTime earliestAllowed = LocalDateTime.of(reqStartdate, earliestStart);
                if (!earliestAllowed.isAfter(LocalDateTime.now())) {
                    response.put("message", "ไม่สามารถสร้างประกาศได้ เนื่องจากเวลาเริ่มต้นของช่วงดูแลที่เลือกไว้ผ่านไปแล้ว กรุณาเลือกวันที่หรือช่วงเวลาใหม่");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // =================== กันสร้างประกาศซ้ำ ===================
            // ถ้าสัตว์เลี้ยงตัวเดิม มีประกาศที่ "ยังไม่จบงาน" (รับสมัคร/รอพิจารณา/กำลังดูแล)
            // และช่วงวันที่ทับกับที่กำลังจะสร้างใหม่ ไม่อนุญาตให้สร้างซ้ำ
            Integer reqPetID = Integer.parseInt(data.get("petID").toString());
            List<String> activeStatuses = Arrays.asList("รับสมัคร", "รอพิจารณา", "กำลังดูแล");
            List<Announcement> existingForPet = announcementRepository.findByPetID(reqPetID);
            for (Announcement exist : existingForPet) {
                if (!activeStatuses.contains(exist.getStatus())) continue;
                LocalDate exStart = exist.getStartdate();
                LocalDate exEnd = exist.getEnddate();
                if (exStart == null || exEnd == null) continue;
                boolean overlap = !reqEnddate.isBefore(exStart) && !reqStartdate.isAfter(exEnd);
                if (overlap) {
                    response.put("message", "สัตว์เลี้ยงตัวนี้มีประกาศในช่วงวันที่นี้อยู่แล้ว กรุณาเลือกวันที่อื่น หรือรอให้ประกาศเดิมเสร็จสิ้น/ถูกยกเลิกก่อน");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // บันทึก CarePet ก่อน
            CarePet carePet = new CarePet();
            carePet.setCareMorning(getBool(data, "careMorning"));
            carePet.setCareAfternoon(getBool(data, "careAfternoon"));
            carePet.setCareEvening(getBool(data, "careEvening"));
            carePet.setCareNight(getBool(data, "careNight"));
            carePet.setFeedMorning(getBool(data, "feedMorning"));
            carePet.setFeedAfternoon(getBool(data, "feedAfternoon"));
            carePet.setFeedEvening(getBool(data, "feedEvening"));
            carePet.setFeedNight(getBool(data, "feedNight"));
            carePet.setFeedMorningDetail(getString(data, "feedMorningDetail"));
            carePet.setFeedAfternoonDetail(getString(data, "feedAfternoonDetail"));
            carePet.setFeedEveningDetail(getString(data, "feedEveningDetail"));
            carePet.setFeedNightDetail(getString(data, "feedNightDetail"));
            carePet.setIsFoodPrepared(getBool(data, "isFoodPrepared"));
            carePet.setFoodAmount(getString(data, "foodAmount"));
            carePet.setMedicineDetail(getString(data, "medicineDetail"));
            // เก็บอึ/ฉี่ เป็นบริการมาตรฐานที่รวมอยู่ในทุกประกาศเสมอ ไม่ใช่บริการเสริมที่เลือกได้อีกต่อไป
            carePet.setIsCleanService(true);
            carePet.setIsWalkService(getBool(data, "isWalkService"));
            CarePet savedCarePet = carePetRepository.save(carePet);

            // บันทึก AssignJob
            // หมายเหตุ: ไม่ตั้งค่า workStatus/isPaymentConfirmed ตั้งแต่ตอนสร้างประกาศ
            // ปล่อยเป็น null ไว้ก่อน เพราะ frontend เช็ค !ann?.workStatus เพื่อโชว์ปุ่ม "งานเสร็จสมบูรณ์"
            // ค่าจริงจะถูกตั้งตอนอัปโหลดสลิป (workStatus) และตอนผู้ดูแลยืนยันรับเงิน (isPaymentConfirmed) เท่านั้น
            AssignJob assignJob = new AssignJob();
            AssignJob savedAssignJob = assignJobRepository.save(assignJob);

            // บันทึก Announcement
            Announcement ann = new Announcement();
            ann.setOwnerID(Integer.parseInt(data.get("ownerID").toString()));
            ann.setPetID(Integer.parseInt(data.get("petID").toString()));
            ann.setPostdate(LocalDateTime.now());
            ann.setStartdate(LocalDate.parse(getString(data, "startdate")));
            ann.setEnddate(LocalDate.parse(getString(data, "enddate")));
            ann.setCurrentweight(getString(data, "currentweight"));
            ann.setAddressNo(getString(data, "addressNo"));
            ann.setStreet(getString(data, "street"));
            ann.setSubdistrict(getString(data, "subdistrict"));
            ann.setDistrict(getString(data, "district"));
            ann.setProvince(getString(data, "province"));
            ann.setZipcode(getString(data, "zipcode"));
            Object lat = data.get("latitude");
            Object lng = data.get("longitude");
            ann.setLatitude(lat != null ? Double.parseDouble(lat.toString()) : null);
            ann.setLongitude(lng != null ? Double.parseDouble(lng.toString()) : null);
            ann.setStatus("รับสมัคร");
            ann.setCarepetID(savedCarePet.getCarepetID());
            ann.setAssignJobID(savedAssignJob.getDetailID());
            ann.setTotalAmount(0.0);

            Announcement saved = announcementRepository.save(ann);
            response.put("message", "สร้างประกาศเรียบร้อยแล้ว");
            response.put("announceID", saved.getAnnounceID());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "ไม่สามารถสร้างประกาศได้ กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }


    // =================== ค้นหาประกาศด้วยชื่อตำบล/อำเภอ ===================
    @GetMapping("/search-by-area")
    public ResponseEntity<List<Map<String, Object>>> searchByArea(
            @RequestParam String keyword,
            @RequestParam(required = false) String petType) {

        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<Announcement> allAnn = announcementRepository.findAll();
            for (Announcement ann : allAnn) {
                // แสดงเฉพาะสถานะที่ยังเปิดรับสมัคร (ไม่โชว์ที่หมดอายุ/ได้รับผู้ดูแลแล้ว/กำลังดูแล/เสร็จสิ้น)
                if (!"รับสมัคร".equals(ann.getStatus()) && !"รอพิจารณา".equals(ann.getStatus())) continue;
                boolean matchArea = false;
                if (ann.getSubdistrict() != null && ann.getSubdistrict().contains(keyword)) matchArea = true;
                if (ann.getDistrict() != null && ann.getDistrict().contains(keyword)) matchArea = true;
                if (!matchArea) continue;

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

                final boolean[] matchPetType = {true};
                petRepository.findById(ann.getPetID()).ifPresent(pet -> {
                    String pType = pet.getPetType() != null ? pet.getPetType().getPetTypeName() : "";
                    if (petType != null && !petType.equals("ทั้งหมด") && !petType.isEmpty()) {
                        if (!pType.contains(petType)) { matchPetType[0] = false; return; }
                    }
                    map.put("petName", pet.getPetName());
                    map.put("petType", pType);
                    map.put("breed", pet.getBreed());
                    map.put("gender", pet.getGender());
                    map.put("birthDate", pet.getBirthDate());
                    map.put("petImage", pet.getPetImage());
                });

                if (!matchPetType[0] || !map.containsKey("petName")) continue;
                result.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // เรียงตามวันที่เริ่มดูแล (startdate) จากใกล้ถึงที่สุดไปไกลที่สุด
        result.sort(Comparator.comparing(m -> (LocalDate) m.get("startdate"), Comparator.nullsLast(Comparator.naturalOrder())));
        return ResponseEntity.ok(result);
    }

    // =================== ดึงรายการประกาศของเจ้าของ ===================
    @GetMapping("/my-announcements/{ownerID}")
    public ResponseEntity<List<Map<String, Object>>> getMyAnnouncements(@PathVariable Integer ownerID) {
        List<Announcement> announcements = announcementRepository.findByOwnerIDOrderByPostdateDesc(ownerID);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Announcement ann : announcements) {
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

            // ดึงข้อมูลสัตว์เลี้ยง
            petRepository.findById(ann.getPetID()).ifPresent(pet -> {
                map.put("petName", pet.getPetName());
                map.put("petImage", pet.getPetImage());
                map.put("petType", pet.getPetType() != null ? pet.getPetType().getPetTypeName() : "");
                map.put("breed", pet.getBreed());
                map.put("gender", pet.getGender());
                map.put("birthDate", pet.getBirthDate());
            });

            // นับจำนวนผู้สมัคร
         // นับจำนวนผู้สมัคร (เฉพาะคำขอล่าสุดของแต่ละคน)
            List<ApplyJob> applicants = applyJobRepository.findByAnnounceID(ann.getAnnounceID());
            Map<Integer, ApplyJob> latestBySitter = new LinkedHashMap<>();
            for (ApplyJob apply : applicants) {
                latestBySitter.put(apply.getSitterID(), apply);
            }
            map.put("applicantCount", latestBySitter.size());

            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // =================== ดึงรายละเอียดประกาศ ===================
    // =================== ดึงรายละเอียดประกาศ (Public — ไม่ต้อง login) ===================
    // สโคปข้อมูลจำกัด: ไม่มีที่อยู่เต็ม/พิกัด, ไม่มีข้อมูลเจ้าของ, ไม่มีรายชื่อผู้สมัคร, ไม่มีสถานะงาน/สลิป
    @GetMapping("/detail-public/{announceID}")
    public ResponseEntity<Map<String, Object>> getAnnouncementDetailPublic(@PathVariable Integer announceID) {
        Optional<Announcement> optional = announcementRepository.findById(announceID);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Announcement ann = optional.get();
        Map<String, Object> response = new HashMap<>();

        response.put("announceID", ann.getAnnounceID());
        response.put("postdate", ann.getPostdate());
        response.put("startdate", ann.getStartdate());
        response.put("enddate", ann.getEnddate());
        response.put("currentweight", ann.getCurrentweight());
        // ที่อยู่ระดับกว้างเท่านั้น (ไม่มี addressNo/street/zipcode/latitude/longitude)
        response.put("subdistrict", ann.getSubdistrict());
        response.put("district", ann.getDistrict());
        response.put("province", ann.getProvince());
        response.put("status", ann.getStatus());

        // ข้อมูลสัตว์เลี้ยง (โชว์เต็ม ไม่อ่อนไหว)
        petRepository.findById(ann.getPetID()).ifPresent(pet -> {
            Map<String, Object> petMap = new HashMap<>();
            petMap.put("petID", pet.getPetID());
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
            petMap.put("behaviorStressAlone", pet.getBehaviorStressAlone());
            petMap.put("behaviorFriendly", pet.getBehaviorFriendly());
            petMap.put("behaviorFearStranger", pet.getBehaviorFearStranger());
            petMap.put("behaviorFearLoudSound", pet.getBehaviorFearLoudSound());
            petMap.put("behaviorBarkLoud", pet.getBehaviorBarkLoud());
            petMap.put("behaviorDislikeTouch", pet.getBehaviorDislikeTouch());
            petMap.put("behaviorBiteScrath", pet.getBehaviorBiteScrath());
            petMap.put("behaviorEscapeExpert", pet.getBehaviorEscapeExpert());
            petMap.put("behaviorHighEnergy", pet.getBehaviorHighEnergy());
            petMap.put("behaviorJumpOnPeople", pet.getBehaviorJumpOnPeople());
            petMap.put("behaviorHardControl", pet.getBehaviorHardControl());
            response.put("pet", petMap);
        });

        // ข้อมูลการดูแล (โชว์ได้ ต้องใช้ตัดสินใจว่าจะสมัครไหม)
        if (ann.getCarepetID() != null) {
            carePetRepository.findById(ann.getCarepetID()).ifPresent(cp -> {
                response.put("carePet", cp);
            });
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{announceID}")
    public ResponseEntity<Map<String, Object>> getAnnouncementDetail(@PathVariable Integer announceID) {
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
        response.put("emergencyMessage", ann.getEmergencyMessage());
        response.put("emergencyImage", ann.getEmergencyImage());
        response.put("emergencyReportedAt", ann.getEmergencyReportedAt());

        // ข้อมูลเจ้าของ
        petOwnerRepository.findById(ann.getOwnerID()).ifPresent(owner -> {
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
        });

        // ข้อมูลสัตว์เลี้ยง
        petRepository.findById(ann.getPetID()).ifPresent(pet -> {
            Map<String, Object> petMap = new HashMap<>();
            petMap.put("petID", pet.getPetID());
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
            petMap.put("behaviorStressAlone", pet.getBehaviorStressAlone());
            petMap.put("behaviorFriendly", pet.getBehaviorFriendly());
            petMap.put("behaviorFearStranger", pet.getBehaviorFearStranger());
            petMap.put("behaviorFearLoudSound", pet.getBehaviorFearLoudSound());
            petMap.put("behaviorBarkLoud", pet.getBehaviorBarkLoud());
            petMap.put("behaviorDislikeTouch", pet.getBehaviorDislikeTouch());
            petMap.put("behaviorBiteScrath", pet.getBehaviorBiteScrath());
            petMap.put("behaviorEscapeExpert", pet.getBehaviorEscapeExpert());
            petMap.put("behaviorHighEnergy", pet.getBehaviorHighEnergy());
            petMap.put("behaviorJumpOnPeople", pet.getBehaviorJumpOnPeople());
            petMap.put("behaviorHardControl", pet.getBehaviorHardControl());
            petMap.put("isDeceased", Boolean.TRUE.equals(pet.getIsDeceased()));
            response.put("pet", petMap);
        });

        // ข้อมูลการดูแล
        if (ann.getCarepetID() != null) {
            carePetRepository.findById(ann.getCarepetID()).ifPresent(cp -> {
                response.put("carePet", cp);
            });
        }
        
     // ดึงรายงานการดูแล (PetCareMedia)
        if (ann.getAssignJobID() != null) {
            assignJobRepository.findById(ann.getAssignJobID()).ifPresent(aj -> {
                response.put("workStatus", aj.getWorkStatus());
                response.put("slipImage", aj.getSlipImage());
                response.put("isPaymentConfirmed", aj.getIsPaymentConfirmed());

                List<PetCareMedia> reports = petCareMediaRepository.findByDetailID(aj.getDetailID());
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
                response.put("reportedSlots", reports.stream().map(PetCareMedia::getTimecare).toList());
            });
        }

        // รายชื่อผู้สมัคร
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

            petSitterRepository.findById(apply.getSitterID()).ifPresent(sitter -> {
                applyMap.put("sitterName", sitter.getFirstname() + " " + sitter.getLastname());
                applyMap.put("sitterImage", sitter.getSitterImage());
                applyMap.put("pricePerDay", sitter.getPricePerDay());
                applyMap.put("subdistrict", sitter.getSubdistrict());
                applyMap.put("district", sitter.getDistrict());
                applyMap.put("province", sitter.getProvince());
                applyMap.put("petAlowPet", sitter.getPetAllowType());

                // คะแนนรีวิว
                Double lat1 = ann.getLatitude();
                Double lng1 = ann.getLongitude();
                Double lat2 = sitter.getLatitude();
                Double lng2 = sitter.getLongitude();
                if (lat1 != null && lng1 != null && lat2 != null && lng2 != null) {
                    double dist = calculateDistance(lat1, lng1, lat2, lng2);
                    applyMap.put("distance", Math.round(dist * 10.0) / 10.0);
                }
            });
            applicantList.add(applyMap);
        }
        response.put("applicants", applicantList);
        response.put("applicantCount", applicantList.size());
        
     // ดึงรายงานการดูแลจาก AssignJob
        if (ann.getAssignJobID() != null) {
            assignJobRepository.findById(ann.getAssignJobID()).ifPresent(aj -> {
                response.put("workStatus", aj.getWorkStatus());
                response.put("slipImage", aj.getSlipImage());
                response.put("isPaymentConfirmed", aj.getIsPaymentConfirmed());
            });
        }

        return ResponseEntity.ok(response);
    }

    // =================== อนุมัติผู้ดูแล ===================
    @Transactional
    @PutMapping("/approve/{applyJobID}")
    public ResponseEntity<Map<String, String>> approveApplicant(@PathVariable Integer applyJobID) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<ApplyJob> optional = applyJobRepository.findById(applyJobID);
            if (optional.isEmpty()) {
                response.put("message", "ไม่พบข้อมูลคำขอ");
                return ResponseEntity.badRequest().body(response);
            }

            ApplyJob approved = optional.get();
            approved.setAppStatus("ได้รับเลือก");
            applyJobRepository.save(approved);

            // อัพเดทสถานะคนอื่น
            List<ApplyJob> others = applyJobRepository.findByAnnounceID(approved.getAnnounceID());
            for (ApplyJob other : others) {
                if (!other.getApplyJobID().equals(applyJobID)) {
                    other.setAppStatus("ไม่ได้รับเลือก");
                    applyJobRepository.save(other);

                    // แจ้งเตือนผู้ดูแลที่ไม่ได้รับเลือก
                    notificationController.saveNotif(
                        other.getSitterID(), "SITTER",
                        "😔 ขออภัย เจ้าของเลือกผู้ดูแลท่านอื่นแล้ว ลองงานอื่นดูใหม่ได้เลย",
                        other.getAnnounceID()
                    );
                }
            }

            // อัพเดทสถานะประกาศ + คำนวณ totalAmount
            announcementRepository.findById(approved.getAnnounceID()).ifPresent(ann -> {
                ann.setStatus("ได้รับผู้ดูแลแล้ว");

                // คำนวณ totalAmount
                petSitterRepository.findById(approved.getSitterID()).ifPresent(sitter -> {
                    if (ann.getStartdate() != null && ann.getEnddate() != null && sitter.getPricePerDay() != null) {
                        long days = ChronoUnit.DAYS.between(ann.getStartdate(), ann.getEnddate()) + 1;
                        ann.setTotalAmount(days * Double.parseDouble(sitter.getPricePerDay().toString()));
                    }
                });
                
             // แจ้งเตือนผู้ดูแลว่าได้รับเลือก
                notificationController.saveNotif(
                    approved.getSitterID(), "SITTER",
                    "🎉 ยินดีด้วย คุณได้รับเลือกให้ดูแลสัตว์เลี้ยง",
                    approved.getAnnounceID()
                );

                announcementRepository.save(ann);
            });

            response.put("message", "อนุมัติผู้ดูแลเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =================== ปิดงาน (งานเสร็จสมบูรณ์) ===================
    @PutMapping("/complete/{announceID}")
    public ResponseEntity<Map<String, String>> completeJob(@PathVariable Integer announceID) {
        Map<String, String> response = new HashMap<>();
        Optional<Announcement> optional = announcementRepository.findById(announceID);
        if (optional.isEmpty()) {
            response.put("message", "ไม่พบประกาศ");
            return ResponseEntity.badRequest().body(response);
        }
        Announcement ann = optional.get();
        // หมายเหตุ: ไม่เปลี่ยนสถานะเป็น "งานเสร็จสิ้น" ที่นี่ เพราะยังไม่ได้ชำระเงินจริง
        // สถานะจะเปลี่ยนเป็น "งานเสร็จสิ้น" ตอนเจ้าของอัปโหลดสลิปเท่านั้น (ดู uploadSlip ด้านล่าง)
        response.put("message", "ส่งไปหน้าชำระเงินเรียบร้อยแล้ว");
        response.put("totalAmount", ann.getTotalAmount() != null ? ann.getTotalAmount().toString() : "0");
        response.put("announceID", announceID.toString());
        return ResponseEntity.ok(response);
    }

 // =================== ลบประกาศ ===================
    @DeleteMapping("/delete/{announceID}")
    public ResponseEntity<Map<String, String>> deleteAnnouncement(@PathVariable Integer announceID) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Announcement> optional = announcementRepository.findById(announceID);
            if (optional.isEmpty()) {
                response.put("message", "ไม่พบประกาศ");
                return ResponseEntity.badRequest().body(response);
            }

            // เช็คว่ามีผู้สมัครที่ยัง active อยู่ไหม (รอพิจารณา หรือ ได้รับเลือก)
            List<ApplyJob> applicants = applyJobRepository.findByAnnounceID(announceID);
            boolean hasActiveApplicant = applicants.stream()
                .anyMatch(a -> "รอพิจารณา".equals(a.getAppStatus()) || "ได้รับเลือก".equals(a.getAppStatus()));
            if (hasActiveApplicant) {
                response.put("message", "ไม่สามารถลบได้ เนื่องจากมีผู้สมัครอยู่");
                return ResponseEntity.badRequest().body(response);
            }

            Announcement ann = optional.get();

            // เก็บ id ไว้ก่อนลบ Announcement (ต้องลบตัวลูกก่อน ไม่งั้น FK จะบล็อกตอนลบ CarePet/AssignJob)
            Integer carepetID = ann.getCarepetID();
            Integer assignJobID = ann.getAssignJobID();

            // ลบ Announcement ก่อน (ตัวลูกที่อ้างอิงไปยัง CarePet/AssignJob)
            announcementRepository.deleteById(announceID);

            // ลบ CarePet (ตอนนี้ไม่มีใครอ้างอิงแล้ว ลบได้ปลอดภัย)
            if (carepetID != null) {
                carePetRepository.deleteById(carepetID);
            }

            // ลบ AssignJob
            if (assignJobID != null) {
                assignJobRepository.deleteById(assignJobID);
            }

            response.put("message", "ลบประกาศเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // =================== แก้ไขประกาศ ===================
    @PutMapping("/update/{announceID}")
    public ResponseEntity<Map<String, String>> updateAnnouncement(
            @PathVariable Integer announceID,
            @RequestBody Map<String, Object> data) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Announcement> optional = announcementRepository.findById(announceID);
            if (optional.isEmpty()) {
                response.put("message", "ไม่พบประกาศ");
                return ResponseEntity.badRequest().body(response);
            }

            // เช็คว่ามีผู้สมัครแล้วหรือยัง
            List<ApplyJob> applicants = applyJobRepository.findByAnnounceID(announceID);
            if (!applicants.isEmpty()) {
                response.put("message", "ไม่สามารถแก้ไขประกาศได้ เนื่องจากมีผู้สมัครแล้ว");
                return ResponseEntity.badRequest().body(response);
            }

            Announcement ann = optional.get();
            ann.setPetID(Integer.parseInt(data.get("petID").toString()));
            ann.setStartdate(LocalDate.parse(data.get("startdate").toString()));
            ann.setEnddate(LocalDate.parse(data.get("enddate").toString()));
            ann.setCurrentweight(getString(data, "currentweight"));
            ann.setAddressNo(getString(data, "addressNo"));
            ann.setStreet(getString(data, "street"));
            ann.setSubdistrict(getString(data, "subdistrict"));
            ann.setDistrict(getString(data, "district"));
            ann.setProvince(getString(data, "province"));
            ann.setZipcode(getString(data, "zipcode"));
            ann.setLatitude(Double.parseDouble(data.get("latitude").toString()));
            ann.setLongitude(Double.parseDouble(data.get("longitude").toString()));
            announcementRepository.save(ann);

            // อัพเดท CarePet
            if (ann.getCarepetID() != null) {
                carePetRepository.findById(ann.getCarepetID()).ifPresent(cp -> {
                    cp.setCareMorning(getBool(data, "careMorning"));
                    cp.setCareAfternoon(getBool(data, "careAfternoon"));
                    cp.setCareEvening(getBool(data, "careEvening"));
                    cp.setCareNight(getBool(data, "careNight"));
                    cp.setFeedMorning(getBool(data, "feedMorning"));
                    cp.setFeedAfternoon(getBool(data, "feedAfternoon"));
                    cp.setFeedEvening(getBool(data, "feedEvening"));
                    cp.setFeedNight(getBool(data, "feedNight"));
                    cp.setFeedMorningDetail(getString(data, "feedMorningDetail"));
                    cp.setFeedAfternoonDetail(getString(data, "feedAfternoonDetail"));
                    cp.setFeedEveningDetail(getString(data, "feedEveningDetail"));
                    cp.setFeedNightDetail(getString(data, "feedNightDetail"));
                    cp.setIsFoodPrepared(getBool(data, "isFoodPrepared"));
                    cp.setFoodAmount(getString(data, "foodAmount"));
                    cp.setMedicineDetail(getString(data, "medicineDetail"));
                    // เก็บอึ/ฉี่ เป็นบริการมาตรฐานที่รวมอยู่ในทุกประกาศเสมอ ไม่ใช่บริการเสริมที่เลือกได้อีกต่อไป
                    cp.setIsCleanService(true);
                    cp.setIsWalkService(getBool(data, "isWalkService"));
                    carePetRepository.save(cp);
                });
            }

            response.put("message", "แก้ไขข้อมูลเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
 // =================== อัพโหลดสลิป (เจ้าของ) ===================
    @PostMapping("/upload-slip/{announceID}")
    public ResponseEntity<Map<String, String>> uploadSlip(
            @PathVariable Integer announceID,
            @RequestParam("slipImage") MultipartFile slipImage) {
        Map<String, String> response = new HashMap<>();
        try {
            Optional<Announcement> optional = announcementRepository.findById(announceID);
            if (optional.isEmpty()) {
                response.put("message", "ไม่พบประกาศ");
                return ResponseEntity.badRequest().body(response);
            }
            Announcement ann = optional.get();
            if (ann.getAssignJobID() == null) {
                response.put("message", "ไม่พบข้อมูลงาน");
                return ResponseEntity.badRequest().body(response);
            }

            // ต้องมีผู้ดูแลที่ได้รับเลือกจริง จึงจะชำระเงินได้
            Optional<ApplyJob> selectedOpt = applyJobRepository.findByAnnounceID(announceID).stream()
                .filter(a -> "ได้รับเลือก".equals(a.getAppStatus()))
                .findFirst();
            if (selectedOpt.isEmpty()) {
                response.put("message", "ไม่พบผู้ดูแลที่ได้รับเลือกสำหรับงานนี้");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<AssignJob> ajOpt = assignJobRepository.findById(ann.getAssignJobID());
            if (ajOpt.isEmpty()) {
                response.put("message", "ไม่พบข้อมูลงาน");
                return ResponseEntity.badRequest().body(response);
            }
            AssignJob aj = ajOpt.get();

            // กันอัปโหลดซ้ำ — งานนี้ชำระเงินและปิดงานไปแล้ว
            if ("ชำระเงินเรียบร้อยแล้ว".equals(aj.getIsPaymentConfirmed())) {
                response.put("message", "งานนี้ชำระเงินเรียบร้อยแล้ว");
                return ResponseEntity.badRequest().body(response);
            }

            String fileName = saveFile(slipImage, uploadDirSlips);
            if (fileName == null) {
                response.put("message", "เกิดข้อผิดพลาดในการอัปโหลดสลิป กรุณาลองใหม่อีกครั้ง");
                return ResponseEntity.badRequest().body(response);
            }

            // ระบบใหม่: เจ้าของอัปโหลดสลิป = ชำระเงินเรียบร้อย + งานเสร็จสิ้นทันที (ไม่ต้องรอผู้ดูแลยืนยัน)
            // ต่างฝ่ายต่างถูกพาไปหน้ารีวิวจนกว่าจะรีวิว (เช็คที่ /api/review/check)
            aj.setSlipImage(fileName);
            aj.setWorkStatus("ชำระเงินแล้ว");
            aj.setIsPaymentConfirmed("ชำระเงินเรียบร้อยแล้ว");
            assignJobRepository.save(aj);

            ann.setStatus("งานเสร็จสิ้น");
            announcementRepository.save(ann);

            // แจ้งเตือนผู้ดูแลว่าได้รับเงินแล้ว และให้รีวิวเจ้าของสัตว์เลี้ยง
            notificationController.saveNotif(
                selectedOpt.get().getSitterID(), "SITTER",
                "💰 เจ้าของสัตว์เลี้ยงชำระเงินแล้ว งานเสร็จสมบูรณ์ กรุณารีวิวเจ้าของสัตว์เลี้ยง",
                announceID
            );

            response.put("message", "อัพโหลดสลิปเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =================== Helper ===================
    private String getString(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val == null ? "" : val.toString().trim();
    }

    private Boolean getBool(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null && Boolean.parseBoolean(val.toString());
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
        } catch (Exception e) { return null; }
    }
}