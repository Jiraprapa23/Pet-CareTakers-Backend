package com.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CarePet")
public class CarePet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carepetID")
    private Integer carepetID;

    // =================== ช่วงเวลาดูแล ===================
    @Column(name = "careMorning")
    private Boolean careMorning = false;

    @Column(name = "careAfternoon")
    private Boolean careAfternoon = false;

    @Column(name = "careEvening")
    private Boolean careEvening = false;

    @Column(name = "careNight")
    private Boolean careNight = false;

    // =================== การให้อาหาร ===================
    @Column(name = "feedMorning")
    private Boolean feedMorning = false;

    @Column(name = "feedAfternoon")
    private Boolean feedAfternoon = false;

    @Column(name = "feedEvening")
    private Boolean feedEvening = false;

    @Column(name = "feedNight")
    private Boolean feedNight = false;

    @Column(name = "feedMorningDetail", length = 255)
    private String feedMorningDetail;

    @Column(name = "feedAfternoonDetail", length = 255)
    private String feedAfternoonDetail;

    @Column(name = "feedEveningDetail", length = 255)
    private String feedEveningDetail;

    @Column(name = "feedNightDetail", length = 255)
    private String feedNightDetail;

    // =================== อาหาร ===================
    @Column(name = "isFoodPrepared")
    private Boolean isFoodPrepared = false;

    @Column(name = "foodAmount", length = 255)
    private String foodAmount;

    // =================== ยา ===================
    @Column(name = "medicineDetail", length = 255)
    private String medicineDetail;

    // =================== บริการเสริม ===================
    @Column(name = "isCleanService")
    private Boolean isCleanService = false;

    @Column(name = "isWalkService")
    private Boolean isWalkService = false;

    // =================== Constructors ===================
    public CarePet() {}

    // =================== Getters & Setters ===================
    public Integer getCarepetID() { return carepetID; }
    public void setCarepetID(Integer carepetID) { this.carepetID = carepetID; }

    public Boolean getCareMorning() { return careMorning; }
    public void setCareMorning(Boolean careMorning) { this.careMorning = careMorning; }

    public Boolean getCareAfternoon() { return careAfternoon; }
    public void setCareAfternoon(Boolean careAfternoon) { this.careAfternoon = careAfternoon; }

    public Boolean getCareEvening() { return careEvening; }
    public void setCareEvening(Boolean careEvening) { this.careEvening = careEvening; }

    public Boolean getCareNight() { return careNight; }
    public void setCareNight(Boolean careNight) { this.careNight = careNight; }

    public Boolean getFeedMorning() { return feedMorning; }
    public void setFeedMorning(Boolean feedMorning) { this.feedMorning = feedMorning; }

    public Boolean getFeedAfternoon() { return feedAfternoon; }
    public void setFeedAfternoon(Boolean feedAfternoon) { this.feedAfternoon = feedAfternoon; }

    public Boolean getFeedEvening() { return feedEvening; }
    public void setFeedEvening(Boolean feedEvening) { this.feedEvening = feedEvening; }

    public Boolean getFeedNight() { return feedNight; }
    public void setFeedNight(Boolean feedNight) { this.feedNight = feedNight; }

    public String getFeedMorningDetail() { return feedMorningDetail; }
    public void setFeedMorningDetail(String feedMorningDetail) { this.feedMorningDetail = feedMorningDetail; }

    public String getFeedAfternoonDetail() { return feedAfternoonDetail; }
    public void setFeedAfternoonDetail(String feedAfternoonDetail) { this.feedAfternoonDetail = feedAfternoonDetail; }

    public String getFeedEveningDetail() { return feedEveningDetail; }
    public void setFeedEveningDetail(String feedEveningDetail) { this.feedEveningDetail = feedEveningDetail; }

    public String getFeedNightDetail() { return feedNightDetail; }
    public void setFeedNightDetail(String feedNightDetail) { this.feedNightDetail = feedNightDetail; }

    public Boolean getIsFoodPrepared() { return isFoodPrepared; }
    public void setIsFoodPrepared(Boolean isFoodPrepared) { this.isFoodPrepared = isFoodPrepared; }

    public String getFoodAmount() { return foodAmount; }
    public void setFoodAmount(String foodAmount) { this.foodAmount = foodAmount; }

    public String getMedicineDetail() { return medicineDetail; }
    public void setMedicineDetail(String medicineDetail) { this.medicineDetail = medicineDetail; }

    public Boolean getIsCleanService() { return isCleanService; }
    public void setIsCleanService(Boolean isCleanService) { this.isCleanService = isCleanService; }

    public Boolean getIsWalkService() { return isWalkService; }
    public void setIsWalkService(Boolean isWalkService) { this.isWalkService = isWalkService; }
}