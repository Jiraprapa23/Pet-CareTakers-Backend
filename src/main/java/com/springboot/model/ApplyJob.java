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

@Entity
@Table(name = "ApplyJob")
public class ApplyJob {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applyJobID")
    private Integer applyJobID;

    // =================== รายละเอียด ===================
    @Column(name = "appStatus", length = 50)
    private String appStatus; // รอพิจารณา, ได้รับเลือก, ไม่ได้รับเลือก

    // =================== FK (ท้ายสุด) ===================
    @Column(name = "detailID")
    private Integer detailID; // FK → AssignJob

    @Column(name = "sitterID")
    private Integer sitterID; // FK → PetSitter

    @Column(name = "announceID")
    private Integer announceID; // FK → Announcement

    // Phase 5: relationship แบบ read-only
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detailID", referencedColumnName = "detailID", insertable = false, updatable = false)
    private AssignJob assignJob;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitterID", referencedColumnName = "sitterID", insertable = false, updatable = false)
    private PetSitter sitter;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announceID", referencedColumnName = "announceID", insertable = false, updatable = false)
    private Announcement announcement;

    // =================== Constructors ===================
    public ApplyJob() {}

    // =================== Getters & Setters ===================
    public Integer getApplyJobID() { return applyJobID; }
    public void setApplyJobID(Integer applyJobID) { this.applyJobID = applyJobID; }

    public String getAppStatus() { return appStatus; }
    public void setAppStatus(String appStatus) { this.appStatus = appStatus; }

    public Integer getDetailID() { return detailID; }
    public void setDetailID(Integer detailID) { this.detailID = detailID; }

    public Integer getSitterID() { return sitterID; }
    public void setSitterID(Integer sitterID) { this.sitterID = sitterID; }

    public Integer getAnnounceID() { return announceID; }
    public void setAnnounceID(Integer announceID) { this.announceID = announceID; }

    public AssignJob getAssignJob() { return assignJob; }
    public PetSitter getSitter() { return sitter; }
    public Announcement getAnnouncement() { return announcement; }
}