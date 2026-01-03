package com.example.application.data.repository;

import com.example.application.data.entity.*;
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
    List<Ksiazka> searchAll(@Param("searchTerm") String searchTerm);

    @Query("select distinct k from Ksiazka k " +
            "join k.daneKsiazki d " +
            "left join d.autorzy a " +
            "where (k.status <> :excludedStatus) and " + // <-- WYKLUCZENIE
            "(lower(d.tytul) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(d.isbn) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(a.imie) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(a.nazwisko) like lower(concat('%', :searchTerm, '%')))")
    List<Ksiazka> searchWithExclusion(@Param("searchTerm") String searchTerm, @Param("excludedStatus") StatusKsiazki excludedStatus);

    List<Ksiazka> findByStatusNot(StatusKsiazki status);

    // 3. Filtrowanie po poddziedzinie (z wykluczeniem)
    List<Ksiazka> findByPoddziedzinaAndStatusNot(Poddziedzina poddziedzina, StatusKsiazki status);

    // 4. Filtrowanie po dziedzinie (z wykluczeniem)
    @Query("select k from Ksiazka k where k.poddziedzina.dziedzina = :dziedzina and k.status <> :excludedStatus")
    List<Ksiazka> findByDziedzinaAndStatusNot(@Param("dziedzina") Dziedzina dziedzina, @Param("excludedStatus") StatusKsiazki excludedStatus);

    // 5. Filtrowanie po autorze (z wykluczeniem)
    @Query("select k from Ksiazka k join k.daneKsiazki d join d.autorzy a where a = :autor and k.status <> :excludedStatus")
    List<Ksiazka> findByAutorAndStatusNot(@Param("autor") Autor autor, @Param("excludedStatus") StatusKsiazki excludedStatus);

    List<Ksiazka> findByWymagaKontroliTrueAndStatus(StatusKsiazki status);

    List<Ksiazka> findByStanFizycznyAndStatusNot(StanFizyczny stan, StatusKsiazki status);
}