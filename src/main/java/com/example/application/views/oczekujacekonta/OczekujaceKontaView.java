package com.example.application.views.oczekujacekonta;

import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"KIEROWNIK", "BIBLIOTEKARZ"})
@Route(value = "aktywacja", layout = MainLayout.class)
public class OczekujaceKontaView extends VerticalLayout {

    Grid<Uzytkownicy> grid = new Grid<>(Uzytkownicy.class);
    CrmService service;

    public OczekujaceKontaView(CrmService service) {
        this.service = service;
        setSizeFull();

        grid.setColumns("imie", "nazwisko", "email", "dataUrodzenia");

        // Przycisk AKTYWUJ
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
        // Musisz dodać metodę findAllPending() w CrmService
        grid.setItems(service.findAllPendingUzytkownicy());
    }
}