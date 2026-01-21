package com.example.application.data.service;

import com.example.application.data.entity.*;
import com.example.application.data.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@Transactional
public class LibraryService {

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

    public LibraryService(RolaRepository rolaRepository,
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

        Dziedzina parent = dziedzinaRepository.findById(poddziedzina.getDziedzina().getId()).orElse(null);

        if (parent != null) {
            parent.getPoddziedziny().removeIf(p -> p.getId().equals(poddziedzina.getId()));
            dziedzinaRepository.save(parent);
        }
    }

    public void deleteDziedzina(Dziedzina dziedzina) {
        dziedzinaRepository.delete(dziedzina);
    }

    public void saveDziedzina(Dziedzina dziedzina) {
        dziedzinaRepository.save(dziedzina);
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

        if (pracownik.getPassword() != null && !pracownik.getPassword().isEmpty()) {
            if (!pracownik.getPassword().startsWith("$2a$")) {
                pracownik.setPassword(passwordEncoder.encode(pracownik.getPassword()));
            }
        }

        pracownicyRepository.save(pracownik);
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

        if (ksiazka.getDaneKsiazki() != null) {
            daneKsiazkiRepository.save(ksiazka.getDaneKsiazki());
        }

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

        boolean czyMoznaWypozyczyc = false;
        Rezerwacja rezerwacjaDoRealizacji = null;

        if (StatusKsiazki.DOSTEPNA.equals(ksiazka.getStatus())) {
            czyMoznaWypozyczyc = true;
        } else if (StatusKsiazki.ZAREZERWOWANA.equals(ksiazka.getStatus())) {
            rezerwacjaDoRealizacji = rezerwacjaRepository.findActiveReservationForBook(ksiazka)
                    .orElseThrow(() -> new IllegalStateException("Błąd spójności danych"));

            if (rezerwacjaDoRealizacji.getUzytkownik().getId().equals(uzytkownik.getId())) {
                czyMoznaWypozyczyc = true;
            } else {
                throw new IllegalStateException("Ta książka jest zarezerwowana przez innego użytkownika!");
            }
        } else {
            throw new IllegalStateException("Ta książka nie jest dostępna (Status: " + ksiazka.getStatus().getName() + ")");
        }

        if (!czyMoznaWypozyczyc) return;

        long liczbaWypozyczonych = wypozyczenieRepository.countByUzytkownikAndDataOddaniaIsNull(uzytkownik);
        if (liczbaWypozyczonych >= 5) {
            throw new IllegalStateException("Osiągnięto limit 5 wypożyczonych książek!");
        }

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

        if (rezerwacjaDoRealizacji != null) {
            rezerwacjaDoRealizacji.setStatus(StatusRezerwacji.ZREALIZOWANA);
            rezerwacjaDoRealizacji.setZrealizowana(true);
            rezerwacjaRepository.save(rezerwacjaDoRealizacji);
        }
    }

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
        return ksiazkaRepository.findByWymagaKontroliTrueAndStatus(StatusKsiazki.W_KONTROLI);
    }

    public void zaktualizujStanPoKontroli(Ksiazka ksiazka, StanFizyczny nowyStan) {
        if (ksiazka == null) return;

        ksiazka.setStanFizyczny(nowyStan);
        ksiazka.setWymagaKontroli(false);
        ksiazka.setDataOstatniejKontroli(java.time.LocalDate.now());

        if (nowyStan == StanFizyczny.DO_RENOWACJI) {
            ksiazka.setStatus(StatusKsiazki.W_RENOWACJI);
        } else if (nowyStan == StanFizyczny.DO_WYCOFANIA) {
            ksiazka.setStatus(StatusKsiazki.W_KONTROLI);
        } else {
            ksiazka.setStatus(StatusKsiazki.DO_ODLOZENIA);
        }

        ksiazkaRepository.save(ksiazka);
    }

    public List<Ksiazka> findKsiazkiDoDecyzjiWycofania() {
        return ksiazkaRepository.findByStanFizycznyAndStatusNot(
                StanFizyczny.DO_WYCOFANIA,
                StatusKsiazki.WYCOFANA
        );
    }

    public void wycofajKsiazke(Ksiazka ksiazka, Pracownicy pracownik, String powod) {
        if (ksiazka == null || pracownik == null) return;

        Wycofanie wycofanie = new Wycofanie(ksiazka, pracownik, java.time.LocalDate.now(), powod);
        wycofanieRepository.save(wycofanie);

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

    @Transactional
    public void zarezerwujKsiazke(Ksiazka ksiazka, Uzytkownicy uzytkownik) {
        if (ksiazka == null || uzytkownik == null) return;

        if (!StatusKsiazki.DOSTEPNA.equals(ksiazka.getStatus())) {
            throw new IllegalStateException("Tej książki nie można zarezerwować (jest niedostępna).");
        }

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
            return;
        }

        rezerwacja.setStatus(StatusRezerwacji.ANULOWANA);
        rezerwacjaRepository.save(rezerwacja);

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

    @org.springframework.transaction.annotation.Transactional
    public void przedluzWypozyczenie(Wypozyczenie wypozyczenie) {
        if (wypozyczenie == null) return;

        if (wypozyczenie.getDataOddania() != null) {
            throw new IllegalStateException("Nie można przedłużyć oddanej książki.");
        }

        if (wypozyczenie.isPrzedluzone()) {
            throw new IllegalStateException("To wypożyczenie było już raz przedłużane.");
        }

        long dniDoTerminu = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(),
                wypozyczenie.getTerminZwrotu()
        );

        if (dniDoTerminu > 3) {
            throw new IllegalStateException("Przedłużenie możliwe najwcześniej 3 dni przed terminem zwrotu.");
        }

        if (dniDoTerminu < 0) {
            throw new IllegalStateException("Termin minął. Prosimy o zwrot książki.");
        }

        wypozyczenie.setTerminZwrotu(wypozyczenie.getTerminZwrotu().plusDays(7));
        wypozyczenie.setPrzedluzone(true);

        wypozyczenieRepository.save(wypozyczenie);
    }

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

    // --- METODY STATYSTYCZNE ---

    public long countWypozyczeniaWOkresie(java.time.LocalDate start, java.time.LocalDate end) {
        return wypozyczenieRepository.countByDataWypozyczeniaBetween(start, end);
    }

    public long countZwrotyWOkresie(java.time.LocalDate start, java.time.LocalDate end) {
        return wypozyczenieRepository.countByDataOddaniaBetween(start, end);
    }

    public long countAllUsers() {
        return uzytkownicyRepository.count();
    }

    public long countAllEmployees() {
        return pracownicyRepository.count();
    }

    public long countAllBooks() {
        return ksiazkaRepository.count();
    }

    public long countKsiazkiByStatus(StatusKsiazki status) {
        return ksiazkaRepository.countByStatus(status);
    }

    public long countActiveUsersInPeriod(java.time.LocalDate start, java.time.LocalDate end) {
        return wypozyczenieRepository.countUniqueUsersByDataWypozyczeniaBetween(start, end);
    }

    public void przeliczKaryUzytkownika(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return;

        List<Wypozyczenie> wypozyczenia = wypozyczenieRepository.findAllByUzytkownik(uzytkownik); // Używamy istniejącej metody repozytorium (Spring Data sam ją wygeneruje po nazwie jeśli jej nie ma, a w poprzednich krokach z niej korzystaliśmy)
        // Jeśli findAllByUzytkownik nie jest widoczne, użyj: findWypozyczeniaByUser(uzytkownik) i pracuj na tej liście

        for (Wypozyczenie w : wypozyczenia) {
            // Jeśli kara została już opłacona (np. wprowadzimy status opłacenia w przyszłości), pomijamy.
            // Na razie po prostu sprawdzamy terminy.

            boolean czyNalezySieKara = false;
            java.time.LocalDate termin = w.getTerminZwrotu();
            java.time.LocalDate oddanie = w.getDataOddania();
            java.time.LocalDate dzis = java.time.LocalDate.now();

            if (oddanie != null) {
                // Książka oddana, sprawdzamy czy po terminie
                if (oddanie.isAfter(termin)) {
                    czyNalezySieKara = true;
                }
            } else {
                // Książka nadal u czytelnika, sprawdzamy czy dzisiaj jest po terminie
                if (dzis.isAfter(termin)) {
                    czyNalezySieKara = true;
                }
            }

            if (czyNalezySieKara) {
                if (w.getKara() == 0.0) { // Jeśli jeszcze nie ma kary, wpisz 10
                    w.setKara(10.0);
                    wypozyczenieRepository.save(w);
                }
            }
        }
    }

    public List<Wypozyczenie> findWypozyczeniaZKarami(Uzytkownicy uzytkownik) {
        // Najpierw upewnijmy się, że kary są aktualne
        przeliczKaryUzytkownika(uzytkownik);

        // Pobieramy wszystkie i filtrujemy te, które mają karę > 0
        return findWypozyczeniaByUser(uzytkownik).stream()
                .filter(w -> w.getKara() > 0)
                .collect(java.util.stream.Collectors.toList());
    }

    public Double obliczSumeKar(Uzytkownicy uzytkownik) {
        List<Wypozyczenie> zadluzone = findWypozyczeniaZKarami(uzytkownik);
        return zadluzone.stream()
                .mapToDouble(Wypozyczenie::getKara)
                .sum();
    }

    public List<WypozyczonaKsiazka> findHistoriaKsiazki(Ksiazka ksiazka) {
        return wypozyczonaKsiazkaRepository.findAllByKsiazkaOrderByWypozyczenieDataWypozyczeniaDesc(ksiazka);
    }
}