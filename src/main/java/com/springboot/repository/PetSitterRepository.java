package com.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.model.PetSitter;

@Repository
public interface PetSitterRepository extends JpaRepository<PetSitter, Integer> {

    // =================== ค้นหาด้วย Email ===================
    Optional<PetSitter> findByEmail(String email);

    // =================== เช็คว่า Email ซ้ำไหม ===================
    boolean existsByEmail(String email);
}