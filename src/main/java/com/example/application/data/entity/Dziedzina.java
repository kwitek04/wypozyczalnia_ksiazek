package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Dziedzina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(max = 50)
    private String nazwa;

    // Mapowanie relacji - jedna dziedzina ma wiele poddziedzin
    @OneToMany(mappedBy = "dziedzina", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Poddziedzina> poddziedziny = new ArrayList<>();

    public Dziedzina() {}

    public Dziedzina(String nazwa) {
        this.nazwa = nazwa;
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public List<Poddziedzina> getPoddziedziny() { return poddziedziny; }
    public void setPoddziedziny(List<Poddziedzina> poddziedziny) { this.poddziedziny = poddziedziny; }
}