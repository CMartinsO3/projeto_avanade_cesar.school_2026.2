package com.caiooliveira.petcheckin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.caiooliveira.petcheckin.dto.DadosCarteira;
import com.caiooliveira.petcheckin.dto.ResultadoExtracao;
import com.caiooliveira.petcheckin.model.MetodoExtracao;

@Service
public class ExtracaoService {

    private static final Logger log = LoggerFactory.getLogger(ExtracaoService.class);

    private final GeminiExtratorService geminiExtrator;
    private final RegexExtratorService regexExtrator;

    public ExtracaoService(GeminiExtratorService geminiExtrator, RegexExtratorService regexExtrator) {
        this.geminiExtrator = geminiExtrator;
        this.regexExtrator = regexExtrator;
    }

    public ResultadoExtracao extrair(String texto) {
        if (geminiExtrator.estaConfigurado()) {
            try {
                DadosCarteira dados = geminiExtrator.extrair(texto);
                return new ResultadoExtracao(dados, MetodoExtracao.IA);
            } catch (RuntimeException e) {
                log.warn("Falha na extração com IA, usando Regex: {}", e.getMessage());
            }
        }
        return new ResultadoExtracao(regexExtrator.extrair(texto), MetodoExtracao.REGEX);
    }
}
