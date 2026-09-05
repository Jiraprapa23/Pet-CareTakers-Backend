package com.springboot.controller;

import com.springboot.model.Notification;
import com.springboot.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired private NotificationRepository notificationRepository;

    // =================== ดึงการแจ้งเตือน ===================
    @GetMapping("/{userID}/{userRole}")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(
            @PathVariable Integer userID,
            @PathVariable String userRole) {
        List<Notification> list = notificationRepository
            .findByUserIDAndUserRoleOrderByCreatedAtDesc(userID, userRole);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification n : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("notifID", n.getNotifID());
            map.put("message", n.getMessage());
            map.put("isRead", n.getIsRead());
            map.put("announceID", n.getAnnounceID());
            map.put("createdAt", n.getCreatedAt());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // =================== นับที่ยังไม่อ่าน ===================
    @GetMapping("/unread-count/{userID}/{userRole}")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @PathVariable Integer userID,
            @PathVariable String userRole) {
        long count = notificationRepository
            .countByUserIDAndUserRoleAndIsRead(userID, userRole, false);
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // =================== อ่านแล้ว (ทีละอัน) ===================
    @PutMapping("/read/{notifID}")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Integer notifID) {
        Map<String, String> response = new HashMap<>();
        notificationRepository.findById(notifID).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
        response.put("message", "อ่านแล้ว");
        return ResponseEntity.ok(response);
    }

    // =================== อ่านทั้งหมด ===================
    @PutMapping("/read-all/{userID}/{userRole}")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @PathVariable Integer userID,
            @PathVariable String userRole) {
        List<Notification> list = notificationRepository
            .findByUserIDAndUserRoleOrderByCreatedAtDesc(userID, userRole);
        for (Notification n : list) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        }
        Map<String, String> response = new HashMap<>();
        response.put("message", "อ่านทั้งหมดแล้ว");
        return ResponseEntity.ok(response);
    }

    // =================== Helper: บันทึก notification ===================
    public void saveNotif(Integer userID, String userRole, String message, Integer announceID) {
        Notification n = new Notification();
        n.setUserID(userID);
        n.setUserRole(userRole);
        n.setMessage(message);
        n.setAnnounceID(announceID);
        notificationRepository.save(n);
    }
}