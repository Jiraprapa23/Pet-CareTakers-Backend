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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PetCareMedia")
public class PetCareMedia {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "petCaremeID")
    private Integer petCaremeID;

    // =================== รูปภาพ/สื่อ ===================
    @Column(name = "image", length = 2000)
    private String image; // JSON array เช่น ["file1.jpg","file2.jpg"]

    @Column(name = "video", length = 255)
    private String video; // ชื่อไฟล์วิดีโอ 1 ไฟล์

    // =================== รายละเอียด ===================
    @Column(name = "timecare", length = 50)
    private String timecare; // เช่น "ช่วงเช้า"

    @Column(name = "careDate")
    private LocalDate careDate; // วันที่ (ปฏิทิน) ของการดูแลนี้ — แยกรายงานแต่ละวันออกจากกัน

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    // =================== FK (ท้ายสุด) ===================
    @Column(name = "detailID")
    private Integer detailID; // FK → AssignJob

    // Phase 5: relationship แบบ read-only
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detailID", referencedColumnName = "detailID", insertable = false, updatable = false)
    private AssignJob assignJob;

    public PetCareMedia() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getPetCaremeID() { return petCaremeID; }
    public void setPetCaremeID(Integer petCaremeID) { this.petCaremeID = petCaremeID; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }

    public String getTimecare() { return timecare; }
    public void setTimecare(String timecare) { this.timecare = timecare; }

    public LocalDate getCareDate() { return careDate; }
    public void setCareDate(LocalDate careDate) { this.careDate = careDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getDetailID() { return detailID; }
    public void setDetailID(Integer detailID) { this.detailID = detailID; }

    public AssignJob getAssignJob() { return assignJob; }
}