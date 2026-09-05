package com.springboot.repository;

import com.springboot.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    boolean existsByAnnouncementIDAndReviewerIDAndReviewerRole(Integer announcementID, Integer reviewerID, String reviewerRole);

    // ดึงรีวิวของผู้ดูแล (targetID = sitterID, targetRole = SITTER)
    List<Review> findByTargetIDAndTargetRole(Integer targetID, String targetRole);

    // ดึงรีวิวของเจ้าของ (targetID = ownerID, targetRole = OWNER)
    List<Review> findByTargetIDAndTargetRoleOrderByCreatedAtDesc(Integer targetID, String targetRole);

    // คำนวณคะแนนเฉลี่ยของผู้ดูแล
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetID = :targetID AND r.targetRole = 'SITTER'")
    Double getAverageRatingBySitter(@Param("targetID") Integer targetID);

    // นับจำนวนรีวิวของผู้ดูแล
    Long countByTargetIDAndTargetRole(Integer targetID, String targetRole);
}