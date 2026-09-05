package com.springboot.controller;

import com.springboot.model.PetSitter;
import com.springboot.model.Review;
import com.springboot.repository.PetSitterRepository;
import com.springboot.repository.ReviewRepository;
import com.springboot.repository.PetOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sitter")
public class SitterController {

    @Autowired
    private PetSitterRepository petSitterRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PetOwnerRepository petOwnerRepository;

    // =================== ค้นหาผู้ดูแลใกล้เคียง ===================
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchSitters(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) String petType,
            @RequestParam(defaultValue = "5.0") Double radius) {

        List<PetSitter> allSitters = petSitterRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (PetSitter sitter : allSitters) {
            if (sitter.getLatitude() == null || sitter.getLongitude() == null) continue;

            // คำนวณระยะทาง (Haversine formula)
            double distance = calculateDistance(lat, lng, sitter.getLatitude(), sitter.getLongitude());

            // กรองรัศมี 5 กม.
            if (distance > radius) continue;

            // กรองประเภทสัตว์
            if (petType != null && !petType.equals("ทั้งหมด") && !petType.isEmpty()) {
                String allowType = sitter.getPetAllowType() != null ? sitter.getPetAllowType() : "";
                if (!allowType.contains(petType) && !allowType.equals("ทั้งแมวและสุนัข") && !allowType.equals("ทั้งหมด")) continue;
            }

            // คำนวณคะแนนรีวิวเฉลี่ย
            Double avgRating = reviewRepository.getAverageRatingBySitter(sitter.getSitterID());
            Long reviewCount = reviewRepository.countByTargetIDAndTargetRole(sitter.getSitterID(), "SITTER");

            Map<String, Object> sitterMap = new HashMap<>();
            sitterMap.put("sitterID", sitter.getSitterID());
            sitterMap.put("firstname", sitter.getFirstname());
            sitterMap.put("lastname", sitter.getLastname());
            sitterMap.put("gender", sitter.getGender());
            sitterMap.put("sitterImage", sitter.getSitterImage());
            sitterMap.put("pricePerDay", sitter.getPricePerDay());
            sitterMap.put("subdistrict", sitter.getSubdistrict());
            sitterMap.put("district", sitter.getDistrict());
            sitterMap.put("province", sitter.getProvince());
            sitterMap.put("petAlowPet", sitter.getPetAllowType());
            sitterMap.put("acceptedPetSize", sitter.getAcceptedPetSize());
            sitterMap.put("distance", Math.round(distance * 10.0) / 10.0);
            sitterMap.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
            sitterMap.put("reviewCount", reviewCount);

            result.add(sitterMap);
        }

        // เรียงจากใกล้ไปไกล
        result.sort(Comparator.comparingDouble(m -> (Double) m.get("distance")));

        return ResponseEntity.ok(result);
    }

    // =================== ดึงโปรไฟล์ผู้ดูแล + รีวิว ===================
    @GetMapping("/profile/{sitterID}")
    public ResponseEntity<Map<String, Object>> getSitterProfile(@PathVariable Integer sitterID) {
        Optional<PetSitter> optional = petSitterRepository.findById(sitterID);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        PetSitter sitter = optional.get();
        Map<String, Object> response = new HashMap<>();

        // ข้อมูลผู้ดูแล
        response.put("sitterID", sitter.getSitterID());
        response.put("firstname", sitter.getFirstname());
        response.put("lastname", sitter.getLastname());
        response.put("gender", sitter.getGender());
        response.put("birthdate", sitter.getBirthdate());
        response.put("phoneNumber", sitter.getPhoneNumber());
        response.put("email", sitter.getEmail());
        response.put("sitterImage", sitter.getSitterImage());
        response.put("addressNo", sitter.getAddressNo());
        response.put("street", sitter.getStreet());
        response.put("subdistrict", sitter.getSubdistrict());
        response.put("district", sitter.getDistrict());
        response.put("province", sitter.getProvince());
        response.put("zipcode", sitter.getZipcode());
        response.put("latitude", sitter.getLatitude());
        response.put("longitude", sitter.getLongitude());
        response.put("petAlowPet", sitter.getPetAllowType());
        response.put("acceptedPetSize", sitter.getAcceptedPetSize());
        response.put("experienceYear", sitter.getExperienceYear());
        response.put("pricePerDay", sitter.getPricePerDay());
        response.put("isMorning", sitter.getIsMorning());
        response.put("isAfternoon", sitter.getIsAfternoon());
        response.put("isEvening", sitter.getIsEvening());
        response.put("isNight", sitter.getIsNight());
        response.put("isFeedMedicine", sitter.getIsFeedMedicine());
        response.put("isCleanService", sitter.getIsCleanService());
        response.put("isWalkService", sitter.getIsWalkService());

        // รีวิว
        List<Review> reviews = reviewRepository.findByTargetIDAndTargetRoleOrderByCreatedAtDesc(sitterID, "SITTER");
        List<Map<String, Object>> reviewList = new ArrayList<>();

        for (Review review : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("reviewID", review.getReviewID());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("comment", review.getComment());
            reviewMap.put("createdAt", review.getCreatedAt());
            reviewMap.put("reviewerRole", review.getReviewerRole());

            // ดึงชื่อผู้รีวิว
            if ("OWNER".equals(review.getReviewerRole())) {
                petOwnerRepository.findById(review.getReviewerID()).ifPresent(owner -> {
                    reviewMap.put("reviewerName", owner.getFirstname() + " " + owner.getLastname());
                    reviewMap.put("reviewerImage", owner.getProfileImage());
                });
            }

            reviewList.add(reviewMap);
        }

        Double avgRating = reviewRepository.getAverageRatingBySitter(sitterID);
        Long reviewCount = reviewRepository.countByTargetIDAndTargetRole(sitterID, "SITTER");

        response.put("reviews", reviewList);
        response.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
        response.put("reviewCount", reviewCount);

        return ResponseEntity.ok(response);
    }


    // =================== ค้นหาผู้ดูแลด้วยชื่อตำบล/อำเภอ ===================
    @GetMapping("/search-by-area")
    public ResponseEntity<List<Map<String, Object>>> searchSittersByArea(
            @RequestParam String keyword,
            @RequestParam(required = false) String petType,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {

        List<PetSitter> allSitters = petSitterRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (PetSitter sitter : allSitters) {
            // กรองตำบล/อำเภอ
            boolean matchArea = false;
            if (sitter.getSubdistrict() != null && sitter.getSubdistrict().contains(keyword)) matchArea = true;
            if (sitter.getDistrict() != null && sitter.getDistrict().contains(keyword)) matchArea = true;
            if (!matchArea) continue;

            // กรองประเภทสัตว์
            if (petType != null && !petType.equals("ทั้งหมด") && !petType.isEmpty()) {
                String allowType = sitter.getPetAllowType() != null ? sitter.getPetAllowType() : "";
                if (!allowType.contains(petType) && !allowType.equals("ทั้งแมวและสุนัข") && !allowType.equals("ทั้งหมด")) continue;
            }

            Double avgRating = reviewRepository.getAverageRatingBySitter(sitter.getSitterID());
            Long reviewCount = reviewRepository.countByTargetIDAndTargetRole(sitter.getSitterID(), "SITTER");

            Map<String, Object> sitterMap = new HashMap<>();
            sitterMap.put("sitterID", sitter.getSitterID());
            sitterMap.put("firstname", sitter.getFirstname());
            sitterMap.put("lastname", sitter.getLastname());
            sitterMap.put("gender", sitter.getGender());
            sitterMap.put("sitterImage", sitter.getSitterImage());
            sitterMap.put("pricePerDay", sitter.getPricePerDay());
            sitterMap.put("subdistrict", sitter.getSubdistrict());
            sitterMap.put("district", sitter.getDistrict());
            sitterMap.put("province", sitter.getProvince());
            sitterMap.put("petAlowPet", sitter.getPetAllowType());
            sitterMap.put("acceptedPetSize", sitter.getAcceptedPetSize());
            sitterMap.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
            sitterMap.put("reviewCount", reviewCount);

            if (lat != null && lng != null && sitter.getLatitude() != null && sitter.getLongitude() != null) {
                double distance = calculateDistance(lat, lng, sitter.getLatitude(), sitter.getLongitude());
                sitterMap.put("distance", Math.round(distance * 10.0) / 10.0);
            }

            result.add(sitterMap);
        }

        return ResponseEntity.ok(result);
    }

    // =================== Haversine Formula ===================
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // รัศมีโลก กม.
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}