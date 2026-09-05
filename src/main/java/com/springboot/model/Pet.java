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

@Entity
@Table(name = "Pet")
public class Pet {

    // =================== ID ===================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "petID")
    private Integer petID;

    // =================== ข้อมูลส่วนตัว ===================
    @Column(name = "petName", nullable = false, length = 50)
    private String petName;

    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "birthDate")
    private LocalDate birthDate;

    // =================== รูปภาพ ===================
    @Column(name = "petImage", length = 255)
    private String petImage;

    // =================== รายละเอียด ===================
    @Column(name = "breed", length = 50)
    private String breed;

    @Column(name = "isDeceased")
    private Boolean isDeceased = false;

    // วัคซีน
    @Column(name = "vacRabiesDate")
    private LocalDate vacRabiesDate;

    @Column(name = "vacDhppiDate")
    private LocalDate vacDhppiDate;

    @Column(name = "vacFvrcpDate")
    private LocalDate vacFvrcpDate;

    // โรคประจำตัว
    @Column(name = "hasCongenitalDisease", length = 10)
    private String hasCongenitalDisease;

    @Column(name = "congenitalDiseaseDetail", length = 255)
    private String congenitalDiseaseDetail;

    @Column(name = "emergencySymptoms", length = 255)
    private String emergencySymptoms;

    @Column(name = "emergencyContact", length = 255)
    private String emergencyContact;

    // พฤติกรรม - ปฏิสัมพันธ์กับคน
    @Column(name = "behaviorStressAlone")
    private Boolean behaviorStressAlone = false;

    @Column(name = "behaviorFriendly")
    private Boolean behaviorFriendly = false;

    @Column(name = "behaviorFearStranger")
    private Boolean behaviorFearStranger = false;

    // พฤติกรรม - เสียงและการตอบสนอง
    @Column(name = "behaviorFearLoudSound")
    private Boolean behaviorFearLoudSound = false;

    @Column(name = "behaviorBarkLoud")
    private Boolean behaviorBarkLoud = false;

    // พฤติกรรม - เสี่ยงต่อการบาดเจ็บ
    @Column(name = "behaviorDislikeTouch")
    private Boolean behaviorDislikeTouch = false;

    @Column(name = "behaviorBiteScrath")
    private Boolean behaviorBiteScrath = false;

    @Column(name = "behaviorEscapeExpert")
    private Boolean behaviorEscapeExpert = false;

    @Column(name = "behaviorHighEnergy")
    private Boolean behaviorHighEnergy = false;

    @Column(name = "behaviorJumpOnPeople")
    private Boolean behaviorJumpOnPeople = false;

    @Column(name = "behaviorHardControl")
    private Boolean behaviorHardControl = false;

    // =================== FK (ท้ายสุด) ===================
    @Column(name = "ownerID", nullable = false)
    private Integer ownerID;

    // Phase 5: relationship แบบ read-only เพื่อใช้อ่านข้อมูลแทน findById
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerID", referencedColumnName = "ownetID", insertable = false, updatable = false)
    private PetOwner owner;

    // FK ไปยังตาราง PET_TYPE (ของเดิม เป็น read-write, EAGER ไม่ใช่ proxy เลย ไม่ต้อง JsonIgnore)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "petTypeID", nullable = false)
    private PetType petType;

    // =================== Constructors ===================
    public Pet() {

    }

    // =================== Getters & Setters ===================
    public Integer getPetID() {
    	return petID;
    }

    public void setPetID(Integer petID) { this.petID = petID; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPetImage() { return petImage; }
    public void setPetImage(String petImage) { this.petImage = petImage; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Boolean getIsDeceased() { return isDeceased; }
    public void setIsDeceased(Boolean isDeceased) { this.isDeceased = isDeceased; }

    public LocalDate getVacRabiesDate() { return vacRabiesDate; }
    public void setVacRabiesDate(LocalDate vacRabiesDate) { this.vacRabiesDate = vacRabiesDate; }

    public LocalDate getVacDhppiDate() { return vacDhppiDate; }
    public void setVacDhppiDate(LocalDate vacDhppiDate) { this.vacDhppiDate = vacDhppiDate; }

    public LocalDate getVacFvrcpDate() { return vacFvrcpDate; }
    public void setVacFvrcpDate(LocalDate vacFvrcpDate) { this.vacFvrcpDate = vacFvrcpDate; }

    public String getHasCongenitalDisease() { return hasCongenitalDisease; }
    public void setHasCongenitalDisease(String hasCongenitalDisease) { this.hasCongenitalDisease = hasCongenitalDisease; }

    public String getCongenitalDiseaseDetail() { return congenitalDiseaseDetail; }
    public void setCongenitalDiseaseDetail(String congenitalDiseaseDetail) { this.congenitalDiseaseDetail = congenitalDiseaseDetail; }

    public String getEmergencySymptoms() { return emergencySymptoms; }
    public void setEmergencySymptoms(String emergencySymptoms) { this.emergencySymptoms = emergencySymptoms; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public Boolean getBehaviorStressAlone() { return behaviorStressAlone; }
    public void setBehaviorStressAlone(Boolean v) { this.behaviorStressAlone = v; }

    public Boolean getBehaviorFriendly() { return behaviorFriendly; }
    public void setBehaviorFriendly(Boolean v) { this.behaviorFriendly = v; }

    public Boolean getBehaviorFearStranger() { return behaviorFearStranger; }
    public void setBehaviorFearStranger(Boolean v) { this.behaviorFearStranger = v; }

    public Boolean getBehaviorFearLoudSound() { return behaviorFearLoudSound; }
    public void setBehaviorFearLoudSound(Boolean v) { this.behaviorFearLoudSound = v; }

    public Boolean getBehaviorBarkLoud() { return behaviorBarkLoud; }
    public void setBehaviorBarkLoud(Boolean v) { this.behaviorBarkLoud = v; }

    public Boolean getBehaviorDislikeTouch() { return behaviorDislikeTouch; }
    public void setBehaviorDislikeTouch(Boolean v) { this.behaviorDislikeTouch = v; }

    public Boolean getBehaviorBiteScrath() { return behaviorBiteScrath; }
    public void setBehaviorBiteScrath(Boolean v) { this.behaviorBiteScrath = v; }

    public Boolean getBehaviorEscapeExpert() { return behaviorEscapeExpert; }
    public void setBehaviorEscapeExpert(Boolean v) { this.behaviorEscapeExpert = v; }

    public Boolean getBehaviorHighEnergy() { return behaviorHighEnergy; }
    public void setBehaviorHighEnergy(Boolean v) { this.behaviorHighEnergy = v; }

    public Boolean getBehaviorJumpOnPeople() { return behaviorJumpOnPeople; }
    public void setBehaviorJumpOnPeople(Boolean v) { this.behaviorJumpOnPeople = v; }

    public Boolean getBehaviorHardControl() { return behaviorHardControl; }
    public void setBehaviorHardControl(Boolean v) { this.behaviorHardControl = v; }

    public Integer getOwnerID() { return ownerID; }
    public void setOwnerID(Integer ownerID) { this.ownerID = ownerID; }

    public PetOwner getOwner() { return owner; }

    public PetType getPetType() { return petType; }
    public void setPetType(PetType petType) { this.petType = petType; }
}