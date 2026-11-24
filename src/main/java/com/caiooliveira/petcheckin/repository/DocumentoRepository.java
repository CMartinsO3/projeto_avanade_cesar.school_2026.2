package com.caiooliveira.petcheckin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caiooliveira.petcheckin.model.Documento;
import com.caiooliveira.petcheckin.model.StatusDocumento;
import com.caiooliveira.petcheckin.model.TipoDocumento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByReservaId(Long reservaId);

    // O Spring monta a consulta pelo nome do método: o documento mais recente (maior id)
    // da reserva, do tipo e status informados e que já tenha os dados extraídos.
    Optional<Documento> findFirstByReservaIdAndTipoAndStatusAndDadosExtraidosIsNotNullOrderByIdDesc(
            Long reservaId, TipoDocumento tipo, StatusDocumento status);
}
