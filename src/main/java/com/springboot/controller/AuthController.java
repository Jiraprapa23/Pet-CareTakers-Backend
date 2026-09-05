package com.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.dto.PetOwnerDTO;
import com.springboot.dto.PetSitterDTO;
import com.springboot.model.PetOwner;
import com.springboot.model.PetSitter;
import com.springboot.service.PetOwnerService;
import com.springboot.service.PetSitterService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PetOwnerService petOwnerService;

    @Autowired
    private PetSitterService petSitterService;

    @Value("${file.upload-dir.owners}")
    private String uploadDirOwners;

    @Value("${file.upload-dir.sitters}")
    private String uploadDirSitters;

    @Value("${file.upload-dir.qrcodes}")
    private String uploadDirQrcodes;

    @Value("${file.upload-dir.pets}")
    private String uploadDirPets;

    @Value("${file.upload-dir.reports}")
    private String uploadDirReports;

    @Value("${file.upload-dir.slips}")
    private String uploadDirSlips;

    // =================== Register Owner Step 1 ===================
    @PostMapping("/register-owner")
    public ResponseEntity<Map<String, Object>> registerOwner(@RequestBody PetOwnerDTO dto) {
        Map<String, Object> response = new HashMap<>();
        Integer ownerID = petOwnerService.register(dto);
        if (ownerID == -1) {
            response.put("message", "อีเมลนี้ถูกลงทะเบียนไว้แล้ว");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "บันทึกข้อมูลสำเร็จ");
        response.put("ownerID", ownerID);
        return ResponseEntity.ok(response);
    }

    // =================== Register Owner Step 2 ===================
    @PostMapping("/upload-owner-image")
    public ResponseEntity<Map<String, String>> uploadOwnerImage(
            @RequestParam("ownerID") Integer ownerID,
            @RequestParam("profileImage") MultipartFile profileImage) {
        Map<String, String> response = new HashMap<>();
        String result = petOwnerService.uploadProfileImage(ownerID, profileImage);
        if (!result.equals("อัพโหลดรูปภาพสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "ลงทะเบียนสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== Update Owner ===================
    @PutMapping("/update-owner/{ownerID}")
    public ResponseEntity<Map<String, String>> updateOwner(
            @PathVariable Integer ownerID,
            @RequestBody PetOwnerDTO dto) {
        Map<String, String> response = new HashMap<>();
        String result = petOwnerService.updateOwner(ownerID, dto);
        if (!result.equals("แก้ไขข้อมูลสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "แก้ไขข้อมูลสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== Update Owner Image ===================
    @PutMapping("/update-owner-image/{ownerID}")
    public ResponseEntity<Map<String, String>> updateOwnerImage(
            @PathVariable Integer ownerID,
            @RequestParam("profileImage") MultipartFile profileImage) {
        Map<String, String> response = new HashMap<>();
        String result = petOwnerService.updateProfileImage(ownerID, profileImage);
        if (!result.equals("อัพโหลดรูปภาพสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "อัพเดทรูปภาพสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== Register Sitter Step 1 ===================
    @PostMapping("/register-sitter")
    public ResponseEntity<Map<String, Object>> registerSitter(@RequestBody PetSitterDTO dto) {
        Map<String, Object> response = new HashMap<>();
        Integer sitterID = petSitterService.register(dto);
        if (sitterID == -1) {
            response.put("message", "อีเมลนี้ถูกลงทะเบียนไว้แล้ว");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "บันทึกข้อมูลสำเร็จ");
        response.put("sitterID", sitterID);
        return ResponseEntity.ok(response);
    }

    // =================== Register Sitter Step 2 ===================
    @PostMapping("/upload-sitter-image")
    public ResponseEntity<Map<String, String>> uploadSitterImage(
            @RequestParam("sitterID") Integer sitterID,
            @RequestParam("sitterImage") MultipartFile sitterImage,
            @RequestParam("qrCodeImage") MultipartFile qrCodeImage) {
        Map<String, String> response = new HashMap<>();
        String result = petSitterService.uploadImages(sitterID, sitterImage, qrCodeImage);
        if (!result.equals("อัพโหลดรูปภาพสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "ลงทะเบียนสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== Update Sitter ===================
    @PutMapping("/update-sitter/{sitterID}")
    public ResponseEntity<Map<String, String>> updateSitter(
            @PathVariable Integer sitterID,
            @RequestBody PetSitterDTO dto) {
        Map<String, String> response = new HashMap<>();
        String result = petSitterService.updateSitter(sitterID, dto);
        if (!result.equals("แก้ไขข้อมูลสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "แก้ไขข้อมูลสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== Update Sitter Images ===================
    @PutMapping("/update-sitter-image/{sitterID}")
    public ResponseEntity<Map<String, String>> updateSitterImage(
            @PathVariable Integer sitterID,
            @RequestParam(value = "sitterImage", required = false) MultipartFile sitterImage,
            @RequestParam(value = "qrCodeImage", required = false) MultipartFile qrCodeImage) {
        Map<String, String> response = new HashMap<>();
        String result = petSitterService.updateImages(sitterID, sitterImage, qrCodeImage);
        if (!result.equals("อัพโหลดรูปภาพสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "อัพเดทรูปภาพสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== Login Owner ===================
    @PostMapping("/login-owner")
    public ResponseEntity<Map<String, Object>> loginOwner(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        PetOwner owner = petOwnerService.login(request.get("email"), request.get("password"));
        if (owner == null) {
            response.put("message", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "เข้าสู่ระบบสำเร็จ");
        response.put("ownerID", owner.getOwnetID());
        response.put("firstname", owner.getFirstname());
        response.put("lastname", owner.getLastname());
        response.put("email", owner.getEmail());
        response.put("profileImage", owner.getProfileImage());
        response.put("addressNo", owner.getAddressNo());
        response.put("street", owner.getStreet());
        response.put("subdistrict", owner.getSubdistrict());
        response.put("district", owner.getDistrict());
        response.put("province", owner.getProvince());
        response.put("zipcode", owner.getZipcode());
        response.put("latitude", owner.getLatitude());
        response.put("longitude", owner.getLongitude());
        response.put("role", owner.getRole());
        return ResponseEntity.ok(response);
    }

    // =================== Login Sitter ===================
    @PostMapping("/login-sitter")
    public ResponseEntity<Map<String, Object>> loginSitter(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        PetSitter sitter = petSitterService.login(request.get("email"), request.get("password"));
        if (sitter == null) {
            response.put("message", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "เข้าสู่ระบบสำเร็จ");
        response.put("sitterID", sitter.getSitterID());
        response.put("firstname", sitter.getFirstname());
        response.put("lastname", sitter.getLastname());
        response.put("email", sitter.getEmail());
        response.put("sitterImage", sitter.getSitterImage());
        response.put("role", sitter.getRole());
        return ResponseEntity.ok(response);
    }

    // =================== Get Owner Profile ===================
    @GetMapping("/profile-owner/{ownerID}")
    public ResponseEntity<Map<String, Object>> getOwnerProfile(@PathVariable Integer ownerID) {
        Map<String, Object> response = new HashMap<>();
        PetOwner owner = petOwnerService.getByID(ownerID);
        if (owner == null) {
            response.put("message", "ไม่พบข้อมูล");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("ownerID", owner.getOwnetID());
        response.put("firstname", owner.getFirstname());
        response.put("lastname", owner.getLastname());
        response.put("gender", owner.getGender());
        response.put("birtdate", owner.getBirtdate());
        response.put("phoneNumber", owner.getPhoneNumber());
        response.put("email", owner.getEmail());
        response.put("profileImage", owner.getProfileImage());
        response.put("addressNo", owner.getAddressNo());
        response.put("street", owner.getStreet());
        response.put("subdistrict", owner.getSubdistrict());
        response.put("district", owner.getDistrict());
        response.put("province", owner.getProvince());
        response.put("zipcode", owner.getZipcode());
        response.put("latitude", owner.getLatitude());
        response.put("longitude", owner.getLongitude());
        response.put("role", owner.getRole());
        return ResponseEntity.ok(response);
    }

    // =================== Get Sitter Profile ===================
    @GetMapping("/profile-sitter/{sitterID}")
    public ResponseEntity<Map<String, Object>> getSitterProfile(@PathVariable Integer sitterID) {
        Map<String, Object> response = new HashMap<>();
        PetSitter sitter = petSitterService.getByID(sitterID);
        if (sitter == null) {
            response.put("message", "ไม่พบข้อมูล");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("sitterID", sitter.getSitterID());
        response.put("firstname", sitter.getFirstname());
        response.put("lastname", sitter.getLastname());
        response.put("gender", sitter.getGender());
        response.put("birthdate", sitter.getBirthdate());
        response.put("phoneNumber", sitter.getPhoneNumber());
        response.put("email", sitter.getEmail());
        response.put("sitterImage", sitter.getSitterImage());
        response.put("addressNo", sitter.getAddressNo());
        response.put("street", sitter.getStreet());
        response.put("subdistrict", sitter.getSubdistrict());
        response.put("district", sitter.getDistrict());
        response.put("province", sitter.getProvince());
        response.put("zipcode", sitter.getZipcode());
        response.put("latitude", sitter.getLatitude());
        response.put("longitude", sitter.getLongitude());
        response.put("petAllowType", sitter.getPetAllowType());
        response.put("acceptedPetSize", sitter.getAcceptedPetSize());
        response.put("experienceYear", sitter.getExperienceYear());
        response.put("pricePerDay", sitter.getPricePerDay());
        response.put("isMorning", sitter.getIsMorning());
        response.put("isAfternoon", sitter.getIsAfternoon());
        response.put("isEvening", sitter.getIsEvening());
        response.put("isNight", sitter.getIsNight());
        response.put("isFeedMedicine", sitter.getIsFeedMedicine());
        response.put("isCleanService", sitter.getIsCleanService());
        response.put("isWalkService", sitter.getIsWalkService());
        response.put("bankName", sitter.getBankName());
        response.put("accountNo", sitter.getAccountNo());
        response.put("accountName", sitter.getAccountName());
        response.put("qrCodeImage", sitter.getQrCodeImage());
        response.put("role", sitter.getRole());
        return ResponseEntity.ok(response);
    }

    // =================== Serve Images (ค้นหาจากทุก subfolder) ===================
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            String[] dirs = { uploadDirOwners, uploadDirSitters, uploadDirQrcodes,
                              uploadDirPets, uploadDirReports, uploadDirSlips };
            for (String dir : dirs) {
                Path filePath = Paths.get(dir).resolve(filename);
                if (java.nio.file.Files.exists(filePath)) {
                    Resource resource = new UrlResource(filePath.toUri());
                    String contentType = filename.toLowerCase().endsWith(".png") ? "image/png"
                            : filename.toLowerCase().endsWith(".mp4") ? "video/mp4"
                            : "image/jpeg";
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                            .body(resource);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}