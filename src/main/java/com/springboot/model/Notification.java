package com.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
public class Notification {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notifID")
    private Integer notifID;

    // =================== รายละเอียด ===================
    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "isRead")
    private Boolean isRead = false;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    // =================== FK (ท้ายสุด) ===================
    // userID: ชี้ได้ทั้ง PetOwner/PetSitter แล้วแต่ role
    // (polymorphic — ทำ relationship object จริงไม่ได้ ตามที่คุยกันไว้)
    @Column(name = "userID")
    private Integer userID;

    @Column(name = "userRole", length = 10)
    private String userRole; // OWNER / SITTER

    @Column(name = "announceID")
    private Integer announceID;

    // Phase 5: relationship แบบ read-only
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announceID", referencedColumnName = "announceID", insertable = false, updatable = false)
    private Announcement announcement;

    public Notification() { this.createdAt = LocalDateTime.now(); this.isRead = false; }

    public Integer getNotifID() { return notifID; }
    public void setNotifID(Integer notifID) { this.notifID = notifID; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getUserID() { return userID; }
    public void setUserID(Integer userID) { this.userID = userID; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Integer getAnnounceID() { return announceID; }
    public void setAnnounceID(Integer announceID) { this.announceID = announceID; }

    public Announcement getAnnouncement() { return announcement; }
}