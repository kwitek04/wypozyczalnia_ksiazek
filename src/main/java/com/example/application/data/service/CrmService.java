package com.example.application.data.service;

import com.example.application.data.entity.*;
import com.example.application.data.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@Transactional
public class CrmService {

    private final ContactRepository contactRepository;
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
    private final TlumaczRepository tlumaczRepository;
    private final WypozyczenieRepository wypozyczenieRepository;
    private final WypozyczonaKsiazkaRepository wypozyczonaKsiazkaRepository;

    public CrmService(ContactRepository contactRepository,
                      StatusRepository statusRepository,
                      RolaRepository rolaRepository,
                      PracownicyRepository pracownicyRepository,
                      PasswordEncoder passwordEncoder,
                      UzytkownicyRepository uzytkownicyRepository,
                      KsiazkaRepository ksiazkaRepository,
                      AutorRepository autorRepository,
                      DziedzinaRepository dziedzinaRepository,
                      PoddziedzinaRepository poddziedzinaRepository,
                      DaneKsiazkiRepository daneKsiazkiRepository,
                      TlumaczRepository tlumaczRepository,
                      WypozyczenieRepository wypozyczenieRepository,
                      WypozyczonaKsiazkaRepository wypozyczonaKsiazkaRepository) {
        this.ksiazkaRepository = ksiazkaRepository;
        this.autorRepository = autorRepository;
        this.dziedzinaRepository = dziedzinaRepository;
        this.poddziedzinaRepository = poddziedzinaRepository;
        this.daneKsiazkiRepository = daneKsiazkiRepository;
        this.contactRepository = contactRepository;
        this.statusRepository = statusRepository;
        this.rolaRepository = rolaRepository;
        this.pracownicyRepository = pracownicyRepository;
        this.passwordEncoder = passwordEncoder;
        this.uzytkownicyRepository = uzytkownicyRepository;
        this.tlumaczRepository = tlumaczRepository;
        this.wypozyczenieRepository = wypozyczenieRepository;
        this.wypozyczonaKsiazkaRepository = wypozyczonaKsiazkaRepository;
    }

