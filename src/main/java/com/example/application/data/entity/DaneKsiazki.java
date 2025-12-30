package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
public class DaneKsiazki {

    @Id
    @NotEmpty
    @Size(min = 13, max = 13)
    private String isbn; // Klucz główny (ISBN-13)

    @NotEmpty
    @Size(max = 50)
    private String tytul;

    @NotEmpty
    @Size(max = 25)
    private String wydawnictwo;

    private Integer rokWydania;

    // --- LOB (ZDJĘCIE) ---
    @Lob
    @Column(length = 10000000)
    private byte[] okladka;

    // --- RELACJA AUTORZY ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "autor_ksiazka",
            // ZMIANA TUTAJ: Wracamy do "ksiazka_id", bo taką kolumnę masz w bazie danych
            joinColumns = @JoinColumn(name = "ksiazka_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Set<Autor> autorzy = new HashSet<>();

// W DaneKsiazki.java

    // ... inne pola ...

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tlumacz_ksiazka",
            joinColumns = @JoinColumn(name = "ksiazka_isbn"),
            inverseJoinColumns = @JoinColumn(name = "tlumacz_id")
    )
    private Set<Tlumacz> tlumacze = new HashSet<>();

    public Set<Tlumacz> getTlumacze() { return tlumacze; }
    public void setTlumacze(Set<Tlumacz> tlumacze) { this.tlumacze = tlumacze; }

    // Konstruktory
    public DaneKsiazki() {}

    public DaneKsiazki(String isbn, String tytul, String wydawnictwo, int rokWydania) {
        this.isbn = isbn;
        this.tytul = tytul;
        this.wydawnictwo = wydawnictwo;
        this.rokWydania = rokWydania;
    }

    // Gettery i Settery
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTytul() { return tytul; }
    public void setTytul(String tytul) { this.tytul = tytul; }

    public String getWydawnictwo() { return wydawnictwo; }
    public void setWydawnictwo(String wydawnictwo) { this.wydawnictwo = wydawnictwo; }

    public Integer getRokWydania() { return rokWydania; }
    public void setRokWydania(Integer rokWydania) { this.rokWydania = rokWydania; }

    public byte[] getOkladka() { return okladka; }
    public void setOkladka(byte[] okladka) { this.okladka = okladka; }

    public Set<Autor> getAutorzy() { return autorzy; }
    public void setAutorzy(Set<Autor> autorzy) { this.autorzy = autorzy; }
}