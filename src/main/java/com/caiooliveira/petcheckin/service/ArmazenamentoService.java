package com.caiooliveira.petcheckin.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.caiooliveira.petcheckin.exception.RegraNegocioException;

@Service
public class ArmazenamentoService {

    private static final Map<String, String> EXTENSOES_PERMITIDAS = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "application/pdf", ".pdf"
    );

    private final Path pastaUploads;

    public ArmazenamentoService(@Value("${app.upload.pasta}") String pasta) {
        this.pastaUploads = Path.of(pasta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(pastaUploads);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de uploads", e);
        }
    }

    public Path salvar(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new RegraNegocioException("O arquivo enviado está vazio");
        }

        String extensao = EXTENSOES_PERMITIDAS.get(arquivo.getContentType());
        if (extensao == null) {
            throw new RegraNegocioException("Formato não suportado. Envie um arquivo PDF, PNG ou JPG");
        }

        Path destino = pastaUploads.resolve(UUID.randomUUID() + extensao);
        try {
            arquivo.transferTo(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível salvar o arquivo", e);
        }
        return destino;
    }
}