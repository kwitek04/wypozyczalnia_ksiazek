package com.example.application.data.repository;

import com.example.application.data.entity.Uzytkownicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UzytkownicyRepository extends JpaRepository<Uzytkownicy, Long> {

    // Metoda do wyszukiwania użytkowników po imieniu lub nazwisku
    @Query("select u from Uzytkownicy u " +
            "where lower(u.imie) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(u.nazwisko) like lower(concat('%', :searchTerm, '%'))")
    List<Uzytkownicy> search(@Param("searchTerm") String searchTerm);

    // Metoda potrzebna do logowania (jeśli zdecydujesz się je włączyć dla czytelników)
    Uzytkownicy findByEmail(String email);

    List<Uzytkownicy> findByEnabled(boolean enabled);
}