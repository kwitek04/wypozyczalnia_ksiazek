package com.example.application.data.entity;

import jakarta.persistence.*;

@Entity
public class ZarezerwowanaKsiazka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rezerwacja_id")
    private Rezerwacja rezerwacja;

    @ManyToOne
    @JoinColumn(name = "ksiazka_id")
    private Ksiazka ksiazka;

    public ZarezerwowanaKsiazka() {}

    public ZarezerwowanaKsiazka(Rezerwacja rezerwacja, Ksiazka ksiazka) {
        this.rezerwacja = rezerwacja;
        this.ksiazka = ksiazka;
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Rezerwacja getRezerwacja() { return rezerwacja; }
    public void setRezerwacja(Rezerwacja rezerwacja) { this.rezerwacja = rezerwacja; }
    public Ksiazka getKsiazka() { return ksiazka; }
    public void setKsiazka(Ksiazka ksiazka) { this.ksiazka = ksiazka; }
}