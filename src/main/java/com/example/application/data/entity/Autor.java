package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(max = 15)
    private String imie;

    @NotEmpty
    @Size(max = 25)
    private String nazwisko;

    @ManyToMany(mappedBy = "autorzy")
    private Set<DaneKsiazki> ksiazki = new HashSet<>();

    public Autor() {}

    public Autor(String imie, String nazwisko) {
        this.imie = imie;
        this.nazwisko = nazwisko;
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public Set<DaneKsiazki> getKsiazki() { return ksiazki; }
    public void setKsiazki(Set<DaneKsiazki> ksiazki) { this.ksiazki = ksiazki; }
}