    public List<Ksiazka> findAllKsiazki(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return ksiazkaRepository.findAll();
        } else {
            // Tu później dodamy filtrowanie po tytule
            return ksiazkaRepository.findAll();
        }
    }

    public List<Ksiazka> findKsiazkiBySearch(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return ksiazkaRepository.findAll();
        } else {
            return ksiazkaRepository.search(searchTerm);
        }
    }

    public List<Autor> findAllAutorzy() { return autorRepository.findAll(); }
    public List<Dziedzina> findAllDziedziny() { return dziedzinaRepository.findAll(); }
    public List<Poddziedzina> findAllPoddziedziny() { return poddziedzinaRepository.findAll(); }
    public List<DaneKsiazki> findAllDaneKsiazki() { return daneKsiazkiRepository.findAll(); }
    public List<Poddziedzina> findPoddziedzinyByDziedzina(Dziedzina dziedzina) {
        return poddziedzinaRepository.findAllByDziedzina(dziedzina);
    }

    public void savePoddziedzina(Poddziedzina poddziedzina) {
        poddziedzinaRepository.save(poddziedzina);
    }

    @Transactional
    public void deletePoddziedzina(Poddziedzina poddziedzina) {
        if (poddziedzina == null) return;

        // 1. Pobieramy świeżą wersję rodzica (Dziedziny) z bazy danych
        // Używamy findById, żeby mieć pewność, że operujemy na zarządzanej encji
        Dziedzina parent = dziedzinaRepository.findById(poddziedzina.getDziedzina().getId()).orElse(null);

        if (parent != null) {
            // 2. Usuwamy poddziedzinę z listy rodzica, sprawdzając po ID
            // (to najbezpieczniejszy sposób, który działa nawet bez metod equals/hashCode)
            parent.getPoddziedziny().removeIf(p -> p.getId().equals(poddziedzina.getId()));

            // 3. Zapisujemy rodzica.
            // Mechanizm 'orphanRemoval=true' w encji Dziedzina automatycznie usunie
            // odłączoną poddziedzinę z bazy danych (wykona SQL DELETE).
            dziedzinaRepository.save(parent);
        }
    }

    public void deleteDziedzina(Dziedzina dziedzina) {
        dziedzinaRepository.delete(dziedzina);
    }

    public void saveDziedzina(Dziedzina dziedzina) {
        dziedzinaRepository.save(dziedzina);
    }


    public List<Contact> findAllContacts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return contactRepository.findAll();
        } else {
            return contactRepository.search(stringFilter);
        }
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

    public void deleteKsiazka(Ksiazka ksiazka) {
        if (ksiazka != null && ksiazka.getId() != null) {
            ksiazkaRepository.delete(ksiazka);
        }
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

    public void saveAutor(Autor autor) {
        if (autor != null) {
            autorRepository.save(autor);
        }
    }

    public java.util.List<com.example.application.data.entity.Tlumacz> findAllTlumacze() {
        return tlumaczRepository.findAll();
    }

    public void saveTlumacz(com.example.application.data.entity.Tlumacz tlumacz) {
        if (tlumacz != null) {
            tlumaczRepository.save(tlumacz);
        }
    }

    public List<Ksiazka> findKsiazkiByPoddziedzina(Poddziedzina p) {
        return ksiazkaRepository.findByPoddziedzina(p);
    }

    public List<Ksiazka> findKsiazkiByDziedzina(Dziedzina d) {
        return ksiazkaRepository.findByDziedzina(d);
    }

    public List<Ksiazka> findKsiazkiByAutor(com.example.application.data.entity.Autor autor) {
        return ksiazkaRepository.findByAutor(autor);
    }

    public void wypozyczKsiazke(Ksiazka ksiazka, Uzytkownicy uzytkownik) {
        if (ksiazka == null || uzytkownik == null) {
            throw new IllegalArgumentException("Nieprawidłowe dane wypożyczenia.");
        }

        // 1. Sprawdź dostępność
        if (!StatusKsiazki.DOSTEPNA.equals(ksiazka.getStatus())) {
            throw new IllegalStateException("Ta książka nie jest już dostępna.");
        }

        // 2. NOWE ZABEZPIECZENIE: Sprawdź limit
        long liczbaWypozyczonych = wypozyczenieRepository.countByUzytkownikAndDataOddaniaIsNull(uzytkownik);
        if (liczbaWypozyczonych >= 5) {
            throw new IllegalStateException("Osiągnięto limit 5 wypożyczonych książek! Oddaj inną pozycję, aby wypożyczyć nową.");
        }
        // 2. Stwórz transakcję wypożyczenia
        Wypozyczenie wypozyczenie = new Wypozyczenie();
        wypozyczenie.setUzytkownik(uzytkownik);
        wypozyczenie.setDataWypozyczenia(java.time.LocalDate.now());
        wypozyczenie.setTerminZwrotu(java.time.LocalDate.now().plusDays(30)); // np. 30 dni na zwrot

        wypozyczenieRepository.save(wypozyczenie);

        // 3. Przypisz książkę do wypożyczenia
        WypozyczonaKsiazka pozycja = new WypozyczonaKsiazka(wypozyczenie, ksiazka);
        wypozyczonaKsiazkaRepository.save(pozycja);

        // 4. Zmień status książki
        ksiazka.setStatus(StatusKsiazki.WYPOZYCZONA);
        ksiazkaRepository.save(ksiazka);
    }

    // Metoda pomocnicza do pobrania użytkownika po emailu (dla Security)
    public Uzytkownicy findUzytkownikByEmail(String email) {
        return uzytkownicyRepository.findByEmail(email);
    }

    public List<Wypozyczenie> findWypozyczeniaByUser(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return java.util.Collections.emptyList();
        return wypozyczenieRepository.findAllByUzytkownikOrderByDataWypozyczeniaDesc(uzytkownik);
    }

    public void zglosZwrot(Wypozyczenie wypozyczenie) {
        if (wypozyczenie == null || wypozyczenie.getDataOddania() != null) return;

        wypozyczenie.setZwrotZgloszony(true);
        wypozyczenieRepository.save(wypozyczenie);
    }

    @org.springframework.transaction.annotation.Transactional
    public void zwrocKsiazke(Wypozyczenie wypozyczenie) {
        if (wypozyczenie == null || wypozyczenie.getDataOddania() != null) return;

        wypozyczenie.setDataOddania(java.time.LocalDate.now());
        wypozyczenieRepository.save(wypozyczenie);

        for (WypozyczonaKsiazka wk : wypozyczenie.getWypozyczoneKsiazki()) {
            Ksiazka k = wk.getKsiazka();
            k.setStatus(StatusKsiazki.DOSTEPNA);
            ksiazkaRepository.save(k);
        }
    }

    public List<Wypozyczenie> findAllActiveWypozyczenia() {
        return wypozyczenieRepository.findAllByDataOddaniaIsNullOrderByDataWypozyczeniaDesc();
    }
}