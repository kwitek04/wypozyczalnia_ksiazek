package com.example.application.views.kontrolastanu;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.StanFizyczny;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.stream.Collectors;

@RolesAllowed({"MAGAZYNIER", "KIEROWNIK", "BIBLIOTEKARZ"}) // Dostęp dla obsługi
@Route(value = "kontrola-stanu", layout = MainLayout.class)
@PageTitle("Kontrola Stanu | Magazyn")
public class KontrolaStanuView extends VerticalLayout {

    private final CrmService service;
    private final Grid<Ksiazka> grid = new Grid<>(Ksiazka.class);

    public KontrolaStanuView(CrmService service) {
        this.service = service;
        setSizeFull();
        setPadding(true);

        add(new H2("Kontrola stanu fizycznego (co 5 wypożyczeń)"));
        add(new Span("Poniższe książki przekroczyły cykl wypożyczeń i wymagają weryfikacji przez magazyniera."));

        configureGrid();
        add(grid);

        updateList();
    }

    private void configureGrid() {
        grid.addClassName("kontrola-grid");
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(k -> k.getDaneKsiazki().getTytul()).setHeader("Tytuł").setAutoWidth(true);
        grid.addColumn(k -> k.getDaneKsiazki().getIsbn()).setHeader("ISBN");
        grid.addColumn(Ksiazka::getLicznikWypozyczen).setHeader("Licznik wypożyczeń").setSortable(true);
        grid.addComponentColumn(ksiazka -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setAlignItems(Alignment.BASELINE);

            Select<StanFizyczny> statusSelect = new Select<>();
            statusSelect.setItems(StanFizyczny.values());
            statusSelect.setValue(ksiazka.getStanFizyczny());
            statusSelect.setPlaceholder("Wybierz stan");
            statusSelect.setWidth("180px");

            Button confirmBtn = new Button("Zatwierdź");
            confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            confirmBtn.addClickListener(e -> {
                service.zaktualizujStanPoKontroli(ksiazka, statusSelect.getValue());
                Notification.show("Stan zaktualizowany. Książka wraca do obiegu.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateList();
            });

            actions.add(statusSelect, confirmBtn);
            return actions;
        }).setHeader("Aktualizacja stanu").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void updateList() {
        grid.setItems(service.findKsiazkiDoKontroli());
    }
}