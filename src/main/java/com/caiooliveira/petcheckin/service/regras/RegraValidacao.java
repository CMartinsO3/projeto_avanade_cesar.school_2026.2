package com.caiooliveira.petcheckin.service.regras;

import java.util.List;

import com.caiooliveira.petcheckin.model.StatusValidacao;

/**
 * Contrato de toda regra de validação. Para criar uma regra nova (ex.: R6),
 * basta criar uma classe @Component que implemente esta interface:
 * o ValidacaoService passa a usá-la sozinho.
 */
public interface RegraValidacao {

    /** Código da regra, ex.: "R1". */
    String codigo();

    /** Descrição curta, exibida na lista de regras atendidas. */
    String descricao();

    /** Devolve as pendências encontradas. Lista vazia = regra atendida. */
    List<String> avaliar(ContextoValidacao contexto);

    /** Resultado da validação quando esta regra falha. A maioria das regras reprova. */
    default StatusValidacao statusSeFalhar() {
        return StatusValidacao.NAO_APTO;
    }
}
