package com.springboot.controller;

import com.springboot.model.Pet;
import com.springboot.model.PetType;
import com.springboot.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetService petService;

    // =================== ดึงประเภทสัตว์เลี้ยงทั้งหมด ===================
    @GetMapping("/types")
    public ResponseEntity<List<PetType>> getAllPetTypes() {
        return ResponseEntity.ok(petService.getAllPetTypes());
    }

    // =================== เพิ่มสัตว์เลี้ยง Step 1 ===================
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addPet(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();

        Integer petID = petService.addPet(data);

        if (petID == -1) {
            response.put("message", "มีข้อมูลสัตว์เลี้ยงนี้อยู่ในระบบแล้ว");
            return ResponseEntity.badRequest().body(response);
        }
        if (petID == -2) {
            response.put("message", "ไม่พบประเภทสัตว์เลี้ยง");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "บันทึกข้อมูลสำเร็จ");
        response.put("petID", petID);
        return ResponseEntity.ok(response);
    }

    // =================== อัพโหลดรูป Step 2 ===================
    @PostMapping("/upload-image/{petID}")
    public ResponseEntity<Map<String, String>> uploadPetImage(
            @PathVariable Integer petID,
            @RequestParam("petImage") MultipartFile petImage) {

        Map<String, String> response = new HashMap<>();
        String result = petService.uploadPetImage(petID, petImage);

        if (!result.equals("อัพโหลดรูปสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "เพิ่มสัตว์เลี้ยงสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== แก้ไขสัตว์เลี้ยง ===================
    @PutMapping("/update/{petID}")
    public ResponseEntity<Map<String, String>> updatePet(
            @PathVariable Integer petID,
            @RequestBody Map<String, Object> data) {

        Map<String, String> response = new HashMap<>();
        String result = petService.updatePet(petID, data);

        if (!result.equals("แก้ไขข้อมูลสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "แก้ไขข้อมูลสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== อัพเดทรูปสัตว์เลี้ยง ===================
    @PutMapping("/update-image/{petID}")
    public ResponseEntity<Map<String, String>> updatePetImage(
            @PathVariable Integer petID,
            @RequestParam("petImage") MultipartFile petImage) {

        Map<String, String> response = new HashMap<>();
        String result = petService.updatePetImage(petID, petImage);

        if (!result.equals("อัพโหลดรูปสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "อัพเดทรูปสำเร็จ");
        return ResponseEntity.ok(response);
    }

    // =================== แจ้งการจากไป ===================
    @PutMapping("/mark-deceased/{petID}")
    public ResponseEntity<Map<String, String>> markDeceased(@PathVariable Integer petID) {
        Map<String, String> response = new HashMap<>();
        String result = petService.markDeceased(petID);

        if (!result.equals("เปลี่ยนสถานะสำเร็จ")) {
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "เปลี่ยนสถานะสัตว์เลี้ยงเรียบร้อยแล้ว");
        return ResponseEntity.ok(response);
    }

    // =================== ดึงรายการสัตว์เลี้ยง ===================
    @GetMapping("/my-pets/{ownerID}")
    public ResponseEntity<List<Pet>> getMyPets(@PathVariable Integer ownerID) {
        return ResponseEntity.ok(petService.getMyPets(ownerID));
    }

    // =================== ดึงรายละเอียด ===================
    @GetMapping("/detail/{petID}")
    public ResponseEntity<Pet> getPetDetail(@PathVariable Integer petID) {
        Pet pet = petService.getPetDetail(petID);
        if (pet == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(pet);
    }
}