package com.example.application.data.repository;

import com.example.application.data.entity.Pracownik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracownikRepository extends JpaRepository<Pracownik, Long> {

    @Query("select p from Pracownik p " +
            "where lower(p.imie) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.nazwisko) like lower(concat('%', :searchTerm, '%'))")
    List<Pracownik> search(@Param("searchTerm") String searchTerm);

    Pracownik findByEmail(String email);
}