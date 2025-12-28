package com.example.application.data.entity;

import com.example.application.data.repository.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Entity
public class Pracownicy extends AbstractEntity {

    @NotEmpty
    @Size(max = 15, message = "Imię może mieć maksymalnie 15 znaków")
    private String imie = "";

    @NotEmpty
    @Size(max = 25, message = "Nazwisko może mieć maksymalnie 25 znaków")
    private String nazwisko = "";

    @Email
    @NotEmpty
    @Size(max = 50, message = "Email może mieć maksymalnie 50 znaków")
    private String email = "";

    @NotEmpty
    @Pattern(regexp = "\\d+", message = "Numer telefonu może zawierać tylko cyfry")
    @Size(min = 9, max = 15, message = "Numer telefonu musi mieć od 9 do 15 cyfr")
    private String nrTelefonu = "";

    @NotNull
    @ManyToOne
    private Rola rola;

    @NotEmpty(message = "Hasło nie może być puste")
    @Size(min = 6, message = "Hasło musi mieć minimum 6 znaków")
    private String password;

    @Override
    public String toString() {
        return imie + " " + nazwisko;
    }

    private boolean enabled = false; // Domyślnie konto jest nieaktywne

    // Gettery i Settery

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNrTelefonu() { return nrTelefonu; }
    public void setNrTelefonu(String nrTelefonu) { this.nrTelefonu = nrTelefonu; }

    public Rola getRola() { return rola; }
    public void setRola(Rola rola) { this.rola = rola; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}