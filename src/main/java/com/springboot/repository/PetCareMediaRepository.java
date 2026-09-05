package com.springboot.repository;

import com.springboot.model.PetCareMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PetCareMediaRepository extends JpaRepository<PetCareMedia, Integer> {
    List<PetCareMedia> findByDetailID(Integer detailID);
    boolean existsByDetailIDAndTimecare(Integer detailID, String timecare);
    boolean existsByDetailIDAndTimecareAndCareDate(Integer detailID, String timecare, LocalDate careDate);
}