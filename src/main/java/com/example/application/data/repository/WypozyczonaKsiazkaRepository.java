package com.example.application.data.repository;
import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.WypozyczonaKsiazka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WypozyczonaKsiazkaRepository extends JpaRepository<WypozyczonaKsiazka, Long> {
    List<WypozyczonaKsiazka> findAllByKsiazkaOrderByWypozyczenieDataWypozyczeniaDesc(Ksiazka ksiazka);
}