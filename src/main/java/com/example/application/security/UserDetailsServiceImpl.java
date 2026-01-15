package com.example.application.security;

import com.example.application.data.entity.Pracownicy;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.repository.PracownicyRepository;
import com.example.application.data.repository.UzytkownicyRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        Pracownicy pracownik = pracownicyRepository.findByEmail(username);
        if (pracownik != null) {
            List<GrantedAuthority> authorities = pracownik.getRole().stream()
                    .map(rola -> new SimpleGrantedAuthority("ROLE_" + rola.getName().toUpperCase()))
                    .collect(Collectors.toList());

            return new User(
                    pracownik.getEmail(),
                    pracownik.getPassword(),
                    pracownik.isEnabled(),
                    true,
                    true,
                    true,
                    authorities
            );
        }

        Uzytkownicy uzytkownik = uzytkownicyRepository.findByEmail(username);
        if (uzytkownik != null) {
            return new User(
                    uzytkownik.getEmail(),
                    uzytkownik.getPassword(),
                    uzytkownik.isEnabled(),
                    true,
                    true,
                    !uzytkownik.isLocked(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        throw new UsernameNotFoundException("Nie znaleziono użytkownika: " + username);
    }
}