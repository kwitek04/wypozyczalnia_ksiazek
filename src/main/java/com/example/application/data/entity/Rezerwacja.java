package com.example.application.data.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Rezerwacja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uzytkownik_id")
    private Uzytkownicy uzytkownik;

    private LocalDate dataRezerwacji;
    private LocalDate waznaDo;

    private boolean zrealizowana = false;

    @Enumerated(EnumType.STRING)
    private StatusRezerwacji status;

    @OneToMany(mappedBy = "rezerwacja", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ZarezerwowanaKsiazka> zarezerwowaneKsiazki = new ArrayList<>();

    public Rezerwacja() {}

    public Rezerwacja(Uzytkownicy uzytkownik) {
        this.uzytkownik = uzytkownik;
        this.dataRezerwacji = LocalDate.now();
        this.waznaDo = LocalDate.now().plusDays(3);
        this.status = StatusRezerwacji.AKTYWNA;// Domyślnie 3 dni na odbiór
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Uzytkownicy getUzytkownik() { return uzytkownik; }
    public void setUzytkownik(Uzytkownicy uzytkownik) { this.uzytkownik = uzytkownik; }
    public LocalDate getDataRezerwacji() { return dataRezerwacji; }
    public void setDataRezerwacji(LocalDate dataRezerwacji) { this.dataRezerwacji = dataRezerwacji; }
    public LocalDate getWaznaDo() { return waznaDo; }
    public void setWaznaDo(LocalDate waznaDo) { this.waznaDo = waznaDo; }
    public boolean isZrealizowana() { return zrealizowana; }
    public void setZrealizowana(boolean zrealizowana) { this.zrealizowana = zrealizowana; }
    public List<ZarezerwowanaKsiazka> getZarezerwowaneKsiazki() { return zarezerwowaneKsiazki; }
    public void setZarezerwowaneKsiazki(List<ZarezerwowanaKsiazka> zarezerwowaneKsiazki) { this.zarezerwowaneKsiazki = zarezerwowaneKsiazki; }
    public StatusRezerwacji getStatus() { return status; }
    public void setStatus(StatusRezerwacji status) { this.status = status; }
}