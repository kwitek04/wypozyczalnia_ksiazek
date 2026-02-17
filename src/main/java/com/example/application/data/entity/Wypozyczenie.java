package com.example.application.data.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Wypozyczenie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownik uzytkownik;

    private LocalDate dataWypozyczenia;
    private LocalDate terminZwrotu;
    private LocalDate dataOddania;

    private Double kara = 0.0;

    private boolean przedluzone = false;

    @OneToMany(mappedBy = "wypozyczenie", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<WypozyczonaKsiazka> wypozyczoneKsiazki = new ArrayList<>();

    public Wypozyczenie() {}

    public Wypozyczenie(Uzytkownik uzytkownik, LocalDate dataWypozyczenia, LocalDate terminZwrotu) {
        this.uzytkownik = uzytkownik;
        this.dataWypozyczenia = dataWypozyczenia;
        this.terminZwrotu = terminZwrotu;
    }

    private boolean zwrotZgloszony = false;

    private boolean naliczonoKareZaZaginiecie = false;

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Uzytkownik getUzytkownik() { return uzytkownik; }
    public void setUzytkownik(Uzytkownik uzytkownik) { this.uzytkownik = uzytkownik; }
    public LocalDate getDataWypozyczenia() { return dataWypozyczenia; }
    public void setDataWypozyczenia(LocalDate dataWypozyczenia) { this.dataWypozyczenia = dataWypozyczenia; }
    public LocalDate getTerminZwrotu() { return terminZwrotu; }
    public void setTerminZwrotu(LocalDate terminZwrotu) { this.terminZwrotu = terminZwrotu; }
    public LocalDate getDataOddania() { return dataOddania; }
    public void setDataOddania(LocalDate dataOddania) { this.dataOddania = dataOddania; }
    public List<WypozyczonaKsiazka> getWypozyczoneKsiazki() { return wypozyczoneKsiazki; }
    public void setWypozyczoneKsiazki(List<WypozyczonaKsiazka> wypozyczoneKsiazki) { this.wypozyczoneKsiazki = wypozyczoneKsiazki; }
    public boolean isZwrotZgloszony() { return zwrotZgloszony; }
    public void setZwrotZgloszony(boolean zwrotZgloszony) { this.zwrotZgloszony = zwrotZgloszony; }
    public boolean isPrzedluzone() { return przedluzone; }
    public void setPrzedluzone(boolean przedluzone) { this.przedluzone = przedluzone; }
    public Double getKara() {
        return kara != null ? kara : 0.0;
    }

    public void setKara(Double kara) {
        this.kara = kara;
    }

    public boolean isNaliczonoKareZaZaginiecie() {
        return naliczonoKareZaZaginiecie;
    }

    public void setNaliczonoKareZaZaginiecie(boolean naliczonoKareZaZaginiecie) {
        this.naliczonoKareZaZaginiecie = naliczonoKareZaZaginiecie;
    }
}