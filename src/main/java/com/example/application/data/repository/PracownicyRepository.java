package com.example.application.data.repository;

import com.example.application.data.entity.Pracownicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PracownicyRepository extends JpaRepository<Pracownicy, Long> {

    @Query("select p from Pracownicy p " +
            "where lower(p.imie) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.nazwisko) like lower(concat('%', :searchTerm, '%'))")
    List<Pracownicy> search(@Param("searchTerm") String searchTerm);

    Pracownicy findByEmail(String email);
}