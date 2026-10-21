package com.caiooliveira.petcheckin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caiooliveira.petcheckin.model.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {
}