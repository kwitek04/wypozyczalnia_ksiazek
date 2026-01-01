package com.example.application.data.repository;

import com.example.application.data.entity.Wycofanie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WycofanieRepository extends JpaRepository<Wycofanie, Long> {
}