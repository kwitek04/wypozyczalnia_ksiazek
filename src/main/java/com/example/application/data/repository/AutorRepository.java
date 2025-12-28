package com.example.application.data.repository;

import com.example.application.data.entity.Autor;
import com.example.application.data.entity.Ksiazka;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}