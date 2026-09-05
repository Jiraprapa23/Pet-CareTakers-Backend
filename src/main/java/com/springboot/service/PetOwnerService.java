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

import com.springboot.dto.PetOwnerDTO;
import com.springboot.model.PetOwner;
import com.springboot.repository.PetOwnerRepository;

@Service
public class PetOwnerService {

    @Autowired
    private PetOwnerRepository petOwnerRepository;

    @Value("${file.upload-dir.owners}")
    private String uploadDirOwners;

    // =================== Register Step 1 ===================
    public Integer register(PetOwnerDTO dto) {
        if (petOwnerRepository.existsByEmail(dto.getEmail())) return -1;

        PetOwner owner = new PetOwner();
        owner.setFirstname(dto.getFirstname());
        owner.setLastname(dto.getLastname());
        owner.setGender(dto.getGender());
        owner.setBirtdate(dto.getBirtdate());
        owner.setPhoneNumber(dto.getPhoneNumber());
        owner.setEmail(dto.getEmail());
        owner.setPassword(dto.getPassword());
        owner.setProfileImage("default.png");
        owner.setAddressNo(dto.getAddressNo());
        owner.setStreet(dto.getStreet() != null ? dto.getStreet() : "-");
        owner.setSubdistrict(dto.getSubdistrict());
        owner.setDistrict(dto.getDistrict());
        owner.setProvince(dto.getProvince());
        owner.setZipcode(dto.getZipcode());
        owner.setLatitude(dto.getLatitude());
        owner.setLongitude(dto.getLongitude());
        owner.setRole("OWNER");

        PetOwner saved = petOwnerRepository.save(owner);
        return saved.getOwnetID();
    }

    // =================== Register Step 2 ===================
    public String uploadProfileImage(Integer ownerID, MultipartFile file) {
        Optional<PetOwner> optional = petOwnerRepository.findById(ownerID);
        if (optional.isEmpty()) return "ไม่พบผู้ใช้";

        String fileName = saveFile(file, uploadDirOwners);
        if (fileName == null) return "เกิดข้อผิดพลาดในการอัพโหลดรูปภาพ";

        PetOwner owner = optional.get();
        owner.setProfileImage(fileName);
        petOwnerRepository.save(owner);
        return "อัพโหลดรูปภาพสำเร็จ";
    }

    // =================== Update Owner ===================
    public String updateOwner(Integer ownerID, PetOwnerDTO dto) {
        Optional<PetOwner> optional = petOwnerRepository.findById(ownerID);
        if (optional.isEmpty()) return "ไม่พบผู้ใช้";

        PetOwner owner = optional.get();
        owner.setFirstname(dto.getFirstname());
        owner.setLastname(dto.getLastname());
        owner.setGender(dto.getGender());
        owner.setBirtdate(dto.getBirtdate());
        owner.setPhoneNumber(dto.getPhoneNumber());
        owner.setAddressNo(dto.getAddressNo());
        owner.setStreet(dto.getStreet() != null ? dto.getStreet() : "-");
        owner.setSubdistrict(dto.getSubdistrict());
        owner.setDistrict(dto.getDistrict());
        owner.setProvince(dto.getProvince());
        owner.setZipcode(dto.getZipcode());
        owner.setLatitude(dto.getLatitude());
        owner.setLongitude(dto.getLongitude());

        petOwnerRepository.save(owner);
        return "แก้ไขข้อมูลสำเร็จ";
    }

    // =================== Update Owner Image ===================
    public String updateProfileImage(Integer ownerID, MultipartFile file) {
        Optional<PetOwner> optional = petOwnerRepository.findById(ownerID);
        if (optional.isEmpty()) return "ไม่พบผู้ใช้";

        String fileName = saveFile(file, uploadDirOwners);
        if (fileName == null) return "เกิดข้อผิดพลาดในการอัพโหลดรูปภาพ";

        PetOwner owner = optional.get();
        owner.setProfileImage(fileName);
        petOwnerRepository.save(owner);
        return "อัพโหลดรูปภาพสำเร็จ";
    }

    // =================== Get By ID ===================
    public PetOwner getByID(Integer ownerID) {
        return petOwnerRepository.findById(ownerID).orElse(null);
    }

    // =================== Login ===================
    public PetOwner login(String email, String password) {
        Optional<PetOwner> optional = petOwnerRepository.findByEmail(email);
        if (optional.isEmpty()) return null;
        PetOwner owner = optional.get();
        if (!owner.getPassword().equals(password)) return null;
        return owner;
    }

    // =================== Save File (Running Number) ===================
    private String saveFile(MultipartFile file, String dirPath) {
        try {
            Path uploadPath = Paths.get(dirPath);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            // หา running number ถัดไป
            long count = Files.list(uploadPath)
                .filter(p -> !Files.isDirectory(p))
                .count();
            long nextNum = count + 1;

            // ดึงนามสกุลไฟล์
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : ".png";

            String fileName = String.format("%03d%s", nextNum, ext);

            // ถ้าชื่อซ้ำให้เพิ่มต่อ
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