package com.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "AssignJob")
public class AssignJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detailID")
    private Integer detailID;

    @Column(name = "workStatus", length = 50)
    private String workStatus;

    @Column(name = "slipImage", length = 255)
    private String slipImage;

    @Column(name = "isPaymentConfirmed", length = 50)
    private String isPaymentConfirmed;

    // =================== Constructors ===================
    public AssignJob() {}

    // =================== Getters & Setters ===================
    public Integer getDetailID() { return detailID; }
    public void setDetailID(Integer detailID) { this.detailID = detailID; }

    public String getWorkStatus() { return workStatus; }
    public void setWorkStatus(String workStatus) { this.workStatus = workStatus; }

    public String getSlipImage() { return slipImage; }
    public void setSlipImage(String slipImage) { this.slipImage = slipImage; }

    public String getIsPaymentConfirmed() { return isPaymentConfirmed; }
    public void setIsPaymentConfirmed(String isPaymentConfirmed) { this.isPaymentConfirmed = isPaymentConfirmed; }
}