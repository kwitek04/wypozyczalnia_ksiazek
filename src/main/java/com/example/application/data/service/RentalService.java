package com.example.application.data.service;

import com.example.application.data.entity.*;
import com.example.application.data.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serwis realizujący główną logikę biznesową wypożyczalni.
 * Odpowiada za procesy wypożyczania, zwrotów, rezerwacji, naliczania kar
 * oraz operacje magazynowe takie jak kontrola stanu i wycofywanie książek.
 */
@Service
@Transactional
public class RentalService {

    private final WypozyczenieRepository wypozyczenieRepository;
    private final WypozyczonaKsiazkaRepository wypozyczonaKsiazkaRepository;
    private final KsiazkaRepository ksiazkaRepository;
    private final RezerwacjaRepository rezerwacjaRepository;
    private final ZarezerwowanaKsiazkaRepository zarezerwowanaKsiazkaRepository;
    private final WycofanieRepository wycofanieRepository;

    public RentalService(WypozyczenieRepository wypozyczenieRepository,
                         WypozyczonaKsiazkaRepository wypozyczonaKsiazkaRepository,
                         KsiazkaRepository ksiazkaRepository,
                         RezerwacjaRepository rezerwacjaRepository,
                         ZarezerwowanaKsiazkaRepository zarezerwowanaKsiazkaRepository,
                         WycofanieRepository wycofanieRepository) {
        this.wypozyczenieRepository = wypozyczenieRepository;
        this.wypozyczonaKsiazkaRepository = wypozyczonaKsiazkaRepository;
        this.ksiazkaRepository = ksiazkaRepository;
        this.rezerwacjaRepository = rezerwacjaRepository;
        this.zarezerwowanaKsiazkaRepository = zarezerwowanaKsiazkaRepository;
        this.wycofanieRepository = wycofanieRepository;
    }

    // Proces wypożyczania

