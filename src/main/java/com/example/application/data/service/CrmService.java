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
    private final WycofanieRepository wycofanieRepository;
    private final RezerwacjaRepository rezerwacjaRepository;
    private final ZarezerwowanaKsiazkaRepository zarezerwowanaKsiazkaRepository;

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
                      WypozyczonaKsiazkaRepository wypozyczonaKsiazkaRepository,
                      WycofanieRepository wycofanieRepository,
                      RezerwacjaRepository rezerwacjaRepository,
                      ZarezerwowanaKsiazkaRepository zarezerwowanaKsiazkaRepository) {       this.ksiazkaRepository = ksiazkaRepository;
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
        this.wycofanieRepository = wycofanieRepository;
        this.rezerwacjaRepository = rezerwacjaRepository;
        this.zarezerwowanaKsiazkaRepository = zarezerwowanaKsiazkaRepository;
    }

    public List<Ksiazka> findAllKsiazki(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return ksiazkaRepository.findAll();
        } else {
            return ksiazkaRepository.searchAll(stringFilter);
        }
    }

    public List<Ksiazka> findAllActiveKsiazki() {
        return ksiazkaRepository.findByStatusNot(StatusKsiazki.WYCOFANA);
    }

    public List<Ksiazka> findKsiazkiBySearch(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return ksiazkaRepository.findByStatusNot(StatusKsiazki.WYCOFANA);
        } else {
            return ksiazkaRepository.searchWithExclusion(searchTerm, StatusKsiazki.WYCOFANA);
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
        return ksiazkaRepository.findByPoddziedzinaAndStatusNot(p, StatusKsiazki.WYCOFANA);
    }

    public List<Ksiazka> findKsiazkiByDziedzina(Dziedzina d) {
        return ksiazkaRepository.findByDziedzinaAndStatusNot(d, StatusKsiazki.WYCOFANA);
    }

    public List<Ksiazka> findKsiazkiByAutor(Autor autor) {
        return ksiazkaRepository.findByAutorAndStatusNot(autor, StatusKsiazki.WYCOFANA);
    }


    @Transactional
    public void wypozyczKsiazke(Ksiazka ksiazka, Uzytkownicy uzytkownik) {
        if (ksiazka == null || uzytkownik == null) {
            throw new IllegalArgumentException("Nieprawidłowe dane wypożyczenia.");
        }

        // --- ZMIANA LOGIKI DOSTĘPNOŚCI ---
        boolean czyMoznaWypozyczyc = false;
        Rezerwacja rezerwacjaDoRealizacji = null;

        if (StatusKsiazki.DOSTEPNA.equals(ksiazka.getStatus())) {
            // Klasyczna sytuacja - książka z półki
            czyMoznaWypozyczyc = true;
        } else if (StatusKsiazki.ZAREZERWOWANA.equals(ksiazka.getStatus())) {
            // Książka zarezerwowana - sprawdzamy czy przez TEGO użytkownika
            rezerwacjaDoRealizacji = rezerwacjaRepository.findActiveReservationForBook(ksiazka)
                    .orElseThrow(() -> new IllegalStateException("Błąd spójności danych: Książka ma status Zarezerwowana, ale brak aktywnej rezerwacji."));

            if (rezerwacjaDoRealizacji.getUzytkownik().getId().equals(uzytkownik.getId())) {
                czyMoznaWypozyczyc = true; // To ten sam użytkownik, można wydać
            } else {
                throw new IllegalStateException("Ta książka jest zarezerwowana przez innego użytkownika!");
            }
        } else {
            throw new IllegalStateException("Ta książka nie jest dostępna (Status: " + ksiazka.getStatus().getName() + ")");
        }

        if (!czyMoznaWypozyczyc) return;

        // Sprawdzenie limitu wypożyczeń (5 sztuk)
        long liczbaWypozyczonych = wypozyczenieRepository.countByUzytkownikAndDataOddaniaIsNull(uzytkownik);
        if (liczbaWypozyczonych >= 5) {
            throw new IllegalStateException("Osiągnięto limit 5 wypożyczonych książek!");
        }

        // --- REALIZACJA WYPOŻYCZENIA ---
        int nowyLicznik = ksiazka.getLicznikWypozyczen() + 1;
        ksiazka.setLicznikWypozyczen(nowyLicznik);

        if (nowyLicznik % 5 == 0) {
            ksiazka.setWymagaKontroli(true);
        }

        Wypozyczenie wypozyczenie = new Wypozyczenie();
        wypozyczenie.setUzytkownik(uzytkownik);
        wypozyczenie.setDataWypozyczenia(java.time.LocalDate.now());
        wypozyczenie.setTerminZwrotu(java.time.LocalDate.now().plusDays(30));

        wypozyczenieRepository.save(wypozyczenie);

        WypozyczonaKsiazka pozycja = new WypozyczonaKsiazka(wypozyczenie, ksiazka);
        wypozyczonaKsiazkaRepository.save(pozycja);

        ksiazka.setStatus(StatusKsiazki.WYPOZYCZONA);
        ksiazkaRepository.save(ksiazka);

        // --- JEŚLI BYŁA TO REZERWACJA, OZNACZAMY JAKO ZREALIZOWANĄ ---
        if (rezerwacjaDoRealizacji != null) {
            rezerwacjaDoRealizacji.setStatus(StatusRezerwacji.ZREALIZOWANA);
            rezerwacjaDoRealizacji.setZrealizowana(true);
            rezerwacjaRepository.save(rezerwacjaDoRealizacji);
        }
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

            if (k.isWymagaKontroli()) {
                k.setStatus(StatusKsiazki.W_KONTROLI);
            } else {
                k.setStatus(StatusKsiazki.DO_ODLOZENIA);
            }
            ksiazkaRepository.save(k);
        }
    }

    public List<Wypozyczenie> findAllActiveWypozyczenia() {
        return wypozyczenieRepository.findAllByDataOddaniaIsNullOrderByDataWypozyczeniaDesc();
    }

    public List<Ksiazka> findKsiazkiDoKontroli() {
        // Szukamy tylko tych, które wymagają kontroli I zostały już zwrócone (są dostępne)
        return ksiazkaRepository.findByWymagaKontroliTrueAndStatus(StatusKsiazki.W_KONTROLI);
    }

    public void zaktualizujStanPoKontroli(Ksiazka ksiazka, StanFizyczny nowyStan) {
        if (ksiazka == null) return;

        ksiazka.setStanFizyczny(nowyStan);
        ksiazka.setWymagaKontroli(false); // Zdejmujemy flagę, bo kontrola wykonana

        // --- ZMIANA: Automatyczna blokada książek uszkodzonych ---
        if (nowyStan == StanFizyczny.DO_RENOWACJI) {
            // Blokujemy wypożyczanie
            ksiazka.setStatus(StatusKsiazki.W_RENOWACJI);
        } else if (nowyStan == StanFizyczny.DO_WYCOFANIA) {
            // Blokujemy wypożyczanie (książka czeka na decyzję kierownika w widoku "Do wycofania")
            // Ustawiamy status W_KONTROLI, żeby nie była dostępna w katalogu
            ksiazka.setStatus(StatusKsiazki.W_KONTROLI);
        } else {
            // Książka jest w dobrym stanie, wraca do obiegu
            ksiazka.setStatus(StatusKsiazki.DO_ODLOZENIA);
        }
        // ---------------------------------------------------------

        ksiazkaRepository.save(ksiazka);
    }

    public List<Ksiazka> findKsiazkiDoDecyzjiWycofania() {
        // Szukamy książek ze stanem DO_WYCOFANIA, które jeszcze nie mają statusu WYCOFANA
        return ksiazkaRepository.findByStanFizycznyAndStatusNot(
                StanFizyczny.DO_WYCOFANIA,
                StatusKsiazki.WYCOFANA
        );
    }

    public void wycofajKsiazke(Ksiazka ksiazka, Pracownicy pracownik, String powod) {
        if (ksiazka == null || pracownik == null) return;

        // Tworzymy rekord w historii wycofań
        Wycofanie wycofanie = new Wycofanie(ksiazka, pracownik, java.time.LocalDate.now(), powod);
        wycofanieRepository.save(wycofanie);

        // Zmieniamy status książki, żeby zniknęła z obiegu
        ksiazka.setStatus(StatusKsiazki.WYCOFANA);
        ksiazkaRepository.save(ksiazka);
    }

    public Pracownicy findPracownikByEmail(String email) {
        return pracownicyRepository.findByEmail(email);
    }

    public com.example.application.data.entity.Wycofanie findWycofanieByKsiazka(Ksiazka ksiazka) {
        return wycofanieRepository.findByKsiazka(ksiazka).orElse(null);
    }

    public List<Ksiazka> findKsiazkiDoOdlozenia() {
        return ksiazkaRepository.findByStatus(StatusKsiazki.DO_ODLOZENIA);
    }

    public void potwierdzOdlozenie(Ksiazka ksiazka) {
        if (ksiazka == null) return;
        ksiazka.setStatus(StatusKsiazki.DOSTEPNA);
        ksiazkaRepository.save(ksiazka);
    }

    // ... (wewnątrz CrmService)

    @Transactional
    public void zarezerwujKsiazke(Ksiazka ksiazka, Uzytkownicy uzytkownik) {
        if (ksiazka == null || uzytkownik == null) return;

        // 1. Sprawdź status książki
        if (!StatusKsiazki.DOSTEPNA.equals(ksiazka.getStatus())) {
            throw new IllegalStateException("Tej książki nie można zarezerwować (jest niedostępna).");
        }

        // 2. NOWOŚĆ: Sprawdź limit rezerwacji (Max 3 aktywne)
        long aktywneRezerwacje = rezerwacjaRepository.countByUzytkownikAndStatus(uzytkownik, StatusRezerwacji.AKTYWNA);
        if (aktywneRezerwacje >= 3) {
            throw new IllegalStateException("Osiągnięto limit 3 aktywnych rezerwacji. Anuluj lub odbierz inne książki.");
        }

        Rezerwacja rezerwacja = new Rezerwacja(uzytkownik);
        rezerwacjaRepository.save(rezerwacja);

        ZarezerwowanaKsiazka zk = new ZarezerwowanaKsiazka(rezerwacja, ksiazka);
        zarezerwowanaKsiazkaRepository.save(zk);

        ksiazka.setStatus(StatusKsiazki.ZAREZERWOWANA);
        ksiazkaRepository.save(ksiazka);
    }

    @Transactional
    public void anulujRezerwacje(Rezerwacja rezerwacja) {
        if (rezerwacja == null || rezerwacja.getStatus() != StatusRezerwacji.AKTYWNA) {
            return; // Można rzucić wyjątek, ale tutaj po prostu ignorujemy
        }

        // 1. Zmień status rezerwacji
        rezerwacja.setStatus(StatusRezerwacji.ANULOWANA);
        rezerwacjaRepository.save(rezerwacja);

        // 2. Zwolnij książki (Status: ZAREZERWOWANA -> DOSTEPNA)
        for (ZarezerwowanaKsiazka zk : rezerwacja.getZarezerwowaneKsiazki()) {
            Ksiazka ksiazka = zk.getKsiazka();
            ksiazka.setStatus(StatusKsiazki.DOSTEPNA);
            ksiazkaRepository.save(ksiazka);
        }
    }

    public List<Rezerwacja> findRezerwacjeByUser(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return java.util.Collections.emptyList();
        return rezerwacjaRepository.findByUzytkownikOrderByDataRezerwacjiDesc(uzytkownik);
    }
}