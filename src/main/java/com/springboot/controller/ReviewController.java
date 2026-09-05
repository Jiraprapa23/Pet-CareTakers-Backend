package com.springboot.controller;

import com.springboot.model.*;
import com.springboot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private PetSitterRepository petSitterRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;

    // =================== ส่งรีวิว ===================
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitReview(@RequestBody Map<String, Object> data) {
        Map<String, String> response = new HashMap<>();
        try {
            Integer announceID = Integer.parseInt(data.get("announceID").toString());
            Integer reviewerID = Integer.parseInt(data.get("reviewerID").toString());
            String reviewerRole = data.get("reviewerRole").toString();
            Integer targetID = Integer.parseInt(data.get("targetID").toString());
            String targetRole = data.get("targetRole").toString();
            Double rating = Double.parseDouble(data.get("rating").toString());
            String comment = data.get("comment").toString().trim();

            // เช็คว่ารีวิวแล้วหรือยัง
            boolean alreadyReviewed = reviewRepository
                .existsByAnnouncementIDAndReviewerIDAndReviewerRole(announceID, reviewerID, reviewerRole);
            if (alreadyReviewed) {
                response.put("message", "คุณรีวิวงานนี้ไปแล้ว");
                return ResponseEntity.badRequest().body(response);
            }

            Review review = new Review();
            review.setAnnouncementID(announceID);
            review.setReviewerID(reviewerID);
            review.setReviewerRole(reviewerRole);
            review.setTargetID(targetID);
            review.setTargetRole(targetRole);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(LocalDateTime.now());
            reviewRepository.save(review);

            response.put("message", "ส่งรีวิวเรียบร้อยแล้ว");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // =================== เช็คว่ารีวิวแล้วหรือยัง ===================
    @GetMapping("/check/{announceID}/{reviewerID}/{reviewerRole}")
    public ResponseEntity<Map<String, Object>> checkReviewed(
            @PathVariable Integer announceID,
            @PathVariable Integer reviewerID,
            @PathVariable String reviewerRole) {
        Map<String, Object> response = new HashMap<>();
        boolean reviewed = reviewRepository
            .existsByAnnouncementIDAndReviewerIDAndReviewerRole(announceID, reviewerID, reviewerRole);
        response.put("reviewed", reviewed);
        return ResponseEntity.ok(response);
    }

    // =================== ดึงรีวิวของผู้ดูแล ===================
    @GetMapping("/sitter/{sitterID}")
    public ResponseEntity<List<Map<String, Object>>> getSitterReviews(@PathVariable Integer sitterID) {
        List<Review> reviews = reviewRepository.findByTargetIDAndTargetRole(sitterID, "SITTER");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewID", r.getReviewID());
            map.put("rating", r.getRating());
            map.put("comment", r.getComment());
            map.put("createdAt", r.getCreatedAt());
            petOwnerRepository.findById(r.getReviewerID()).ifPresent(owner -> {
                map.put("reviewerName", owner.getFirstname() + " " + owner.getLastname());
                map.put("reviewerImage", owner.getProfileImage());
            });
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}