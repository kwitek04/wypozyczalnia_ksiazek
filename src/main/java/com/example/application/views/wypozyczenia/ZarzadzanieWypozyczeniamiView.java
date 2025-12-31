package com.example.application.views.wypozyczenia;

import com.example.application.data.entity.Wypozyczenie;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RolesAllowed({"KIEROWNIK", "BIBLIOTEKARZ"}) // Dostęp tylko dla pracowników
@Route(value = "zarzadzanie-wypozyczeniami", layout = MainLayout.class)
@PageTitle("Zarządzanie Wypożyczeniami | Biblioteka")
public class ZarzadzanieWypozyczeniamiView extends VerticalLayout {

    private final CrmService service;
    private final Grid<Wypozyczenie> grid = new Grid<>(Wypozyczenie.class);
    private final TextField searchField = new TextField();

    public ZarzadzanieWypozyczeniamiView(CrmService service) {
        this.service = service;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Panel zwrotów i wypożyczeń"));

        configureSearchField();
        add(searchField);

        configureGrid();
        add(grid);

        updateList();
    }

    private void configureSearchField() {
        searchField.setPlaceholder("Szukaj po użytkowniku, tytule lub ISBN...");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());
        searchField.setWidth("50%");
    }

    private void configureGrid() {
        grid.addClassName("admin-wypozyczenia-grid");
        grid.setSizeFull();
        grid.removeAllColumns(); // Czyścimy domyślne kolumny

        // 1. Użytkownik (Kto wypożyczył)
        grid.addColumn(w -> w.getUzytkownik().getImie() + " " + w.getUzytkownik().getNazwisko())
                .setHeader("Użytkownik")
                .setSortable(true)
                .setAutoWidth(true);

        // 2. Tytuł książki
        grid.addColumn(w -> w.getWypozyczoneKsiazki().stream()
                        .map(wk -> wk.getKsiazka().getDaneKsiazki().getTytul())
                        .collect(Collectors.joining(", ")))
                .setHeader("Tytuł")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(w -> w.getWypozyczoneKsiazki().stream()
                        .map(wk -> wk.getKsiazka().getDaneKsiazki().getIsbn())
                        .collect(Collectors.joining(", ")))
                .setHeader("ISBN")
                .setSortable(true)
                .setAutoWidth(true);

        // 3. Data wypożyczenia
        grid.addColumn(Wypozyczenie::getDataWypozyczenia)
                .setHeader("Data wypożyczenia")
                .setSortable(true)
                .setAutoWidth(true);

        // 4. Termin zwrotu (z wyróżnieniem opóźnień)
        grid.addComponentColumn(w -> {
            Span s = new Span(w.getTerminZwrotu().toString());
            // Jeśli dzisiaj jest po terminie -> kolor czerwony
            if (LocalDate.now().isAfter(w.getTerminZwrotu())) {
                s.getElement().getThemeList().add("badge error");
                s.setText(s.getText() + " (Przetrzymana!)");
            }
            return s;
        }).setHeader("Termin zwrotu").setSortable(true).setAutoWidth(true);

        // 5. Status zgłoszenia (Tutaj widać, czy klient kliknął "Zgłoś zwrot")
        grid.addComponentColumn(w -> {
                    if (w.isZwrotZgloszony()) {
                        Span s = new Span("ZGŁOSZONO ZWROT");
                        s.getElement().getThemeList().add("badge warning primary");
                        return s;
                    }
                    return new Span("");
                }).setHeader("Akcja użytkownika")
                .setSortable(true)
                // Sortujemy tak, żeby zgłoszone zwroty (true) były na górze
                .setComparator((w1, w2) -> Boolean.compare(w2.isZwrotZgloszony(), w1.isZwrotZgloszony()))
                .setAutoWidth(true);

        // Stylizacja i obsługa kliknięcia
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // Kliknięcie w wiersz otwiera dialog, w którym pracownik może kliknąć "Zatwierdź zwrot"
        grid.addItemClickListener(e -> {
            WypozyczenieDetailsDialog dialog = new WypozyczenieDetailsDialog(e.getItem(), service);
            // Dodajemy listener na zamknięcie dialogu, żeby odświeżyć listę (np. jak zniknie oddana książka)
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    updateList();
                }
            });
            dialog.open();
        });
    }

    private void updateList() {
        List<Wypozyczenie> allActive = service.findAllActiveWypozyczenia();
        String filter = searchField.getValue().toLowerCase();

        if (filter.isEmpty()) {
            grid.setItems(allActive);
        } else {
            // Filtrowanie w pamięci (Java Stream)
            List<Wypozyczenie> filtered = allActive.stream()
                    .filter(w -> {
                        String userFull = (w.getUzytkownik().getImie() + " " + w.getUzytkownik().getNazwisko()).toLowerCase();
                        String titles = w.getWypozyczoneKsiazki().stream()
                                .map(wk -> wk.getKsiazka().getDaneKsiazki().getTytul().toLowerCase())
                                .collect(Collectors.joining(" "));
                        String isbns = w.getWypozyczoneKsiazki().stream()
                                .map(wk -> wk.getKsiazka().getDaneKsiazki().getIsbn().toLowerCase())
                                .collect(Collectors.joining(" "));

                        return userFull.contains(filter) || titles.contains(filter) || isbns.contains(filter);
                    })
                    .collect(Collectors.toList());
            grid.setItems(filtered);
        }
    }
}