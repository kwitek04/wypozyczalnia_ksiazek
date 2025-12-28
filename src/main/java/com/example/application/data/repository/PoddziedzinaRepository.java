package com.example.application.data.repository;

import com.example.application.data.entity.Poddziedzina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.application.data.entity.Dziedzina;

public interface PoddziedzinaRepository extends JpaRepository<Poddziedzina, Long> {
    // Dodatkowa przydatna metoda: znajdź poddziedziny należące do konkretnej dziedziny
    List<Poddziedzina> findAllByDziedzina(Dziedzina dziedzina);
}