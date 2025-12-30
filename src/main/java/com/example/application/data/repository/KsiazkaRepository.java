package com.example.application.data.repository;

import com.example.application.data.entity.Ksiazka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface KsiazkaRepository extends JpaRepository<Ksiazka, Long> {

    @Query("select distinct k from Ksiazka k " +
            "join k.daneKsiazki d " +
            "left join d.autorzy a " +
            "where lower(d.tytul) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(d.isbn) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(a.imie) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(a.nazwisko) like lower(concat('%', :searchTerm, '%'))")
    List<Ksiazka> search(@Param("searchTerm") String searchTerm);
}