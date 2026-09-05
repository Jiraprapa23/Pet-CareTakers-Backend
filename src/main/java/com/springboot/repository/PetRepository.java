package com.springboot.repository;

import com.springboot.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Integer> {

    List<Pet> findByOwnerID(Integer ownerID);

    boolean existsByOwnerIDAndPetName(Integer ownerID, String petName);

    boolean existsByOwnerIDAndPetNameAndPetIDNot(Integer ownerID, String petName, Integer petID);
}