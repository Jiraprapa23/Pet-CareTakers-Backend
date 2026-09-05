package com.springboot.repository;
import com.springboot.model.CarePet;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CarePetRepository extends JpaRepository<CarePet, Integer> {
	
}