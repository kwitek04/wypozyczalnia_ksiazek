package com.example.application.data.repository;

import com.example.application.data.entity.Ksiazka;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DaneKsiazkiRepository extends JpaRepository<Ksiazka, Long> {
}