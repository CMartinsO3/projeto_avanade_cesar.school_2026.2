package com.caiooliveira.petcheckin.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.caiooliveira.petcheckin.dto.ResultadoOcr;
import com.caiooliveira.petcheckin.exception.ProcessamentoDocumentoException;

@Service
public class OcrService {

    private static final int DPI_PDF = 300;
    private static final int MAXIMO_PAGINAS_PDF = 5;

    private final String comando;
    private final String idioma;
    private final long timeoutSegundos;

    public OcrService(@Value("${app.ocr.comando}") String comando,
                      @Value("${app.ocr.idioma}") String idioma,
                      @Value("${app.ocr.timeout-segundos}") long timeoutSegundos) {
        this.comando = comando;
        this.idioma = idioma;
        this.timeoutSegundos = timeoutSegundos;
    }

    public ResultadoOcr extrairTexto(Path arquivo, String contentType) {
        if (!"application/pdf".equals(contentType)) {
            return executarTesseract(arquivo);
        }

        List<Path> paginas = converterPdfEmImagens(arquivo);
        try {
            List<ResultadoOcr> resultados = paginas.stream()
                    .map(this::executarTesseract)
                    .toList();
            return juntar(resultados);
        } finally {
            paginas.forEach(this::apagarTemporario);
        }
    }

    private List<Path> converterPdfEmImagens(Path pdf) {
        List<Path> imagens = new ArrayList<>();
        try (PDDocument documento = Loader.loadPDF(pdf.toFile())) {
            PDFRenderer renderizador = new PDFRenderer(documento);
            int totalPaginas = Math.min(documento.getNumberOfPages(), MAXIMO_PAGINAS_PDF);

            for (int pagina = 0; pagina < totalPaginas; pagina++) {
                BufferedImage imagem = renderizador.renderImageWithDPI(pagina, DPI_PDF, ImageType.GRAY);
                Path arquivoImagem = Files.createTempFile("ocr-pagina-", ".png");
                ImageIO.write(imagem, "png", arquivoImagem.toFile());
                imagens.add(arquivoImagem);
            }
            return imagens;
        } catch (IOException e) {
            imagens.forEach(this::apagarTemporario);
            throw new ProcessamentoDocumentoException("Não foi possível ler o PDF", e);
        }
    }

    private ResultadoOcr executarTesseract(Path imagem) {
        Path saidaTsv = null;
        try {
            saidaTsv = Files.createTempFile("ocr-", ".tsv");

            ProcessBuilder processo = new ProcessBuilder(
                    comando, imagem.toString(), "stdout", "-l", idioma, "tsv");
            processo.redirectOutput(saidaTsv.toFile());
            processo.redirectError(ProcessBuilder.Redirect.DISCARD);

            Process execucao = processo.start();
            boolean terminou = execucao.waitFor(timeoutSegundos, TimeUnit.SECONDS);
            if (!terminou) {
                execucao.destroyForcibly();
                throw new ProcessamentoDocumentoException(
                        "O OCR demorou mais que " + timeoutSegundos + " segundos");
            }
            if (execucao.exitValue() != 0) {
                throw new ProcessamentoDocumentoException(
                        "O Tesseract terminou com erro (código " + execucao.exitValue() + ")");
            }

            return interpretarTsv(Files.readAllLines(saidaTsv, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ProcessamentoDocumentoException(
                    "Não foi possível executar o Tesseract: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessamentoDocumentoException("O OCR foi interrompido", e);
        } finally {
            apagarTemporario(saidaTsv);
        }
    }

    private ResultadoOcr interpretarTsv(List<String> linhasTsv) {
        StringBuilder texto = new StringBuilder();
        double somaConfianca = 0;
        int quantidadePalavras = 0;
        String linhaAnterior = null;

        for (String linhaTsv : linhasTsv) {
            String[] colunas = linhaTsv.split("\t");
            if (colunas.length < 12 || !colunas[0].equals("5")) {
                continue;
            }
            String palavra = colunas[11].trim();
            if (palavra.isEmpty()) {
                continue;
            }

            String linhaAtual = colunas[2] + "-" + colunas[3] + "-" + colunas[4];
            if (linhaAnterior != null) {
                texto.append(linhaAtual.equals(linhaAnterior) ? " " : "\n");
            }
            texto.append(palavra);
            linhaAnterior = linhaAtual;

            somaConfianca += Double.parseDouble(colunas[10]);
            quantidadePalavras++;
        }

        double confiancaMedia = quantidadePalavras == 0 ? 0 : somaConfianca / quantidadePalavras;
        return new ResultadoOcr(texto.toString(), arredondar(confiancaMedia), quantidadePalavras);
    }

    private ResultadoOcr juntar(List<ResultadoOcr> resultados) {
        StringBuilder texto = new StringBuilder();
        double somaPonderada = 0;
        int totalPalavras = 0;

        for (ResultadoOcr resultado : resultados) {
            if (!texto.isEmpty()) {
                texto.append("\n\n");
            }
            texto.append(resultado.texto());
            somaPonderada += resultado.confianca() * resultado.quantidadePalavras();
            totalPalavras += resultado.quantidadePalavras();
        }

        double confiancaMedia = totalPalavras == 0 ? 0 : somaPonderada / totalPalavras;
        return new ResultadoOcr(texto.toString(), arredondar(confiancaMedia), totalPalavras);
    }

    private double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    private void apagarTemporario(Path arquivo) {
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            // arquivo temporário: se não der para apagar, o sistema operacional limpa depois
        }
    }
}