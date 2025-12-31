package com.example.application.views.wypozyczenia;

import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.entity.Wypozyczenie;
import com.example.application.data.entity.WypozyczonaKsiazka;
import com.example.application.data.service.CrmService;
import com.example.application.security.SecurityService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@PermitAll
@Route(value = "moje-wypozyczenia", layout = MainLayout.class)
@PageTitle("Moje Wypożyczenia | Biblioteka")
public class MojeWypozyczeniaView extends VerticalLayout {

    private final CrmService service;
    private final SecurityService securityService;
    private final Grid<Wypozyczenie> grid = new Grid<>(Wypozyczenie.class);

    public MojeWypozyczeniaView(CrmService service, SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Historia wypożyczeń"));
        configureGrid();
        add(grid);

        updateList();
    }

    private void configureGrid() {
        grid.addClassName("wypozyczenia-grid");
        grid.setSizeFull();
        grid.removeAllColumns(); // Usuwamy domyślne kolumny

        // 1. Tytuły książek (z listy wypożyczonych pozycji)
        grid.addColumn(wypozyczenie -> {
            return wypozyczenie.getWypozyczoneKsiazki().stream()
                    .map(wk -> wk.getKsiazka().getDaneKsiazki().getTytul())
                    .collect(Collectors.joining(", "));
        }).setHeader("Książka").setAutoWidth(true);

        // 2. Data wypożyczenia
        grid.addColumn(Wypozyczenie::getDataWypozyczenia).setHeader("Data wypożyczenia").setAutoWidth(true);

        // 3. Termin zwrotu (z logiką kolorów)
        grid.addComponentColumn(wypozyczenie -> {
            LocalDate termin = wypozyczenie.getTerminZwrotu();
            LocalDate oddano = wypozyczenie.getDataOddania();
            Span badge = new Span(termin.toString());

            if (oddano == null) {
                // Jeszcze nie oddano
                long daysOverdue = ChronoUnit.DAYS.between(termin, LocalDate.now());
                if (daysOverdue > 0) {
                    // Przetrzymana!
                    badge.setText(termin + " (Opóźnienie: " + daysOverdue + " dni)");
                    badge.getElement().getThemeList().add("badge error");
                } else {
                    // W terminie
                    badge.getElement().getThemeList().add("badge contrast");
                }
            } else {
                // Oddano - data jest historyczna
                badge.getElement().getThemeList().add("badge success");
            }
            return badge;
        }).setHeader("Termin zwrotu").setAutoWidth(true);

        // 4. Status
        grid.addComponentColumn(wypozyczenie -> {
            if (wypozyczenie.getDataOddania() != null) {
                Span span = new Span("Zwrócono");
                span.getElement().getThemeList().add("badge success");
                return span;
            } else {
                Span span = new Span("Wypożyczona");
                span.getElement().getThemeList().add("badge");
                return span;
            }
        }).setHeader("Status").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void updateList() {
        UserDetails userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            Uzytkownicy u = service.findUzytkownikByEmail(userDetails.getUsername());
            if (u != null) {
                grid.setItems(service.findWypozyczeniaByUser(u));
            }
        }
    }
}