package com.example.application.data.repository;

import com.example.application.data.entity.Tlumacz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TlumaczRepository extends JpaRepository<Tlumacz, Long> {
}