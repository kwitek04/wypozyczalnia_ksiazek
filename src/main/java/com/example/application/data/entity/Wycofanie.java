package com.example.application.data.entity;

import com.example.application.data.repository.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Wycofanie extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "ksiazka_id")
    @NotNull
    private Ksiazka ksiazka;

    @ManyToOne
    @JoinColumn(name = "pracownik_id")
    @NotNull
    private Pracownicy pracownik;

    private LocalDate dataWycofania;

    private String powod;

    public Wycofanie() {}

    public Wycofanie(Ksiazka ksiazka, Pracownicy pracownik, LocalDate dataWycofania, String powod) {
        this.ksiazka = ksiazka;
        this.pracownik = pracownik;
        this.dataWycofania = dataWycofania;
        this.powod = powod;
    }

    // Gettery i Settery
    public Ksiazka getKsiazka() { return ksiazka; }
    public void setKsiazka(Ksiazka ksiazka) { this.ksiazka = ksiazka; }

    public Pracownicy getPracownik() { return pracownik; }
    public void setPracownik(Pracownicy pracownik) { this.pracownik = pracownik; }

    public LocalDate getDataWycofania() { return dataWycofania; }
    public void setDataWycofania(LocalDate dataWycofania) { this.dataWycofania = dataWycofania; }

    public String getPowod() { return powod; }
    public void setPowod(String powod) { this.powod = powod; }
}