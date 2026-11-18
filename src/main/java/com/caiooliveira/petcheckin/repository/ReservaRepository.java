package com.caiooliveira.petcheckin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caiooliveira.petcheckin.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
}