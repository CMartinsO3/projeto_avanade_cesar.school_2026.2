package com.caiooliveira.petcheckin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caiooliveira.petcheckin.model.Validacao;

public interface ValidacaoRepository extends JpaRepository<Validacao, Long> {

    Optional<Validacao> findFirstByReservaIdOrderByIdDesc(Long reservaId);
}
