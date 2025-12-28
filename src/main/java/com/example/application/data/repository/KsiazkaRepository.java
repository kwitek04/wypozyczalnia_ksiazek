package com.example.application.data.repository;

import com.example.application.data.entity.Ksiazka;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KsiazkaRepository extends JpaRepository<Ksiazka, Long> {
}