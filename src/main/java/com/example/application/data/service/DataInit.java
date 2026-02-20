package com.example.application.data.service;

import com.example.application.data.entity.Pracownik;
import com.example.application.data.entity.Rola;
import com.example.application.data.repository.PracownikRepository;
import com.example.application.data.repository.RolaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInit implements CommandLineRunner {

    private final PracownikRepository pracownicyRepository;
    private final RolaRepository rolaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(PracownikRepository pracownicyRepository,
                    RolaRepository rolaRepository,
                    PasswordEncoder passwordEncoder) {
        this.pracownicyRepository = pracownicyRepository;
        this.rolaRepository = rolaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rola rolaKierownik = rolaRepository.findAll().stream()
                .filter(r -> r.getName().equals("KIEROWNIK"))
                .findFirst()
                .orElseGet(() -> rolaRepository.save(new Rola("KIEROWNIK")));

        createRoleIfNotFound("BIBLIOTEKARZ");
        createRoleIfNotFound("MAGAZYNIER");


        if (pracownicyRepository.count() == 0) {
            Pracownik admin = new Pracownik();
            admin.setImie("Admin");
            admin.setNazwisko("Admin");
            admin.setEmail("admin@admin.pl");
            admin.setNrTelefonu("000000000");
            admin.setPassword(passwordEncoder.encode("admin")); //
            admin.setEnabled(true);
            admin.setRole(new HashSet<>(Collections.singletonList(rolaKierownik)));

            pracownicyRepository.save(admin);
            System.out.println(">>> Utworzono konto administratora: admin@admin.pl / admin");
        }
    }

    private void createRoleIfNotFound(String roleName) {
        if (rolaRepository.findAll().stream().noneMatch(r -> r.getName().equals(roleName))) {
            rolaRepository.save(new Rola(roleName));
        }
    }
}