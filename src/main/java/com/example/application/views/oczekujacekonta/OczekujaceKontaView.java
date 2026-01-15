package com.example.application.views.oczekujacekonta;

import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.service.LibraryService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"KIEROWNIK", "BIBLIOTEKARZ"})
@Route(value = "aktywacja", layout = MainLayout.class)
public class OczekujaceKontaView extends VerticalLayout {

    Grid<Uzytkownicy> grid = new Grid<>(Uzytkownicy.class);
    LibraryService service;

    public OczekujaceKontaView(LibraryService service) {
        this.service = service;
        setSizeFull();

        grid.setColumns("imie", "nazwisko", "email", "dataUrodzenia");

        grid.addComponentColumn(uzytkownik -> {
            Button activeBtn = new Button("Aktywuj", e -> {
                uzytkownik.setEnabled(true);
                service.saveUzytkownik(uzytkownik);
                updateList();
                Notification.show("Konto aktywowane!");
            });
            activeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            return activeBtn;
        });

        add(new H2("Konta oczekujące na weryfikację"), grid);
        updateList();
    }

    private void updateList() {
        grid.setItems(service.findAllPendingUzytkownicy());
    }
}