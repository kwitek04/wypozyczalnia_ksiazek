package com.example.application.data.repository;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.entity.Wypozyczenie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WypozyczenieRepository extends JpaRepository<Wypozyczenie, Long> {
    List<Wypozyczenie> findAllByUzytkownikOrderByDataWypozyczeniaDesc(Uzytkownicy uzytkownik);

    long countByUzytkownikAndDataOddaniaIsNull(Uzytkownicy uzytkownik);

    List<Wypozyczenie> findAllByDataOddaniaIsNullOrderByDataWypozyczeniaDesc();

    long countByDataWypozyczeniaBetween(java.time.LocalDate start, java.time.LocalDate end);

    long countByDataOddaniaBetween(java.time.LocalDate start, java.time.LocalDate end);

    @Query("select count(distinct w.uzytkownik) from Wypozyczenie w where w.dataWypozyczenia between :start and :end")
    long countUniqueUsersByDataWypozyczeniaBetween(@Param("start") java.time.LocalDate start, @Param("end") java.time.LocalDate end);

    List<Wypozyczenie> findAllByUzytkownik(Uzytkownicy uzytkownik);
}