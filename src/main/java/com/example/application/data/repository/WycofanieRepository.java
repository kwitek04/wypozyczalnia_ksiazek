package com.example.application.data.repository;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Wycofanie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WycofanieRepository extends JpaRepository<Wycofanie, Long> {
    Optional<Wycofanie> findByKsiazka(Ksiazka ksiazka);
}