    /**
     * Realizuje proces wypożyczenia książki dla użytkownika.
     * Weryfikuje dostępność książki, limity wypożyczeń oraz ewentualne zadłużenie użytkownika.
     * Obsługuje również realizację rezerwacji, jeśli taka istnieje.
     */
    public void wypozyczKsiazke(Ksiazka ksiazka, Uzytkownicy uzytkownik) {
        if (ksiazka == null || uzytkownik == null) {
            throw new IllegalArgumentException("Nieprawidłowe dane wypożyczenia.");
        }

        przeliczKaryUzytkownika(uzytkownik);
        if (obliczSumeKar(uzytkownik) > 0) {
            throw new IllegalStateException("Nie możesz wypożyczyć książki, ponieważ masz nieopłacone należności wobec biblioteki.");
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
        wypozyczenie.setDataWypozyczenia(LocalDate.now());
        wypozyczenie.setTerminZwrotu(LocalDate.now().plusDays(30));

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

    /**
     * Przedłuża termin zwrotu wypożyczenia o 7 dni.
     * Operacja jest możliwa tylko raz i tylko w określonym oknie czasowym przed terminem zwrotu.
     */
    public void przedluzWypozyczenie(Wypozyczenie wypozyczenie) {
        if (wypozyczenie == null) return;

        if (wypozyczenie.getDataOddania() != null) {
            throw new IllegalStateException("Nie można przedłużyć oddanej książki.");
        }

        if (wypozyczenie.isPrzedluzone()) {
            throw new IllegalStateException("To wypożyczenie było już raz przedłużane.");
        }

        long dniDoTerminu = ChronoUnit.DAYS.between(LocalDate.now(), wypozyczenie.getTerminZwrotu());

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

    // Proces zwrotu książek

    /**
     * Rejestruje zgłoszenie zwrotu przez użytkownika (deklaracja chęci oddania książki).
     */
    public void zglosZwrot(Wypozyczenie wypozyczenie) {
        if (wypozyczenie == null || wypozyczenie.getDataOddania() != null) return;

        wypozyczenie.setZwrotZgloszony(true);
        wypozyczenieRepository.save(wypozyczenie);
    }

    /**
     * Zatwierdza fizyczny zwrot książki do biblioteki.
     * Aktualizuje statusy książek kierując je do kontroli lub odłożenia na półkę.
     */
    public void zwrocKsiazke(Wypozyczenie wypozyczenie) {
        if (wypozyczenie == null || wypozyczenie.getDataOddania() != null) return;

        wypozyczenie.setDataOddania(LocalDate.now());
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

    // Zarządzanie rezerwacjami

    /**
     * Rezerwuje książkę dla użytkownika, jeśli nie ma on zaległości i nie przekroczył limitu rezerwacji.
     */
    public void zarezerwujKsiazke(Ksiazka ksiazka, Uzytkownicy uzytkownik) {
        if (ksiazka == null || uzytkownik == null) return;

        przeliczKaryUzytkownika(uzytkownik);
        if (obliczSumeKar(uzytkownik) > 0) {
            throw new IllegalStateException("Nie możesz zarezerwować książki, ponieważ masz nieopłacone należności wobec wypożyczalni.");
        }

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

    /**
     * Anuluje aktywną rezerwację i zwalnia zarezerwowane książki.
     */
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

    // Naliczanie kar

    /**
     * Przelicza i aktualizuje kary dla wszystkich wypożyczeń danego użytkownika.
     * Uwzględnia kary za opóźnienie oraz kary za zagubienie (długotrwałe przetrzymanie).
     */
    public void przeliczKaryUzytkownika(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return;

        List<Wypozyczenie> wypozyczenia = wypozyczenieRepository.findAllByUzytkownik(uzytkownik);

        for (Wypozyczenie w : wypozyczenia) {
            LocalDate termin = w.getTerminZwrotu();
            LocalDate oddanie = w.getDataOddania();
            LocalDate dzis = LocalDate.now();

            // Podstawowa kara za opóźnienie (10 zł)
            boolean czyNalezySieKara = false;
            if (oddanie != null) {
                if (oddanie.isAfter(termin)) czyNalezySieKara = true;
            } else {
                if (dzis.isAfter(termin)) czyNalezySieKara = true;
            }

            if (czyNalezySieKara && w.getKara() == 0.0) {
                w.setKara(10.0);
            }

            // Kara za "zagubienie" (powyżej 7 dni spóźnienia) dla książek nieoddanych
            if (oddanie == null) {
                long dniSpoznienia = ChronoUnit.DAYS.between(termin, dzis);

                if (dniSpoznienia > 7 && !w.isNaliczonoKareZaZaginiecie()) {
                    double dodatkowaKara = 0.0;

                    for (WypozyczonaKsiazka wk : w.getWypozyczoneKsiazki()) {
                        Ksiazka k = wk.getKsiazka();

                        // Uznajemy książkę za utraconą, jeśli jeszcze nie jest wycofana
                        if (!StatusKsiazki.WYCOFANA.equals(k.getStatus())) {
                            k.setStatus(StatusKsiazki.WYCOFANA);

                            double cenaKsiazki = k.getDaneKsiazki().getCena() != null ? k.getDaneKsiazki().getCena() : 0.0;
                            if (cenaKsiazki == 0) cenaKsiazki = 40.0;

                            dodatkowaKara += (cenaKsiazki * 5); // Kara to 5-krotność ceny książki
                            ksiazkaRepository.save(k);
                        }
                    }

                    w.setKara(w.getKara() + dodatkowaKara);
                    w.setNaliczonoKareZaZaginiecie(true);
                }
            }
            wypozyczenieRepository.save(w);
        }
    }

    public List<Wypozyczenie> findWypozyczeniaZKarami(Uzytkownicy uzytkownik) {
        przeliczKaryUzytkownika(uzytkownik);
        return findWypozyczeniaByUser(uzytkownik).stream()
                .filter(w -> w.getKara() > 0)
                .collect(Collectors.toList());
    }

    public Double obliczSumeKar(Uzytkownicy uzytkownik) {
        List<Wypozyczenie> zadluzone = findWypozyczeniaZKarami(uzytkownik);
        return zadluzone.stream()
                .mapToDouble(Wypozyczenie::getKara)
                .sum();
    }

    // Operacje magazynowe (Wycofanie, Kontrola, Odłożenie)

    /**
     * Aktualizuje stan książki po przeprowadzonej kontroli fizycznej.
     * Decyduje o dalszym losie egzemplarza (powrót na półkę, renowacja, wycofanie).
     */
    public void zaktualizujStanPoKontroli(Ksiazka ksiazka, StanFizyczny nowyStan) {
        if (ksiazka == null) return;

        ksiazka.setStanFizyczny(nowyStan);
        ksiazka.setWymagaKontroli(false);
        ksiazka.setDataOstatniejKontroli(LocalDate.now());

        if (nowyStan == StanFizyczny.DO_RENOWACJI) {
            ksiazka.setStatus(StatusKsiazki.W_RENOWACJI);
        } else if (nowyStan == StanFizyczny.DO_WYCOFANIA) {
            ksiazka.setStatus(StatusKsiazki.W_KONTROLI); // Zostaje w kontroli do momentu decyzji kierownika
        } else {
            ksiazka.setStatus(StatusKsiazki.DO_ODLOZENIA);
        }

        ksiazkaRepository.save(ksiazka);
    }

    /**
     * Potwierdza odłożenie książki na półkę, czyniąc ją dostępną do wypożyczenia.
     */
    public void potwierdzOdlozenie(Ksiazka ksiazka) {
        if (ksiazka == null) return;
        ksiazka.setStatus(StatusKsiazki.DOSTEPNA);
        ksiazkaRepository.save(ksiazka);
    }

    public void wycofajKsiazke(Ksiazka ksiazka, Pracownicy pracownik, String powod) {
        if (ksiazka == null || pracownik == null) return;

        Wycofanie wycofanie = new Wycofanie(ksiazka, pracownik, LocalDate.now(), powod);
        wycofanieRepository.save(wycofanie);

        ksiazka.setStatus(StatusKsiazki.WYCOFANA);
        ksiazkaRepository.save(ksiazka);
    }

    public Wycofanie findWycofanieByKsiazka(Ksiazka ksiazka) {
        return wycofanieRepository.findByKsiazka(ksiazka).orElse(null);
    }

    // Metody pomocnicze i statystyczne (odczyt)

    public List<Wypozyczenie> findWypozyczeniaByUser(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return Collections.emptyList();
        return wypozyczenieRepository.findAllByUzytkownikOrderByDataWypozyczeniaDesc(uzytkownik);
    }

    public List<Wypozyczenie> findAllActiveWypozyczenia() {
        return wypozyczenieRepository.findAllByDataOddaniaIsNullOrderByDataWypozyczeniaDesc();
    }

    public List<Rezerwacja> findRezerwacjeByUser(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) return Collections.emptyList();
        return rezerwacjaRepository.findByUzytkownikOrderByDataRezerwacjiDesc(uzytkownik);
    }

    public List<WypozyczonaKsiazka> findHistoriaKsiazki(Ksiazka ksiazka) {
        return wypozyczonaKsiazkaRepository.findAllByKsiazkaOrderByWypozyczenieDataWypozyczeniaDesc(ksiazka);
    }

    public long countWypozyczeniaWOkresie(LocalDate start, LocalDate end) {
        return wypozyczenieRepository.countByDataWypozyczeniaBetween(start, end);
    }

    public long countZwrotyWOkresie(LocalDate start, LocalDate end) {
        return wypozyczenieRepository.countByDataOddaniaBetween(start, end);
    }

    public long countActiveUsersInPeriod(LocalDate start, LocalDate end) {
        return wypozyczenieRepository.countUniqueUsersByDataWypozyczeniaBetween(start, end);
    }
}