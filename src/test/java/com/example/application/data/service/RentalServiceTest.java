package com.example.application.data.service;

import com.example.application.data.entity.*;
import com.example.application.data.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy RentalService.
 * Weryfikują poprawność logiki biznesowej dotyczącej wypożyczeń, zwrotów, rezerwacji oraz naliczania kar.
 */
@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private WypozyczenieRepository wypozyczenieRepository;
    @Mock
    private WypozyczonaKsiazkaRepository wypozyczonaKsiazkaRepository;
    @Mock
    private KsiazkaRepository ksiazkaRepository;
    @Mock
    private RezerwacjaRepository rezerwacjaRepository;
    @Mock
    private ZarezerwowanaKsiazkaRepository zarezerwowanaKsiazkaRepository;
    @Mock
    private WycofanieRepository wycofanieRepository;

    @InjectMocks
    private RentalService rentalService;

    private Uzytkownik uzytkownik;
    private Ksiazka ksiazka;

    /**
     * Przygotowanie danych testowych przed każdym uruchomieniem testu.
     */
    @BeforeEach
    void setUp() {
        uzytkownik = new Uzytkownik();
        uzytkownik.setId(1L);
        uzytkownik.setEmail("test@test.pl");

        ksiazka = new Ksiazka();
        ksiazka.setId(100L);
        ksiazka.setStatus(StatusKsiazki.DOSTEPNA);
        ksiazka.setLicznikWypozyczen(0);
        // Inicjalizacja danych szczegółowych wymaganych przez niektóre metody
        ksiazka.setDaneKsiazki(new DaneKsiazki("1234567890", "Tytuł Testowy", "Wydawnictwo", 2020));
    }

    // WYPOŻYCZANIE

    @Test
    @DisplayName("Powinien umożliwić wypożyczenie książki, gdy użytkownik nie ma długów ani limitu")
    void wypozyczKsiazke_Sukces_GdyWszystkoOk() {
        // Given: Użytkownik bez długów i wypożyczeń
        when(wypozyczenieRepository.findAllByUzytkownik(uzytkownik)).thenReturn(Collections.emptyList());
        when(wypozyczenieRepository.countByUzytkownikAndDataOddaniaIsNull(uzytkownik)).thenReturn(0L);

        // When: Próba wypożyczenia
        rentalService.wypozyczKsiazke(ksiazka, uzytkownik);

        // Then: Status książki zmieniony, encje zapisane
        assertEquals(StatusKsiazki.WYPOZYCZONA, ksiazka.getStatus());
        verify(wypozyczenieRepository, times(1)).save(any(Wypozyczenie.class));
        verify(wypozyczonaKsiazkaRepository, times(1)).save(any(WypozyczonaKsiazka.class));
    }

    @Test
    @DisplayName("Powinien zablokować wypożyczenie, gdy użytkownik osiągnął limit 5 książek")
    void wypozyczKsiazke_Blad_GdyPrzekroczonoLimit() {
        // Given: Użytkownik ma już 5 aktywnych wypożyczeń
        when(wypozyczenieRepository.findAllByUzytkownik(uzytkownik)).thenReturn(Collections.emptyList());
        when(wypozyczenieRepository.countByUzytkownikAndDataOddaniaIsNull(uzytkownik)).thenReturn(5L);

        // When & Then: Oczekujemy wyjątku
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rentalService.wypozyczKsiazke(ksiazka, uzytkownik);
        });

        assertTrue(exception.getMessage().contains("limit 5 wypożyczonych książek"));
        verify(wypozyczenieRepository, never()).save(any(Wypozyczenie.class));
    }

    @Test
    @DisplayName("Powinien zablokować wypożyczenie, gdy książka nie jest dostępna (np. w renowacji)")
    void wypozyczKsiazke_Blad_GdyKsiazkaNiedostepna() {
        // Given: Książka ma status W_RENOWACJI
        when(wypozyczenieRepository.findAllByUzytkownik(uzytkownik)).thenReturn(Collections.emptyList());
        ksiazka.setStatus(StatusKsiazki.W_RENOWACJI);

        // When & Then: Wyjątek powinien zostać rzucony
        assertThrows(IllegalStateException.class, () -> {
            rentalService.wypozyczKsiazke(ksiazka, uzytkownik);
        });
    }

    // NALICZANIE KAR

    @Test
    @DisplayName("Powinien naliczyć podstawową karę (10 zł) za przekroczenie terminu zwrotu")
    void przeliczKaryUzytkownika_NaliczaKareZaSpoznienie() {
        // Given: Wypożyczenie przeterminowane o 5 dni
        Wypozyczenie stareWypozyczenie = new Wypozyczenie();
        stareWypozyczenie.setUzytkownik(uzytkownik);
        stareWypozyczenie.setTerminZwrotu(LocalDate.now().minusDays(5));
        stareWypozyczenie.setKara(0.0);

        when(wypozyczenieRepository.findAllByUzytkownik(uzytkownik)).thenReturn(List.of(stareWypozyczenie));

        // When: Przeliczenie kar
        rentalService.przeliczKaryUzytkownika(uzytkownik);

        // Then: Kara powinna wynosić 10.0 zł
        assertEquals(10.0, stareWypozyczenie.getKara());
        verify(wypozyczenieRepository, times(1)).save(stareWypozyczenie);
    }

    // ZWROTY
    @Test
    @DisplayName("Powinien poprawnie przetworzyć zwrot książki i zmienić jej status")
    void zwrocKsiazke_Sukces_ZmieniaStatusy() {
        // Given: Aktywne wypożyczenie
        Wypozyczenie wypozyczenie = new Wypozyczenie();
        wypozyczenie.setUzytkownik(uzytkownik);

        WypozyczonaKsiazka wk = new WypozyczonaKsiazka(wypozyczenie, ksiazka);
        wypozyczenie.setWypozyczoneKsiazki(List.of(wk));

        // When: Zwrot książki
        rentalService.zwrocKsiazke(wypozyczenie);

        // Then: Data oddania ustawiona, książka skierowana do odłożenia
        assertNotNull(wypozyczenie.getDataOddania(), "Data oddania powinna zostać ustawiona");
        assertEquals(LocalDate.now(), wypozyczenie.getDataOddania());
        assertEquals(StatusKsiazki.DO_ODLOZENIA, ksiazka.getStatus());

        verify(wypozyczenieRepository).save(wypozyczenie);
        verify(ksiazkaRepository).save(ksiazka);
    }

    @Test
    @DisplayName("Powinien skierować książkę do kontroli, jeśli flaga wymagaKontroli jest ustawiona")
    void zwrocKsiazke_KierujeDoKontroli_GdyWymagana() {
        // Given: Książka wymagająca kontroli
        Wypozyczenie wypozyczenie = new Wypozyczenie();
        ksiazka.setWymagaKontroli(true);
        WypozyczonaKsiazka wk = new WypozyczonaKsiazka(wypozyczenie, ksiazka);
        wypozyczenie.setWypozyczoneKsiazki(List.of(wk));

        // When: Zwrot książki
        rentalService.zwrocKsiazke(wypozyczenie);

        // Then: Status książki to W_KONTROLI
        assertEquals(StatusKsiazki.W_KONTROLI, ksiazka.getStatus());
    }

    // REZERWACJE

    @Test
    @DisplayName("Powinien zablokować rezerwację, gdy użytkownik przekroczył limit 3 rezerwacji")
    void zarezerwujKsiazke_Blad_GdyLimitPrzekroczony() {
        // Given: Użytkownik ma 3 aktywne rezerwacje
        when(rezerwacjaRepository.countByUzytkownikAndStatus(uzytkownik, StatusRezerwacji.AKTYWNA))
                .thenReturn(3L);
        when(wypozyczenieRepository.findAllByUzytkownik(uzytkownik)).thenReturn(Collections.emptyList());

        // When & Then: Wyjątek o limicie
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rentalService.zarezerwujKsiazke(ksiazka, uzytkownik);
        });

        assertTrue(exception.getMessage().contains("limit 3 aktywnych rezerwacji"));
    }

    @Test
    @DisplayName("Powinien zablokować rezerwację, gdy użytkownik ma nieopłacone kary")
    void zarezerwujKsiazke_Blad_GdyUzytkownikMaDlug() {
        // Given: Użytkownik ma naliczoną karę
        Wypozyczenie dlug = new Wypozyczenie();
        dlug.setKara(15.0);
        dlug.setTerminZwrotu(LocalDate.now().plusDays(5));

        List<Wypozyczenie> listaDlugow = List.of(dlug);

        when(wypozyczenieRepository.findAllByUzytkownik(uzytkownik)).thenReturn(listaDlugow);
        when(wypozyczenieRepository.findAllByUzytkownikOrderByDataWypozyczeniaDesc(uzytkownik)).thenReturn(listaDlugow);

        // When & Then: Wyjątek o zadłużeniu
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rentalService.zarezerwujKsiazke(ksiazka, uzytkownik);
        });

        assertTrue(exception.getMessage().contains("nieopłacone należności"));
    }

    // PRZEDŁUŻENIA

    @Test
    @DisplayName("Powinien poprawnie przedłużyć termin zwrotu o 7 dni")
    void przedluzWypozyczenie_Sukces() {
        // Given: Wypożyczenie kończące się za 2 dni (kwalifikuje się do przedłużenia)
        Wypozyczenie w = new Wypozyczenie();
        w.setTerminZwrotu(LocalDate.now().plusDays(2));
        w.setPrzedluzone(false);

        // When: Przedłużenie
        rentalService.przedluzWypozyczenie(w);

        // Then: Termin przesunięty, flaga ustawiona
        assertTrue(w.isPrzedluzone());
        assertEquals(LocalDate.now().plusDays(9), w.getTerminZwrotu()); // 2 + 7 dni
        verify(wypozyczenieRepository).save(w);
    }

    @Test
    @DisplayName("Powinien zablokować przedłużenie, jeśli do terminu zostało więcej niż 3 dni")
    void przedluzWypozyczenie_Blad_ZbytWczesnie() {
        // Given: Termin zwrotu za 10 dni (zbyt wcześnie na przedłużenie)
        Wypozyczenie w = new Wypozyczenie();
        w.setTerminZwrotu(LocalDate.now().plusDays(10));

        // When & Then: Wyjątek
        assertThrows(IllegalStateException.class, () -> {
            rentalService.przedluzWypozyczenie(w);
        });
    }
}