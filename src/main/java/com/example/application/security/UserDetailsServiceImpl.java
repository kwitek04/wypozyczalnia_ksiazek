package com.example.application.security;

import com.example.application.data.entity.Pracownicy;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.repository.PracownicyRepository;
import com.example.application.data.repository.UzytkownicyRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;


import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PracownicyRepository pracownicyRepository;
    private final UzytkownicyRepository uzytkownicyRepository;

    public UserDetailsServiceImpl(PracownicyRepository pracownicyRepository,
                                  UzytkownicyRepository uzytkownicyRepository) {
        this.pracownicyRepository = pracownicyRepository;
        this.uzytkownicyRepository = uzytkownicyRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Szukamy w pracownikach
        Pracownicy pracownik = pracownicyRepository.findByEmail(username);
        if (pracownik != null) {
            return new User(
                    pracownik.getEmail(),
                    pracownik.getPassword(),
                    pracownik.isEnabled(), // enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    true, // accountNonLocked
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + pracownik.getRola().getName().toUpperCase()))
            );
        }

        // 2. Jeśli nie ma w pracownikach, szukamy w użytkownikach
        Uzytkownicy uzytkownik = uzytkownicyRepository.findByEmail(username);
        if (uzytkownik != null) {
            // Użytkownikom (czytelnikom) nadajemy domyślną rolę ROLE_USER
            return new User(
                    uzytkownik.getEmail(),
                    uzytkownik.getPassword(),
                    uzytkownik.isEnabled(), // enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    !uzytkownik.isLocked(), // accountNonLocked
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        throw new UsernameNotFoundException("Nie znaleziono użytkownika: " + username);
    }
}