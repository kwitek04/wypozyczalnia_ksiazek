package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
public class Poddziedzina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(max = 50)
    private String nazwa;

    @ManyToOne
    @JoinColumn(name = "dziedzina_id")
    private Dziedzina dziedzina;

    public Poddziedzina() {}

    public Poddziedzina(String nazwa, Dziedzina dziedzina) {
        this.nazwa = nazwa;
        this.dziedzina = dziedzina;
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public Dziedzina getDziedzina() { return dziedzina; }
    public void setDziedzina(Dziedzina dziedzina) { this.dziedzina = dziedzina; }
}