package com.example.application.data.service;

import com.example.application.data.entity.*;
import com.example.application.data.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CrmService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final StatusRepository statusRepository;
    private final RolaRepository rolaRepository;
    private final PracownicyRepository pracownicyRepository;
    private final PasswordEncoder passwordEncoder;
    private final UzytkownicyRepository uzytkownicyRepository;
    private final KsiazkaRepository ksiazkaRepository;
    private final AutorRepository autorRepository;
    private final DziedzinaRepository dziedzinaRepository;
    private final PoddziedzinaRepository poddziedzinaRepository;
    private final DaneKsiazkiRepository daneKsiazkiRepository;

    public CrmService(ContactRepository contactRepository,
                      CompanyRepository companyRepository,
                      StatusRepository statusRepository,
                      RolaRepository rolaRepository,
                      PracownicyRepository pracownicyRepository,
                      PasswordEncoder passwordEncoder,
                      UzytkownicyRepository uzytkownicyRepository,
                      KsiazkaRepository ksiazkaRepository,
                      AutorRepository autorRepository,
                      DziedzinaRepository dziedzinaRepository,
                      PoddziedzinaRepository poddziedzinaRepository,
                      DaneKsiazkiRepository daneKsiazkiRepository) {
        this.ksiazkaRepository = ksiazkaRepository;
        this.autorRepository = autorRepository;
        this.dziedzinaRepository = dziedzinaRepository;
        this.poddziedzinaRepository = poddziedzinaRepository;
        this.daneKsiazkiRepository = daneKsiazkiRepository;
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.statusRepository = statusRepository;
        this.rolaRepository = rolaRepository;
        this.pracownicyRepository = pracownicyRepository;
        this.passwordEncoder = passwordEncoder;
        this.uzytkownicyRepository = uzytkownicyRepository;
    }

    public List<Ksiazka> findAllKsiazki(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return ksiazkaRepository.findAll();
        } else {
            // Tu później dodamy filtrowanie po tytule
            return ksiazkaRepository.findAll();
        }
    }

    public List<Autor> findAllAutorzy() { return autorRepository.findAll(); }
    public List<Dziedzina> findAllDziedziny() { return dziedzinaRepository.findAll(); }
    public List<Poddziedzina> findAllPoddziedziny() { return poddziedzinaRepository.findAll(); }
    public List<DaneKsiazki> findAllDaneKsiazki() { return daneKsiazkiRepository.findAll(); }



    public List<Contact> findAllContacts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return contactRepository.findAll();
        } else {
            return contactRepository.search(stringFilter);
        }
    }

    public long countContacts() {
        return contactRepository.count();
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }

    public void saveContact(Contact contact) {
        if (contact == null) {
            System.err.println("Contact is null. Are you sure you have connected your form to the application?");
            return;
        }
        contactRepository.save(contact);
    }

    public List<Pracownicy> findAllPracownicy(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return pracownicyRepository.findAll();
        } else {
            return pracownicyRepository.search(stringFilter);
        }
    }

    public long countPracownicy() {
        return pracownicyRepository.count();
    }

    public void deletePracownicy(Pracownicy pracownicy) {
        pracownicyRepository.delete(pracownicy);
    }

    public void savePracownicy(Pracownicy pracownik) {
        if (pracownik == null) return;

        if (pracownik.getId() == null) {
            pracownik.setEnabled(true);
        }

        // Logika: Jeśli pole hasła nie jest puste, szyfrujemy je przed zapisem
        if (pracownik.getPassword() != null && !pracownik.getPassword().isEmpty()) {
            // Sprawdzamy, czy hasło już jest zakodowane (zaczyna się od $2a$)
            // Jeśli nie jest - szyfrujemy
            if (!pracownik.getPassword().startsWith("$2a$")) {
                pracownik.setPassword(passwordEncoder.encode(pracownik.getPassword()));
            }
        }

        pracownicyRepository.save(pracownik);
    }

    public List<Status> findAllStatuses(){
        return statusRepository.findAll();
    }

    public List<Rola> findAllRoles(){
        return rolaRepository.findAll();
    }

    public List<Uzytkownicy> findAllUzytkownicy(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return uzytkownicyRepository.findAll();
        } else {
            return uzytkownicyRepository.search(filterText);
        }
    }

    public void saveKsiazka(Ksiazka ksiazka) {
        if (ksiazka == null) return;

        // 1. Najpierw zapisujemy opis (ISBN), jeśli to nowa pozycja w katalogu
        if (ksiazka.getDaneKsiazki() != null) {
            daneKsiazkiRepository.save(ksiazka.getDaneKsiazki());
        }

        // 2. Potem zapisujemy konkretny egzemplarz (Ksiazka)
        ksiazkaRepository.save(ksiazka);
    }

    public void saveUzytkownik(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return;

        if (uzytkownik.getId() == null) {
            uzytkownik.setEnabled(false);
        }

        // Szyfrowanie hasła (identycznie jak u pracowników)
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

    public List<Uzytkownicy> findAllPendingUzytkownicy() {
        return uzytkownicyRepository.findByEnabled(false);
    }
}