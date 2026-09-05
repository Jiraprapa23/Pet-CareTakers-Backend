package com.springboot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.dto.PetSitterDTO;
import com.springboot.model.PetSitter;
import com.springboot.repository.PetSitterRepository;

@Service
public class PetSitterService {

    @Autowired
    private PetSitterRepository petSitterRepository;

    @Value("${file.upload-dir.sitters}")
    private String uploadDirSitters;

    @Value("${file.upload-dir.qrcodes}")
    private String uploadDirQrcodes;

    // =================== Register Step 1 ===================
    public Integer register(PetSitterDTO dto) {
        if (petSitterRepository.existsByEmail(dto.getEmail())) return -1;

        PetSitter sitter = new PetSitter();
        sitter.setFirstname(dto.getFirstname());
        sitter.setLastname(dto.getLastname());
        sitter.setGender(dto.getGender());
        sitter.setBirthdate(dto.getBirthdate());
        sitter.setPhoneNumber(dto.getPhoneNumber());
        sitter.setEmail(dto.getEmail());
        sitter.setPassword(dto.getPassword());
        sitter.setSitterImage("default.png");
        sitter.setAddressNo(dto.getAddressNo());
        sitter.setStreet(dto.getStreet() != null ? dto.getStreet() : "-");
        sitter.setSubdistrict(dto.getSubdistrict());
        sitter.setDistrict(dto.getDistrict());
        sitter.setProvince(dto.getProvince());
        sitter.setZipcode(dto.getZipcode());
        sitter.setLatitude(dto.getLatitude());
        sitter.setLongitude(dto.getLongitude());
        sitter.setPetAllowType(dto.getPetAllowType());
        sitter.setAcceptedPetSize(dto.getAcceptedPetSize());
        sitter.setExperienceYear(dto.getExperienceYear());
        sitter.setPricePerDay(dto.getPricePerDay());
        sitter.setIsMorning(dto.getIsMorning());
        sitter.setIsAfternoon(dto.getIsAfternoon());
        sitter.setIsEvening(dto.getIsEvening());
        sitter.setIsNight(dto.getIsNight());
        sitter.setIsFeedMedicine(dto.getIsFeedMedicine());
        sitter.setIsCleanService(dto.getIsCleanService());
        sitter.setIsWalkService(dto.getIsWalkService());
        sitter.setBankName(dto.getBankName());
        sitter.setAccountNo(dto.getAccountNo());
        sitter.setAccountName(dto.getAccountName());
        sitter.setQrCodeImage("default.png");
        sitter.setRole("SITTER");

        PetSitter saved = petSitterRepository.save(sitter);
        return saved.getSitterID();
    }

    // =================== Register Step 2 ===================
    public String uploadImages(Integer sitterID, MultipartFile sitterImageFile, MultipartFile qrCodeImageFile) {
        Optional<PetSitter> optional = petSitterRepository.findById(sitterID);
        if (optional.isEmpty()) return "ไม่พบผู้ใช้";

        String sitterImageName = saveFile(sitterImageFile, uploadDirSitters);
        if (sitterImageName == null) return "เกิดข้อผิดพลาดในการอัพโหลดรูปโปรไฟล์";

        String qrCodeImageName = saveFile(qrCodeImageFile, uploadDirQrcodes);
        if (qrCodeImageName == null) return "เกิดข้อผิดพลาดในการอัพโหลด QR Code";

        PetSitter sitter = optional.get();
        sitter.setSitterImage(sitterImageName);
        sitter.setQrCodeImage(qrCodeImageName);
        petSitterRepository.save(sitter);
        return "อัพโหลดรูปภาพสำเร็จ";
    }

    // =================== Update Sitter ===================
    public String updateSitter(Integer sitterID, PetSitterDTO dto) {
        Optional<PetSitter> optional = petSitterRepository.findById(sitterID);
        if (optional.isEmpty()) return "ไม่พบผู้ใช้";

        PetSitter sitter = optional.get();
        sitter.setFirstname(dto.getFirstname());
        sitter.setLastname(dto.getLastname());
        sitter.setGender(dto.getGender());
        sitter.setBirthdate(dto.getBirthdate());
        sitter.setPhoneNumber(dto.getPhoneNumber());
        sitter.setAddressNo(dto.getAddressNo());
        sitter.setStreet(dto.getStreet() != null ? dto.getStreet() : "-");
        sitter.setSubdistrict(dto.getSubdistrict());
        sitter.setDistrict(dto.getDistrict());
        sitter.setProvince(dto.getProvince());
        sitter.setZipcode(dto.getZipcode());
        sitter.setLatitude(dto.getLatitude());
        sitter.setLongitude(dto.getLongitude());
        sitter.setPetAllowType(dto.getPetAllowType());
        sitter.setAcceptedPetSize(dto.getAcceptedPetSize());
        sitter.setExperienceYear(dto.getExperienceYear());
        sitter.setPricePerDay(dto.getPricePerDay());
        sitter.setIsMorning(dto.getIsMorning());
        sitter.setIsAfternoon(dto.getIsAfternoon());
        sitter.setIsEvening(dto.getIsEvening());
        sitter.setIsNight(dto.getIsNight());
        sitter.setIsFeedMedicine(dto.getIsFeedMedicine());
        sitter.setIsCleanService(dto.getIsCleanService());
        sitter.setIsWalkService(dto.getIsWalkService());
        sitter.setBankName(dto.getBankName());
        sitter.setAccountNo(dto.getAccountNo());
        sitter.setAccountName(dto.getAccountName());

        petSitterRepository.save(sitter);
        return "แก้ไขข้อมูลสำเร็จ";
    }

    // =================== Update Sitter Images ===================
    public String updateImages(Integer sitterID, MultipartFile sitterImageFile, MultipartFile qrCodeImageFile) {
        Optional<PetSitter> optional = petSitterRepository.findById(sitterID);
        if (optional.isEmpty()) return "ไม่พบผู้ใช้";

        PetSitter sitter = optional.get();

        if (sitterImageFile != null && !sitterImageFile.isEmpty()) {
            String name = saveFile(sitterImageFile, uploadDirSitters);
            if (name != null) sitter.setSitterImage(name);
        }

        if (qrCodeImageFile != null && !qrCodeImageFile.isEmpty()) {
            String name = saveFile(qrCodeImageFile, uploadDirQrcodes);
            if (name != null) sitter.setQrCodeImage(name);
        }

        petSitterRepository.save(sitter);
        return "อัพโหลดรูปภาพสำเร็จ";
    }

    // =================== Get By ID ===================
    public PetSitter getByID(Integer sitterID) {
        return petSitterRepository.findById(sitterID).orElse(null);
    }

    // =================== Login ===================
    public PetSitter login(String email, String password) {
        Optional<PetSitter> optional = petSitterRepository.findByEmail(email);
        if (optional.isEmpty()) return null;
        PetSitter sitter = optional.get();
        if (!sitter.getPassword().equals(password)) return null;
        return sitter;
    }

    // =================== Save File (Running Number) ===================
    private String saveFile(MultipartFile file, String dirPath) {
        try {
            Path uploadPath = Paths.get(dirPath);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            long count = Files.list(uploadPath)
                .filter(p -> !Files.isDirectory(p))
                .count();
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