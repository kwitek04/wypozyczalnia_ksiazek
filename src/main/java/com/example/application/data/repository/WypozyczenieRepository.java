package com.example.application.data.repository;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.entity.Wypozyczenie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WypozyczenieRepository extends JpaRepository<Wypozyczenie, Long> {
    List<Wypozyczenie> findAllByUzytkownikOrderByDataWypozyczeniaDesc(Uzytkownicy uzytkownik);

    long countByUzytkownikAndDataOddaniaIsNull(Uzytkownicy uzytkownik);

    List<Wypozyczenie> findAllByDataOddaniaIsNullOrderByDataWypozyczeniaDesc();
}