package com.example.application.data.entity;

public enum StatusKsiazki {
    DOSTEPNA("Dostępna"),
    WYPOZYCZONA("Wypożyczona"),
    WYCOFANA("Wycofana"),
    W_RENOWACJI("W renowacji"),
    W_KONTROLI("W kontroli"),
    DO_ODLOZENIA("Do odłożenia"),
    ZAREZERWOWANA("Zarezerwowana");

    private final String name;

    StatusKsiazki(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}