package com.example.application.data.entity;

public enum StanFizyczny {
    BARDZO_DOBRY("Bardzo dorby"),
    DOBRY("Dobry"),
    SREDNI("Średni"),
    DO_RENOWACJI("Do renowacji"),
    DO_WYCOFANIA("Do wycofania");

    private final String nazwa;

    StanFizyczny(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getNazwa() {
        return nazwa;
    }

    @Override
    public String toString() {
        return nazwa;
    }
}