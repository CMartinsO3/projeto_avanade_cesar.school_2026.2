package com.caiooliveira.petcheckin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caiooliveira.petcheckin.model.Documento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByReservaId(Long reservaId);
}