package com.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.model.PetOwner;

@Repository
public interface PetOwnerRepository extends JpaRepository<PetOwner, Integer> {

    // =================== ค้นหาด้วย Email ===================
    Optional<PetOwner> findByEmail(String email);

    // =================== เช็คว่า Email ซ้ำไหม ===================
    boolean existsByEmail(String email);
}