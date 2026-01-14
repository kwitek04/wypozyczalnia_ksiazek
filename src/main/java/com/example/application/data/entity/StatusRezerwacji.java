package com.example.application.data.entity;

public enum StatusRezerwacji {
    AKTYWNA("Aktywna"),
    ZREALIZOWANA("Odebrana"),
    ANULOWANA("Anulowana"),
    PRZETERMINOWANA("Przeterminowana");

    private final String nazwa;

    StatusRezerwacji(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getNazwa() {
        return nazwa;
    }
}