package com.example.application.data.entity;

import com.example.application.data.repository.AbstractEntity;
import jakarta.persistence.Entity;

@Entity
public class Rola extends AbstractEntity {
    private String name;

    public Rola() {
    }

    public Rola(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}