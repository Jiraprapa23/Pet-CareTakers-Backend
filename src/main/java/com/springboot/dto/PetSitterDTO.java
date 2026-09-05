package com.springboot.dto;

import java.time.LocalDate;

public class PetSitterDTO {

    // =================== ข้อมูลส่วนตัว ===================
    private String firstname;
    private String lastname;
    private String gender;
    private LocalDate birthdate;
    private String phoneNumber;
    private String email;
    private String password;
    private String sitterImage;

    // =================== ที่อยู่ ===================
    private String addressNo;
    private String street;
    private String subdistrict;
    private String district;
    private String province;
    private String zipcode;
    private Double latitude;
    private Double longitude;

    // =================== รายละเอียดการรับดูแล ===================
    private String petAllowType;
    private String acceptedPetSize;
    private String experienceYear;
    private String pricePerDay;
    private String isMorning;
    private String isAfternoon;
    private String isEvening;
    private String isNight;

    // =================== บริการเสริม ===================
    private String isFeedMedicine;
    private String isCleanService;
    private String isWalkService;

    // =================== รายละเอียดการเงิน ===================
    private String bankName;
    private String accountNo;
    private String accountName;
    private String qrCodeImage;

    // =================== Constructors ===================
    public PetSitterDTO() {

    }

    // =================== Getters & Setters ===================
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSitterImage() {
        return sitterImage;
    }

    public void setSitterImage(String sitterImage) {
        this.sitterImage = sitterImage;
    }

    public String getAddressNo() {
        return addressNo;
    }

    public void setAddressNo(String addressNo) {
        this.addressNo = addressNo;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getSubdistrict() {
        return subdistrict;
    }

    public void setSubdistrict(String subdistrict) {
        this.subdistrict = subdistrict;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPetAllowType() {
        return petAllowType;
    }

    public void setPetAllowType(String petAllowType) {
        this.petAllowType = petAllowType;
    }

    public String getAcceptedPetSize() {
        return acceptedPetSize;
    }

    public void setAcceptedPetSize(String acceptedPetSize) {
        this.acceptedPetSize = acceptedPetSize;
    }

    public String getExperienceYear() {
        return experienceYear;
    }

    public void setExperienceYear(String experienceYear) {
        this.experienceYear = experienceYear;
    }

    public String getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(String pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getIsMorning() {
        return isMorning;
    }

    public void setIsMorning(String isMorning) {
        this.isMorning = isMorning;
    }

    public String getIsAfternoon() {
        return isAfternoon;
    }

    public void setIsAfternoon(String isAfternoon) {
        this.isAfternoon = isAfternoon;
    }

    public String getIsEvening() {
        return isEvening;
    }

    public void setIsEvening(String isEvening) {
        this.isEvening = isEvening;
    }

    public String getIsNight() {
        return isNight;
    }

    public void setIsNight(String isNight) {
        this.isNight = isNight;
    }

    public String getIsFeedMedicine() {
        return isFeedMedicine;
    }

    public void setIsFeedMedicine(String isFeedMedicine) {
        this.isFeedMedicine = isFeedMedicine;
    }

    public String getIsCleanService() {
        return isCleanService;
    }

    public void setIsCleanService(String isCleanService) {
        this.isCleanService = isCleanService;
    }

    public String getIsWalkService() {
        return isWalkService;
    }

    public void setIsWalkService(String isWalkService) {
        this.isWalkService = isWalkService;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getQrCodeImage() {
        return qrCodeImage;
    }

    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }
}
