package com.example.application.views.odlozenie;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.service.LibraryService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"MAGAZYNIER", "KIEROWNIK"})
@Route(value = "do-odlozenia", layout = MainLayout.class)
@PageTitle("Książki do odłożenia | Magazyn")
public class KsiazkiDoOdlozeniaView extends VerticalLayout {

    private final LibraryService service;
    private final Grid<Ksiazka> grid = new Grid<>(Ksiazka.class);

    public KsiazkiDoOdlozeniaView(LibraryService service) {
        this.service = service;
        setSizeFull();
        setPadding(true);

        add(new H2("Książki do odłożenia na półkę"));
        add(new Span("Lista książek, które zostały zwrócone i oczekują na odłożenie na półkę."));

        configureGrid();
        add(grid);

        updateList();
    }

    private void configureGrid() {
        grid.addClassName("odlozenie-grid");
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(k -> k.getDaneKsiazki().getTytul()).setHeader("Tytuł").setAutoWidth(true);
        grid.addColumn(k -> k.getDaneKsiazki().getIsbn()).setHeader("ISBN");

        grid.addColumn(k -> {
            if (k.getPoddziedzina() != null) {
                return k.getPoddziedzina().getDziedzina().getNazwa() + " > " + k.getPoddziedzina().getNazwa();
            }
            return "Brak lokalizacji";
        }).setHeader("Lokalizacja na półce").setAutoWidth(true);

        grid.addComponentColumn(ksiazka -> {
            Button confirmBtn = new Button("Potwierdź odłożenie");
            confirmBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

            confirmBtn.addClickListener(e -> {
                service.potwierdzOdlozenie(ksiazka);
                Notification.show("Książka dostępna w systemie.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateList();
            });

            return confirmBtn;
        }).setHeader("Akcja").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void updateList() {
        grid.setItems(service.findKsiazkiDoOdlozenia());
    }
}