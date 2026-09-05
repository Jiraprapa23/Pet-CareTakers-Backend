package com.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PET_TYPE")
public class PetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "petTypeID")
    private Integer petTypeID;

    @Column(name = "petTypeName", nullable = false, length = 50)
    private String petTypeName;

    // =================== Constructors ===================
    public PetType() {}

    public PetType(Integer petTypeID, String petTypeName) {
        this.petTypeID = petTypeID;
        this.petTypeName = petTypeName;
    }

    // =================== Getters & Setters ===================
    public Integer getPetTypeID() { 
    	return petTypeID; 
    }
    
    public void setPetTypeID(Integer petTypeID) { 
    	this.petTypeID = petTypeID; 
    }

    public String getPetTypeName() { 
    	return petTypeName; 
    }
    
    public void setPetTypeName(String petTypeName) { 
    	this.petTypeName = petTypeName; 
    }
}