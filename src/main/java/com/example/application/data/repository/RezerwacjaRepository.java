package com.example.application.data.repository;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Rezerwacja;
import com.example.application.data.entity.StatusRezerwacji;
import com.example.application.data.entity.Uzytkownicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RezerwacjaRepository extends JpaRepository<Rezerwacja, Long> {

    List<Rezerwacja> findByUzytkownikOrderByDataRezerwacjiDesc(Uzytkownicy uzytkownik);

    // Liczy aktywne rezerwacje danego użytkownika
    @Query("select count(r) from Rezerwacja r where r.uzytkownik = :uzytkownik and r.status = :status")
    long countByUzytkownikAndStatus(@Param("uzytkownik") Uzytkownicy uzytkownik, @Param("status") StatusRezerwacji status);

    @Query("select r from Rezerwacja r join r.zarezerwowaneKsiazki zk where zk.ksiazka = :ksiazka and r.status = 'AKTYWNA'")
    Optional<Rezerwacja> findActiveReservationForBook(@Param("ksiazka") Ksiazka ksiazka);
}