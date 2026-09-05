package com.springboot.repository;
import com.springboot.model.AssignJob;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AssignJobRepository extends JpaRepository<AssignJob, Integer> {
	
}