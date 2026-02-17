package com.example.application.data.service;

import com.example.application.data.entity.*;
import com.example.application.data.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private KsiazkaRepository ksiazkaRepository;
    @Mock
    private DaneKsiazkiRepository daneKsiazkiRepository;
    @Mock
    private DziedzinaRepository dziedzinaRepository;
    @Mock
    private PoddziedzinaRepository poddziedzinaRepository;
    @Mock
    private AutorRepository autorRepository;
    @Mock
    private TlumaczRepository tlumaczRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("Zapis książki powinien zapisać także jej dane szczegółowe (DaneKsiazki)")
    void saveKsiazka_ZapisujeRelacje() {
        // Given
        Ksiazka k = new Ksiazka();
        DaneKsiazki d = new DaneKsiazki();
        d.setIsbn("1234567890");
        k.setDaneKsiazki(d);

        // When
        bookService.saveKsiazka(k);

        // Then
        verify(daneKsiazkiRepository).save(d); // Najpierw dane
        verify(ksiazkaRepository).save(k);     // Potem książka
    }

    @Test
    @DisplayName("Usuwanie poddziedziny powinno usunąć ją z listy u rodzica (Dziedziny) przed usunięciem rekordu")
    void deletePoddziedzina_CzysciRelacjeRodzica() {
        // Given
        Dziedzina rodzic = new Dziedzina("Nauka");
        rodzic.setId(1L);

        Poddziedzina dziecko = new Poddziedzina("Fizyka", rodzic);
        dziecko.setId(10L);

        // Symulujemy, że lista poddziedzin rodzica zawiera ten element
        rodzic.setPoddziedziny(new ArrayList<>());
        rodzic.getPoddziedziny().add(dziecko);

        // Mockujemy znalezienie rodzica w bazie
        when(dziedzinaRepository.findById(1L)).thenReturn(Optional.of(rodzic));

        // When
        bookService.deletePoddziedzina(dziecko);

        // Then
        // 1. Sprawdzamy czy dziecko zniknęło z listy rodzica
        assertTrue(rodzic.getPoddziedziny().isEmpty(), "Poddziedzina powinna zostać usunięta z listy rodzica");
        // 2. Sprawdzamy czy zapisano zaktualizowanego rodzica
        verify(dziedzinaRepository).save(rodzic);
        // 3. Sprawdzamy czy usunięto dziecko
        verify(poddziedzinaRepository).delete(dziecko);
    }

    @Test
    @DisplayName("Wyszukiwanie z pustą frazą powinno zwrócić wszystkie aktywne książki")
    void findKsiazkiBySearch_PustyString_ZwracaWszystkie() {
        // When
        bookService.findKsiazkiBySearch("");

        // Then
        // Powinno wywołać metodę, która tylko filtruje wycofane, a nie szuka po tekście
        verify(ksiazkaRepository).findByStatusNot(StatusKsiazki.WYCOFANA);
        verify(ksiazkaRepository, never()).searchWithExclusion(anyString(), any());
    }
}