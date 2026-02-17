package com.example.application.data.service;

import com.example.application.data.entity.Pracownik;
import com.example.application.data.entity.Uzytkownik;
import com.example.application.data.repository.PracownicyRepository;
import com.example.application.data.repository.RolaRepository;
import com.example.application.data.repository.UzytkownicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UzytkownicyRepository uzytkownicyRepository;
    @Mock
    private PracownicyRepository pracownicyRepository;
    @Mock
    private RolaRepository rolaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Nowy użytkownik powinien być domyślnie nieaktywny (enabled=false) i mieć zakodowane hasło")
    void saveUzytkownik_Nowy_UstawiaDomyslneWartosci() {
        // Given
        Uzytkownik u = new Uzytkownik();
        u.setPassword("tajneHaslo123");

        when(passwordEncoder.encode("tajneHaslo123")).thenReturn("encoded_tajneHaslo123");

        // When
        userService.saveUzytkownik(u);

        // Then
        assertFalse(u.isEnabled(), "Nowe konto czytelnika powinno być nieaktywne");
        assertEquals("encoded_tajneHaslo123", u.getPassword(), "Hasło musi zostać zakodowane");
        verify(uzytkownicyRepository).save(u);
    }

    @Test
    @DisplayName("Nowy pracownik powinien być domyślnie aktywny (enabled=true)")
    void savePracownicy_Nowy_UstawiaAktywny() {
        // Given
        Pracownik p = new Pracownik();
        p.setPassword("admin123");

        when(passwordEncoder.encode("admin123")).thenReturn("encoded_admin");

        // When
        userService.savePracownicy(p);

        // Then
        assertTrue(p.isEnabled(), "Konto pracownika powinno być domyślnie aktywne");
        verify(pracownicyRepository).save(p);
    }

    @Test
    @DisplayName("Zmiana hasła użytkownika powinna je zakodować przed zapisem")
    void updatePassword_KodujeHaslo() {
        // Given
        Uzytkownik u = new Uzytkownik();
        u.setPassword("stareHaslo");

        when(passwordEncoder.encode("noweHaslo")).thenReturn("encoded_noweHaslo");

        // When
        userService.updatePassword(u, "noweHaslo");

        // Then
        assertEquals("encoded_noweHaslo", u.getPassword());
        verify(uzytkownicyRepository).save(u);
    }
}