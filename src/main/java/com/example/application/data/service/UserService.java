package com.example.application.data.service;

import com.example.application.data.entity.Pracownicy;
import com.example.application.data.entity.Rola;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.repository.PracownicyRepository;
import com.example.application.data.repository.RolaRepository;
import com.example.application.data.repository.UzytkownicyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis odpowiedzialny za zarządzanie użytkownikami systemu.
 * Obsługuje logikę dotyczącą czytelników, personelu oraz ról i bezpieczeństwa.
 */
@Service
@Transactional
public class UserService {

    private final UzytkownicyRepository uzytkownicyRepository;
    private final PracownicyRepository pracownicyRepository;
    private final RolaRepository rolaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UzytkownicyRepository uzytkownicyRepository,
                       PracownicyRepository pracownicyRepository,
                       RolaRepository rolaRepository,
                       PasswordEncoder passwordEncoder) {
        this.uzytkownicyRepository = uzytkownicyRepository;
        this.pracownicyRepository = pracownicyRepository;
        this.rolaRepository = rolaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Zarządzanie czytelnikami

    /**
     * Wyszukuje czytelników na podstawie fragmentu imienia lub nazwiska.
     */
    public List<Uzytkownicy> findAllUzytkownicy(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return uzytkownicyRepository.findAll();
        } else {
            return uzytkownicyRepository.search(filterText);
        }
    }

    /**
     * Zwraca listę kont czytelników, które oczekują na aktywację.
     */
    public List<Uzytkownicy> findAllPendingUzytkownicy() {
        return uzytkownicyRepository.findByEnabled(false);
    }

    public Uzytkownicy findUzytkownikByEmail(String email) {
        return uzytkownicyRepository.findByEmail(email);
    }

    public long countAllUsers() {
        return uzytkownicyRepository.count();
    }

    /**
     * Zapisuje lub aktualizuje dane czytelnika.
     * W przypadku nowego konta domyślnie ustawia je jako nieaktywne oraz szyfruje hasło przed zapisem.
     */
    public void saveUzytkownik(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return;

        if (uzytkownik.getId() == null) {
            uzytkownik.setEnabled(false);
        }

        if (uzytkownik.getPassword() != null && !uzytkownik.getPassword().isEmpty()) {
            if (!uzytkownik.getPassword().startsWith("$2a$")) {
                uzytkownik.setPassword(passwordEncoder.encode(uzytkownik.getPassword()));
            }
        }
        uzytkownicyRepository.save(uzytkownik);
    }

    public void deleteUzytkownik(Uzytkownicy uzytkownik) {
        uzytkownicyRepository.delete(uzytkownik);
    }

    // Zarządzanie pracownikami

    public List<Pracownicy> findAllPracownicy(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return pracownicyRepository.findAll();
        } else {
            return pracownicyRepository.search(stringFilter);
        }
    }

    public Pracownicy findPracownikByEmail(String email) {
        return pracownicyRepository.findByEmail(email);
    }

    public long countAllEmployees() {
        return pracownicyRepository.count();
    }

    /**
     * Zapisuje dane pracownika.
     * Nowi pracownicy są domyślnie aktywni, a hasło jest automatycznie szyfrowane.
     */
    public void savePracownicy(Pracownicy pracownik) {
        if (pracownik == null) return;

        if (pracownik.getId() == null) {
            pracownik.setEnabled(true);
        }

        if (pracownik.getPassword() != null && !pracownik.getPassword().isEmpty()) {
            if (!pracownik.getPassword().startsWith("$2a$")) {
                pracownik.setPassword(passwordEncoder.encode(pracownik.getPassword()));
            }
        }
        pracownicyRepository.save(pracownik);
    }

    public void deletePracownicy(Pracownicy pracownicy) {
        pracownicyRepository.delete(pracownicy);
    }

    public List<Rola> findAllRoles() {
        return rolaRepository.findAll();
    }

    // Bezpieczeństwo i hasła

    public void updatePassword(Uzytkownicy uzytkownik, String newPassword) {
        if (uzytkownik == null) return;
        uzytkownik.setPassword(passwordEncoder.encode(newPassword));
        uzytkownicyRepository.save(uzytkownik);
    }

    public void updatePassword(Pracownicy pracownik, String newPassword) {
        if (pracownik == null) return;
        pracownik.setPassword(passwordEncoder.encode(newPassword));
        pracownicyRepository.save(pracownik);
    }
}