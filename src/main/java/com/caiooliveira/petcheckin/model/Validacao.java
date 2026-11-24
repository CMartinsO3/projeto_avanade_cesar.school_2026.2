package com.caiooliveira.petcheckin.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "validacoes")
public class Validacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusValidacao status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "validacao_regras_atendidas", joinColumns = @JoinColumn(name = "validacao_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "regra", nullable = false)
    private List<String> regrasAtendidas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "validacao_pendencias", joinColumns = @JoinColumn(name = "validacao_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "pendencia", nullable = false, length = 500)
    private List<String> pendencias = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Validacao() {
    }

    public Validacao(Reserva reserva, Documento documento, StatusValidacao status,
                     List<String> regrasAtendidas, List<String> pendencias, LocalDateTime dataHora) {
        this.reserva = reserva;
        this.documento = documento;
        this.status = status;
        this.regrasAtendidas = new ArrayList<>(regrasAtendidas);
        this.pendencias = new ArrayList<>(pendencias);
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Documento getDocumento() {
        return documento;
    }

    public StatusValidacao getStatus() {
        return status;
    }

    public List<String> getRegrasAtendidas() {
        return List.copyOf(regrasAtendidas);
    }

    public List<String> getPendencias() {
        return List.copyOf(pendencias);
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
