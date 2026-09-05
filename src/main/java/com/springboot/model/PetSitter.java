package com.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "PetSitter")
public class PetSitter {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sitterID")
    private Integer sitterID;

    // =================== ข้อมูลส่วนตัว ===================
    @Column(name = "firstname", nullable = false, length = 50)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 50)
    private String lastname;

    @Column(name = "gender", nullable = false, length = 25)
    private String gender;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "phoneNumber", nullable = false, length = 10)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password", nullable = false, length = 16)
    private String password;

    // =================== ที่อยู่ ===================
    @Column(name = "addressNo", nullable = false, length = 100)
    private String addressNo;

    @Column(name = "street", nullable = false, length = 100)
    private String street;

    @Column(name = "subdistrict", nullable = false, length = 50)
    private String subdistrict;

    @Column(name = "district", nullable = false, length = 50)
    private String district;

    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Column(name = "zipcode", nullable = false, length = 5)
    private String zipcode;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    // =================== รูปภาพ ===================
    @Column(name = "sitterImage", nullable = false, length = 255)
    private String sitterImage;

    @Column(name = "qrCodeImage", nullable = false, length = 255)
    private String qrCodeImage;

    // =================== รายละเอียดการรับดูแล ===================
    @Column(name = "petAllowType", nullable = false, length = 50)
    private String petAllowType;

    @Column(name = "acceptedPetSize", nullable = false, length = 50)
    private String acceptedPetSize;

    @Column(name = "experienceYear", nullable = false, length = 100)
    private String experienceYear;

    @Column(name = "pricePerDay", nullable = false, length = 5)
    private String pricePerDay;

    @Column(name = "isMorning", nullable = false, length = 50)
    private String isMorning;

    @Column(name = "isAfternoon", nullable = false, length = 50)
    private String isAfternoon;

    @Column(name = "isEvening", nullable = false, length = 50)
    private String isEvening;

    @Column(name = "isNight", nullable = false, length = 50)
    private String isNight;

    // บริการเสริม
    @Column(name = "isFeedMedicine", nullable = false, length = 50)
    private String isFeedMedicine;

    @Column(name = "isCleanService", nullable = false, length = 50)
    private String isCleanService;

    @Column(name = "isWalkService", nullable = false, length = 50)
    private String isWalkService;

    // รายละเอียดการเงิน
    @Column(name = "bankName", nullable = false, length = 50)
    private String bankName;

    @Column(name = "accountNo", nullable = false, length = 15)
    private String accountNo;

    @Column(name = "accountName", nullable = false, length = 100)
    private String accountName;

    // Role
    @Column(name = "role", nullable = false, length = 10)
    private String role = "SITTER";

    // =================== Constructors ===================
    public PetSitter() {

    }

    // =================== Getters & Setters ===================
    public Integer getSitterID() { return sitterID; }
    public void setSitterID(Integer sitterID) { this.sitterID = sitterID; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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

    public String getSitterImage() { return sitterImage; }
    public void setSitterImage(String sitterImage) { this.sitterImage = sitterImage; }

    public String getQrCodeImage() { return qrCodeImage; }
    public void setQrCodeImage(String qrCodeImage) { this.qrCodeImage = qrCodeImage; }

    public String getPetAllowType() { return petAllowType; }
    public void setPetAllowType(String petAllowType) { this.petAllowType = petAllowType; }

    public String getAcceptedPetSize() { return acceptedPetSize; }
    public void setAcceptedPetSize(String acceptedPetSize) { this.acceptedPetSize = acceptedPetSize; }

    public String getExperienceYear() { return experienceYear; }
    public void setExperienceYear(String experienceYear) { this.experienceYear = experienceYear; }

    public String getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(String pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getIsMorning() { return isMorning; }
    public void setIsMorning(String isMorning) { this.isMorning = isMorning; }

    public String getIsAfternoon() { return isAfternoon; }
    public void setIsAfternoon(String isAfternoon) { this.isAfternoon = isAfternoon; }

    public String getIsEvening() { return isEvening; }
    public void setIsEvening(String isEvening) { this.isEvening = isEvening; }

    public String getIsNight() { return isNight; }
    public void setIsNight(String isNight) { this.isNight = isNight; }

    public String getIsFeedMedicine() { return isFeedMedicine; }
    public void setIsFeedMedicine(String isFeedMedicine) { this.isFeedMedicine = isFeedMedicine; }

    public String getIsCleanService() { return isCleanService; }
    public void setIsCleanService(String isCleanService) { this.isCleanService = isCleanService; }

    public String getIsWalkService() { return isWalkService; }
    public void setIsWalkService(String isWalkService) { this.isWalkService = isWalkService; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}