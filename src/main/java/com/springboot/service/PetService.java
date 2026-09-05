package com.springboot.service;

import com.springboot.model.Pet;
import com.springboot.model.PetType;
import com.springboot.repository.PetRepository;
import com.springboot.repository.PetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetTypeRepository petTypeRepository;

    @Value("${file.upload-dir.pets}")
    private String uploadDirPets;

    // =================== เพิ่มสัตว์เลี้ยง Step 1 ===================
    public Integer addPet(Map<String, Object> data) {

        Integer ownerID = Integer.parseInt(data.get("ownerID").toString());
        String petName = data.get("petName").toString().trim();

        // เช็คชื่อซ้ำ
        if (petRepository.existsByOwnerIDAndPetName(ownerID, petName)) {
            return -1;
        }

        // ดึง PetType จาก DB
        Object petTypeIDObj = data.get("petTypeID");
        if (petTypeIDObj == null) return -2;
        Integer petTypeID = Integer.parseInt(petTypeIDObj.toString());
        Optional<PetType> petTypeOpt = petTypeRepository.findById(petTypeID);
        if (petTypeOpt.isEmpty()) return -2;

        Pet pet = new Pet();
        pet.setOwnerID(ownerID);
        pet.setPetName(petName);
        pet.setPetType(petTypeOpt.get());
        pet.setBreed(getString(data, "breed"));
        pet.setGender(data.get("gender").toString());
        pet.setIsDeceased(false);

        String birthDateStr = getString(data, "birthDate");
        if (!birthDateStr.isEmpty()) {
            pet.setBirthDate(LocalDate.parse(birthDateStr));
        }

        pet.setPetImage("default.png");

        // วัคซีน
        String vacRabies = getString(data, "vacRabiesDate");
        pet.setVacRabiesDate(vacRabies.isEmpty() ? null : LocalDate.parse(vacRabies));

        String vacDhppi = getString(data, "vacDhppiDate");
        pet.setVacDhppiDate(vacDhppi.isEmpty() ? null : LocalDate.parse(vacDhppi));

        String vacFvrcp = getString(data, "vacFvrcpDate");
        pet.setVacFvrcpDate(vacFvrcp.isEmpty() ? null : LocalDate.parse(vacFvrcp));

        // โรคประจำตัว
        boolean hasDisease = getBool(data, "hasCongenitalDisease");
        pet.setHasCongenitalDisease(hasDisease ? "มี" : "ไม่มี");
        pet.setCongenitalDiseaseDetail(hasDisease ? getString(data, "congenitalDiseaseDetail") : "");
        pet.setEmergencySymptoms(hasDisease ? getString(data, "emergencySymptoms") : "");
        pet.setEmergencyContact(hasDisease ? getString(data, "emergencyContact") : "");

        // พฤติกรรม
        pet.setBehaviorStressAlone(getBool(data, "behaviorStressAlone"));
        pet.setBehaviorFriendly(getBool(data, "behaviorFriendly"));
        pet.setBehaviorFearStranger(getBool(data, "behaviorFearStranger"));
        pet.setBehaviorFearLoudSound(getBool(data, "behaviorFearLoudSound"));
        pet.setBehaviorBarkLoud(getBool(data, "behaviorBarkLoud"));
        pet.setBehaviorDislikeTouch(getBool(data, "behaviorDislikeTouch"));
        pet.setBehaviorBiteScrath(getBool(data, "behaviorBiteScrath"));
        pet.setBehaviorEscapeExpert(getBool(data, "behaviorEscapeExpert"));
        pet.setBehaviorHighEnergy(getBool(data, "behaviorHighEnergy"));
        pet.setBehaviorJumpOnPeople(getBool(data, "behaviorJumpOnPeople"));
        pet.setBehaviorHardControl(getBool(data, "behaviorHardControl"));

        Pet saved = petRepository.save(pet);
        return saved.getPetID();
    }

    // =================== อัพโหลดรูป Step 2 ===================
    public String uploadPetImage(Integer petID, MultipartFile file) {
        Optional<Pet> optional = petRepository.findById(petID);
        if (optional.isEmpty()) return "ไม่พบสัตว์เลี้ยง";

        String fileName = saveFile(file, uploadDirPets);
        if (fileName == null) return "เกิดข้อผิดพลาดในการอัพโหลดรูป";

        Pet pet = optional.get();
        pet.setPetImage(fileName);
        petRepository.save(pet);
        return "อัพโหลดรูปสำเร็จ";
    }

    // =================== แก้ไขสัตว์เลี้ยง ===================
    public String updatePet(Integer petID, Map<String, Object> data) {
        Optional<Pet> optional = petRepository.findById(petID);
        if (optional.isEmpty()) return "ไม่พบสัตว์เลี้ยง";

        Pet pet = optional.get();
        String petName = data.get("petName").toString().trim();

        // เช็คชื่อซ้ำ (ยกเว้นตัวเอง)
        if (petRepository.existsByOwnerIDAndPetNameAndPetIDNot(pet.getOwnerID(), petName, petID)) {
            return "มีข้อมูลสัตว์เลี้ยงนี้อยู่ในระบบแล้ว";
        }

        // ดึง PetType
        Object petTypeIDObj = data.get("petTypeID");
        if (petTypeIDObj == null) return "ไม่พบประเภทสัตว์เลี้ยง";
        Integer petTypeID = Integer.parseInt(petTypeIDObj.toString());
        Optional<PetType> petTypeOpt = petTypeRepository.findById(petTypeID);
        if (petTypeOpt.isEmpty()) return "ไม่พบประเภทสัตว์เลี้ยง";

        pet.setPetName(petName);
        pet.setPetType(petTypeOpt.get());
        pet.setBreed(getString(data, "breed"));
        pet.setGender(data.get("gender").toString());

        String birthDateStr = getString(data, "birthDate");
        if (!birthDateStr.isEmpty()) {
            pet.setBirthDate(LocalDate.parse(birthDateStr));
        }

        // วัคซีน
        String vacRabies = getString(data, "vacRabiesDate");
        pet.setVacRabiesDate(vacRabies.isEmpty() ? null : LocalDate.parse(vacRabies));

        String vacDhppi = getString(data, "vacDhppiDate");
        pet.setVacDhppiDate(vacDhppi.isEmpty() ? null : LocalDate.parse(vacDhppi));

        String vacFvrcp = getString(data, "vacFvrcpDate");
        pet.setVacFvrcpDate(vacFvrcp.isEmpty() ? null : LocalDate.parse(vacFvrcp));

        // โรคประจำตัว
        boolean hasDisease = getBool(data, "hasCongenitalDisease");
        pet.setHasCongenitalDisease(hasDisease ? "มี" : "ไม่มี");
        pet.setCongenitalDiseaseDetail(hasDisease ? getString(data, "congenitalDiseaseDetail") : "");
        pet.setEmergencySymptoms(hasDisease ? getString(data, "emergencySymptoms") : "");
        pet.setEmergencyContact(hasDisease ? getString(data, "emergencyContact") : "");

        // พฤติกรรม
        pet.setBehaviorStressAlone(getBool(data, "behaviorStressAlone"));
        pet.setBehaviorFriendly(getBool(data, "behaviorFriendly"));
        pet.setBehaviorFearStranger(getBool(data, "behaviorFearStranger"));
        pet.setBehaviorFearLoudSound(getBool(data, "behaviorFearLoudSound"));
        pet.setBehaviorBarkLoud(getBool(data, "behaviorBarkLoud"));
        pet.setBehaviorDislikeTouch(getBool(data, "behaviorDislikeTouch"));
        pet.setBehaviorBiteScrath(getBool(data, "behaviorBiteScrath"));
        pet.setBehaviorEscapeExpert(getBool(data, "behaviorEscapeExpert"));
        pet.setBehaviorHighEnergy(getBool(data, "behaviorHighEnergy"));
        pet.setBehaviorJumpOnPeople(getBool(data, "behaviorJumpOnPeople"));
        pet.setBehaviorHardControl(getBool(data, "behaviorHardControl"));

        petRepository.save(pet);
        return "แก้ไขข้อมูลสำเร็จ";
    }

    // =================== อัพเดทรูปสัตว์เลี้ยง ===================
    public String updatePetImage(Integer petID, MultipartFile file) {
        Optional<Pet> optional = petRepository.findById(petID);
        if (optional.isEmpty()) return "ไม่พบสัตว์เลี้ยง";

        String fileName = saveFile(file, uploadDirPets);
        if (fileName == null) return "เกิดข้อผิดพลาดในการอัพโหลดรูป";

        Pet pet = optional.get();
        pet.setPetImage(fileName);
        petRepository.save(pet);
        return "อัพโหลดรูปสำเร็จ";
    }

    // =================== แจ้งการจากไป ===================
    public String markDeceased(Integer petID) {
        Optional<Pet> optional = petRepository.findById(petID);
        if (optional.isEmpty()) return "ไม่พบสัตว์เลี้ยง";

        Pet pet = optional.get();
        pet.setIsDeceased(true);
        petRepository.save(pet);
        return "เปลี่ยนสถานะสำเร็จ";
    }

    // =================== ดึงรายการสัตว์เลี้ยง ===================
    public List<Pet> getMyPets(Integer ownerID) {
        return petRepository.findByOwnerID(ownerID);
    }

    // =================== ดึงรายละเอียด ===================
    public Pet getPetDetail(Integer petID) {
        return petRepository.findById(petID).orElse(null);
    }

    // =================== ดึงประเภทสัตว์เลี้ยงทั้งหมด ===================
    public List<PetType> getAllPetTypes() {
        return petTypeRepository.findAll();
    }

    // =================== Helper ===================
    private String getString(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val == null ? "" : val.toString().trim();
    }

    private Boolean getBool(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null && Boolean.parseBoolean(val.toString());
    }

    // =================== บันทึกไฟล์ (Running Number) ===================
    private String saveFile(MultipartFile file, String dirPath) {
        try {
            Path uploadPath = Paths.get(dirPath);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            long count = Files.list(uploadPath).filter(p -> !Files.isDirectory(p)).count();
            long nextNum = count + 1;

            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : ".png";

            String fileName = String.format("%03d%s", nextNum, ext);
            while (Files.exists(uploadPath.resolve(fileName))) {
                nextNum++;
                fileName = String.format("%03d%s", nextNum, ext);
            }

            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}