package com.caiooliveira.petcheckin.model;

import java.time.LocalDate;

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
@Table(name = "reservas")
public class Reserva
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(nullable = false)
    private LocalDate dataCheckin;

    @Column(nullable = false)
    private LocalDate dataCheckout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status;

    protected Reserva() {
    }

    public Reserva(Pet pet, LocalDate dataCheckin, LocalDate dataCheckout)
    {
        this.pet = pet;
        this.dataCheckin = dataCheckin;
        this.dataCheckout = dataCheckout;
        this.status = StatusReserva.AGUARDANDO_DOCUMENTOS;
    }

    public Long getId() { return id; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public LocalDate getDataCheckin() { return dataCheckin; }
    public void setDataCheckin(LocalDate dataCheckin) { this.dataCheckin = dataCheckin; }

    public LocalDate getDataCheckout() { return dataCheckout; }
    public void setDataCheckout(LocalDate dataCheckout) { this.dataCheckout = dataCheckout; }

    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }
}