package com.example.application.data.entity;

import com.example.application.data.repository.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Entity
public class Uzytkownicy extends AbstractEntity {

    @NotEmpty
    @Size(max = 15, message = "Imię może mieć maksymalnie 15 znaków")
    private String imie = "";

    @NotEmpty
    @Size(max = 25, message = "Nazwisko może mieć maksymalnie 25 znaków")
    private String nazwisko = "";

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być z przeszłości")
    private LocalDate dataUrodzenia;

    @Email
    @NotEmpty
    @Size(max = 50, message = "Email może mieć maksymalnie 50 znaków")
    private String email = "";

    @NotEmpty
    @Pattern(regexp = "\\d+", message = "Numer telefonu może zawierać tylko cyfry")
    @Size(min = 9, max = 15, message = "Numer telefonu musi mieć od 9 do 15 cyfr")
    private String nrTelefonu = "";

    @NotEmpty(message = "Hasło nie może być puste")
    @Size(min = 6, message = "Hasło musi mieć minimum 6 znaków")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).*$", message = "Hasło musi mieć minimum jedną literę i jedną cyfrę")
    private String password;

    @Override
    public String toString() {
        return imie + " " + nazwisko;
    }

    private boolean enabled = false; // Domyślnie konto jest nieaktywne

    private boolean locked = false;

    // Gettery i Settery

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public LocalDate getDataUrodzenia() { return dataUrodzenia; }
    public void setDataUrodzenia(LocalDate dataUrodzenia) { this.dataUrodzenia = dataUrodzenia; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNrTelefonu() { return nrTelefonu; }
    public void setNrTelefonu(String nrTelefonu) { this.nrTelefonu = nrTelefonu; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}