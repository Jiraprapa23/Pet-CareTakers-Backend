package com.springboot.repository;
import com.springboot.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    List<Announcement> findByOwnerIDOrderByPostdateDesc(Integer ownerID);
}