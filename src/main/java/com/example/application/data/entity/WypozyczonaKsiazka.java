package com.example.application.data.entity;

import jakarta.persistence.*;

@Entity
public class WypozyczonaKsiazka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wypozyczenie_id")
    private Wypozyczenie wypozyczenie;

    @ManyToOne
    @JoinColumn(name = "ksiazka_id")
    private Ksiazka ksiazka;

    public WypozyczonaKsiazka() {}

    public WypozyczonaKsiazka(Wypozyczenie wypozyczenie, Ksiazka ksiazka) {
        this.wypozyczenie = wypozyczenie;
        this.ksiazka = ksiazka;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Wypozyczenie getWypozyczenie() { return wypozyczenie; }
    public void setWypozyczenie(Wypozyczenie wypozyczenie) { this.wypozyczenie = wypozyczenie; }
    public Ksiazka getKsiazka() { return ksiazka; }
    public void setKsiazka(Ksiazka ksiazka) { this.ksiazka = ksiazka; }
}