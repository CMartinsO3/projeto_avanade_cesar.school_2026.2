package com.caiooliveira.petcheckin.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    @Column(nullable = false)
    private String nomeOriginal;

    @Column(nullable = false)
    private String caminhoArquivo;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long tamanhoBytes;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDocumento status;

    @Column(columnDefinition = "TEXT")
    private String textoExtraido;

    private Double confiancaOcr;

    private Integer palavrasOcr;

    protected Documento() {
    }

    public Documento(Reserva reserva, TipoDocumento tipo, String nomeOriginal,
            String caminhoArquivo, String contentType, Long tamanhoBytes) {
        this.reserva = reserva;
        this.tipo = tipo;
        this.nomeOriginal = nomeOriginal;
        this.caminhoArquivo = caminhoArquivo;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
        this.dataEnvio = LocalDateTime.now();
        this.status = StatusDocumento.RECEBIDO;
    }

    public void registrarOcr(String texto, double confianca, int quantidadePalavras) {
        this.textoExtraido = texto;
        this.confiancaOcr = confianca;
        this.palavrasOcr = quantidadePalavras;
        this.status = StatusDocumento.PROCESSADO;
    }

    public void registrarFalhaProcessamento() {
        this.status = StatusDocumento.FALHA_PROCESSAMENTO;
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public StatusDocumento getStatus() {
        return status;
    }

    public String getTextoExtraido() {
        return textoExtraido;
    }

    public Double getConfiancaOcr() {
        return confiancaOcr;
    }

    public Integer getPalavrasOcr() {
        return palavrasOcr;
    }
}