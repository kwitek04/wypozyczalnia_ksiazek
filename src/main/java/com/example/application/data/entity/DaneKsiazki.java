package com.example.application.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Lob;
import jakarta.persistence.Column;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "autor_ksiazka",
            joinColumns = @JoinColumn(name = "ksiazka_id"),      // klucz do DaneKsiazki (ISBN)
            inverseJoinColumns = @JoinColumn(name = "autor_id") // klucz do Autor (id)
    )




    private Set<Autor> autorzy = new HashSet<>();
    public Set<Autor> getAutorzy() { return autorzy; }
    public void setAutorzy(Set<Autor> autorzy) { this.autorzy = autorzy; }

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
}