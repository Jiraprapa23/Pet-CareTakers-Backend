package com.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "PetOwner")
public class PetOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ownetID")
    private Integer ownetID;

    @Column(name = "firstname", nullable = false, length = 50)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 50)
    private String lastname;

    @Column(name = "gender", nullable = false, length = 25)
    private String gender;

    @Column(name = "birtdate", nullable = false)
    private LocalDate birtdate;

    @Column(name = "phoneNumber", nullable = false, length = 10)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password", nullable = false, length = 16)
    private String password;

    @Column(name = "profileImage", nullable = false, length = 255)
    private String profileImage;

    @Column(name = "addressNo", nullable = false, length = 100)
    private String addressNo;

    @Column(name = "subdistrict", nullable = false, length = 50)
    private String subdistrict;

    @Column(name = "district", nullable = false, length = 50)
    private String district;

    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Column(name = "zipcode", nullable = false, length = 5)
    private String zipcode;

    @Column(name = "street", length = 100)
    private String street;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "role", nullable = false, length = 10)
    private String role = "OWNER";

    // =================== Constructors ===================
    public PetOwner() {
    	
    }

    // =================== Getters & Setters ===================
    public Integer getOwnetID() { return ownetID; }
    public void setOwnetID(Integer ownetID) { this.ownetID = ownetID; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirtdate() { return birtdate; }
    public void setBirtdate(LocalDate birtdate) { this.birtdate = birtdate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getAddressNo() { return addressNo; }
    public void setAddressNo(String addressNo) { this.addressNo = addressNo; }

    public String getSubdistrict() { return subdistrict; }
    public void setSubdistrict(String subdistrict) { this.subdistrict = subdistrict; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
