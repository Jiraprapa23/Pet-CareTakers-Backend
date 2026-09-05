package com.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "Review")
public class Review {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewID")
    private Long reviewID;

    // =================== รายละเอียด ===================
    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    // =================== FK (ท้ายสุด) ===================
    // ผู้รีวิว/ผู้ถูกรีวิว: ชี้ได้ทั้ง PetOwner/PetSitter แล้วแต่ role
    // (polymorphic — ทำ relationship object จริงไม่ได้ ตามที่คุยกันไว้)
    @Column(name = "reviewerID", nullable = false)
    private Integer reviewerID;

    @Column(name = "reviewerRole", nullable = false, length = 10)
    private String reviewerRole; // "OWNER" หรือ "SITTER"

    @Column(name = "targetID", nullable = false)
    private Integer targetID;

    @Column(name = "targetRole", nullable = false, length = 10)
    private String targetRole; // "OWNER" หรือ "SITTER"

    // FK ไปยัง Announcement
    @Column(name = "announcementID")
    private Integer announcementID;

    // Phase 5: relationship แบบ read-only
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcementID", referencedColumnName = "announceID", insertable = false, updatable = false)
    private Announcement announcement;

    // =================== Constructors ===================
    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    // =================== Getters & Setters ===================
    public Long getReviewID() { return reviewID; }
    public void setReviewID(Long reviewID) { this.reviewID = reviewID; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getReviewerID() { return reviewerID; }
    public void setReviewerID(Integer reviewerID) { this.reviewerID = reviewerID; }

    public String getReviewerRole() { return reviewerRole; }
    public void setReviewerRole(String reviewerRole) { this.reviewerRole = reviewerRole; }

    public Integer getTargetID() { return targetID; }
    public void setTargetID(Integer targetID) { this.targetID = targetID; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public Integer getAnnouncementID() { return announcementID; }
    public void setAnnouncementID(Integer announcementID) { this.announcementID = announcementID; }

    public Announcement getAnnouncement() { return announcement; }
}