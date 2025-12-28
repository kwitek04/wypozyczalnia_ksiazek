package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class Ksiazka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String stanFizyczny; // np. "Bardzo dobry", "Uszkodzona okładka"

    @Enumerated(EnumType.STRING)
    private StatusKsiazki status;

    @OneToOne
    @JoinColumn(name = "isbn_id") // Klucz obcy łączący fizyczny egzemplarz z opisem (ISBN)
    private DaneKsiazki daneKsiazki;

    @ManyToOne
    @JoinColumn(name = "poddziedzina_id")
    private Poddziedzina poddziedzina;

    public Ksiazka() {}

    public Ksiazka(String stanFizyczny, StatusKsiazki status, DaneKsiazki daneKsiazki) {
        this.stanFizyczny = stanFizyczny;
        this.status = status;
        this.daneKsiazki = daneKsiazki;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStanFizyczny() { return stanFizyczny; }
    public void setStanFizyczny(String stanFizyczny) { this.stanFizyczny = stanFizyczny; }

    public StatusKsiazki getStatus() { return status; }
    public void setStatus(StatusKsiazki status) { this.status = status; }

    public DaneKsiazki getDaneKsiazki() { return daneKsiazki; }
    public void setDaneKsiazki(DaneKsiazki daneKsiazki) { this.daneKsiazki = daneKsiazki; }

    public Poddziedzina getPoddziedzina() { return poddziedzina; }
    public void setPoddziedzina(Poddziedzina poddziedzina) { this.poddziedzina = poddziedzina; }
}