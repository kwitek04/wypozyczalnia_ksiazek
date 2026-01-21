package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class Ksiazka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StanFizyczny stanFizyczny = StanFizyczny.BARDZO_DOBRY;

    @Enumerated(EnumType.STRING)
    private StatusKsiazki status;

    @OneToOne
    @JoinColumn(name = "isbn_id")
    private DaneKsiazki daneKsiazki;

    @ManyToOne
    @JoinColumn(name = "poddziedzina_id")
    private Poddziedzina poddziedzina;

    private int licznikWypozyczen = 0;
    private boolean wymagaKontroli = false;
    private java.time.LocalDate dataOstatniejKontroli;

    public Ksiazka() {}

    public Ksiazka(StanFizyczny stanFizyczny, StatusKsiazki status, DaneKsiazki daneKsiazki) {
        this.stanFizyczny = stanFizyczny;
        this.status = status;
        this.daneKsiazki = daneKsiazki;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StanFizyczny getStanFizyczny() { return stanFizyczny; }
    public void setStanFizyczny(StanFizyczny stanFizyczny) { this.stanFizyczny = stanFizyczny; }

    public StatusKsiazki getStatus() { return status; }
    public void setStatus(StatusKsiazki status) { this.status = status; }

    public DaneKsiazki getDaneKsiazki() { return daneKsiazki; }
    public void setDaneKsiazki(DaneKsiazki daneKsiazki) { this.daneKsiazki = daneKsiazki; }

    public Poddziedzina getPoddziedzina() { return poddziedzina; }
    public void setPoddziedzina(Poddziedzina poddziedzina) { this.poddziedzina = poddziedzina; }

    public int getLicznikWypozyczen() { return licznikWypozyczen; }
    public void setLicznikWypozyczen(int licznikWypozyczen) { this.licznikWypozyczen = licznikWypozyczen; }

    public boolean isWymagaKontroli() { return wymagaKontroli; }
    public void setWymagaKontroli(boolean wymagaKontroli) { this.wymagaKontroli = wymagaKontroli; }

    public java.time.LocalDate getDataOstatniejKontroli() {
        return dataOstatniejKontroli;
    }

    public void setDataOstatniejKontroli(java.time.LocalDate dataOstatniejKontroli) {
        this.dataOstatniejKontroli = dataOstatniejKontroli;
    }
}