package com.example.application.data.repository;

import com.example.application.data.entity.Uzytkownik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UzytkownicyRepository extends JpaRepository<Uzytkownik, Long> {

    // Metoda do wyszukiwania użytkowników po imieniu lub nazwisku
    @Query("select u from Uzytkownik u " +
            "where lower(u.imie) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(u.nazwisko) like lower(concat('%', :searchTerm, '%'))")
    List<Uzytkownik> search(@Param("searchTerm") String searchTerm);

    Uzytkownik findByEmail(String email);

    List<Uzytkownik> findByEnabled(boolean enabled);
}