package com.springboot.repository;

import com.springboot.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIDAndUserRoleOrderByCreatedAtDesc(Integer userID, String userRole);
    long countByUserIDAndUserRoleAndIsRead(Integer userID, String userRole, Boolean isRead);
}