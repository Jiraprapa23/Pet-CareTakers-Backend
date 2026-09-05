package com.springboot.repository;
import com.springboot.model.ApplyJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ApplyJobRepository extends JpaRepository<ApplyJob, Integer> {
    List<ApplyJob> findByAnnounceID(Integer announceID);
    List<ApplyJob> findBySitterID(Integer sitterID);
    boolean existsByAnnounceIDAndSitterID(Integer announceID, Integer sitterID);
}