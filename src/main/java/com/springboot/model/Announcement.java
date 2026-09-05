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
@Table(name = "Announcement")
public class Announcement {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announceID")
    private Integer announceID;

    // =================== ที่อยู่ ===================
    @Column(name = "addressNo", length = 100)
    private String addressNo;

    @Column(name = "street", length = 100)
    private String street;

    @Column(name = "subdistrict", length = 255)
    private String subdistrict;

    @Column(name = "district", length = 255)
    private String district;

    @Column(name = "province", length = 255)
    private String province;

    @Column(name = "zipcode", length = 10)
    private String zipcode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // =================== รายละเอียด ===================
    @Column(name = "postdate")
    private LocalDateTime postdate;

    @Column(name = "startdate")
    private LocalDate startdate;

    @Column(name = "enddate")
    private LocalDate enddate;

    @Column(name = "currentweight", length = 50)
    private String currentweight;

    @Column(name = "totalAmount")
    private Double totalAmount;

    @Column(name = "status", length = 50)
    private String status; // รับสมัคร, รอพิจารณา, ได้รับผู้ดูแลแล้ว, กำลังดูแล, งานเสร็จสิ้น

    // =================== FK (ท้ายสุด) ===================
    @Column(name = "ownerID", nullable = false)
    private Integer ownerID;

    @Column(name = "petID", nullable = false)
    private Integer petID;

    @Column(name = "CarePet_carepetID")
    private Integer carepetID;

    @Column(name = "AssignJob_detailID")
    private Integer assignJobID;

    // Phase 5: relationship แบบ read-only
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerID", referencedColumnName = "ownetID", insertable = false, updatable = false)
    private PetOwner owner;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petID", referencedColumnName = "petID", insertable = false, updatable = false)
    private Pet pet;

    // EAGER แทน LAZY: ป้องกัน Hibernate session cache คืน proxy ตอน Controller
    // เรียก carePetRepository.findById()/assignJobRepository.findById() แยกต่างหาก
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CarePet_carepetID", referencedColumnName = "carepetID", insertable = false, updatable = false)
    private CarePet carePet;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AssignJob_detailID", referencedColumnName = "detailID", insertable = false, updatable = false)
    private AssignJob assignJob;

    // =================== Constructors ===================
    public Announcement() {}

    // =================== Getters & Setters ===================
    public Integer getAnnounceID() { return announceID; }
    public void setAnnounceID(Integer announceID) { this.announceID = announceID; }

    public String getAddressNo() { return addressNo; }
    public void setAddressNo(String addressNo) { this.addressNo = addressNo; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getSubdistrict() { return subdistrict; }
    public void setSubdistrict(String subdistrict) { this.subdistrict = subdistrict; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getPostdate() { return postdate; }
    public void setPostdate(LocalDateTime postdate) { this.postdate = postdate; }

    public LocalDate getStartdate() { return startdate; }
    public void setStartdate(LocalDate startdate) { this.startdate = startdate; }

    public LocalDate getEnddate() { return enddate; }
    public void setEnddate(LocalDate enddate) { this.enddate = enddate; }

    public String getCurrentweight() { return currentweight; }
    public void setCurrentweight(String currentweight) { this.currentweight = currentweight; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getOwnerID() { return ownerID; }
    public void setOwnerID(Integer ownerID) { this.ownerID = ownerID; }

    public Integer getPetID() { return petID; }
    public void setPetID(Integer petID) { this.petID = petID; }

    public Integer getCarepetID() { return carepetID; }
    public void setCarepetID(Integer carepetID) { this.carepetID = carepetID; }

    public Integer getAssignJobID() { return assignJobID; }
    public void setAssignJobID(Integer assignJobID) { this.assignJobID = assignJobID; }

    public PetOwner getOwner() { return owner; }
    public Pet getPet() { return pet; }
    public CarePet getCarePet() { return carePet; }
    public AssignJob getAssignJob() { return assignJob; }
}