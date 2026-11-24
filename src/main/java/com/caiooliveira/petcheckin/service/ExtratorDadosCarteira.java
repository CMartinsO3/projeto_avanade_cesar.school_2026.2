package com.caiooliveira.petcheckin.service;

import com.caiooliveira.petcheckin.dto.DadosCarteira;

/**
 * Contrato de quem sabe transformar o texto do OCR em dados estruturados.
 * Hoje há duas implementações: Gemini (IA) e Regex. Trocar de IA = criar outra implementação.
 */
public interface ExtratorDadosCarteira {

    DadosCarteira extrair(String texto);
}
