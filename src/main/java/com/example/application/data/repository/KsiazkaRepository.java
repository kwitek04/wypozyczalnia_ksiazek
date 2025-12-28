package com.example.application.data.repository;

import com.example.application.data.entity.Ksiazka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KsiazkaRepository extends JpaRepository<Ksiazka, Long> {
    List<Ksiazka> findByDaneKsiazkiTytulContainingIgnoreCase(String tytul);
}