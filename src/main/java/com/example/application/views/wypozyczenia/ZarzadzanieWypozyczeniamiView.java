package com.example.application.views.wypozyczenia;

import com.example.application.data.entity.Wypozyczenie;
import com.example.application.data.service.RentalService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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

/**
 * Widok operacyjny przeznaczony wyłącznie dla pracowników z rolą BIBLIOTEKARZ.
 * Służy do zarządzania bieżącymi wypożyczeniami:
 * - Przeglądania listy aktywnych wypożyczeń,
 * - Wyszukiwania wypożyczeń po użytkowniku, tytule lub ISBN,
 * - Obsługi zgłoszonych zwrotów.
 */

@RolesAllowed({"BIBLIOTEKARZ"})
@Route(value = "zarzadzanie-wypozyczeniami", layout = MainLayout.class)
@PageTitle("Zarządzanie Wypożyczeniami | Biblioteka")
public class ZarzadzanieWypozyczeniamiView extends VerticalLayout {

    private final RentalService rentalService;
    private final Grid<Wypozyczenie> grid = new Grid<>(Wypozyczenie.class);
    private final TextField searchField = new TextField();

    public ZarzadzanieWypozyczeniamiView(RentalService rentalService) {
        this.rentalService = rentalService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureSearchField();
        add(searchField);

        configureGrid();
        add(grid);

        updateList();
    }

    private void configureSearchField() {
        searchField.setPlaceholder("Wyszukaj...");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());
        searchField.setWidth("50%");
    }

    private void configureGrid() {
        grid.addClassName("admin-wypozyczenia-grid");
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(w -> w.getUzytkownik().getImie() + " " + w.getUzytkownik().getNazwisko())
                .setHeader("Użytkownik")
                .setSortable(true)
                .setAutoWidth(true);

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

        grid.addColumn(Wypozyczenie::getDataWypozyczenia)
                .setHeader("Data wypożyczenia")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addComponentColumn(w -> {
            Span s = new Span(w.getTerminZwrotu().toString());
            if (LocalDate.now().isAfter(w.getTerminZwrotu())) {
                s.getElement().getThemeList().add("badge error");
                s.setText(s.getText() + " (Przetrzymana)");
            }
            return s;
        }).setHeader("Termin zwrotu").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(w -> {
                    if (w.isZwrotZgloszony()) {
                        Span s = new Span("ZGŁOSZONO ZWROT");
                        s.getElement().getThemeList().add("badge warning primary");
                        return s;
                    }
                    return new Span("");
                }).setHeader("Akcja użytkownika")
                .setSortable(true)
                .setComparator((w1, w2) -> Boolean.compare(w2.isZwrotZgloszony(), w1.isZwrotZgloszony()))
                .setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addItemClickListener(e -> {
            WypozyczenieDetailsDialog dialog = new WypozyczenieDetailsDialog(e.getItem(), rentalService);
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    updateList();
                }
            });
            dialog.open();
        });
    }

    private void updateList() {
        List<Wypozyczenie> allActive = rentalService.findAllActiveWypozyczenia();
        String filter = searchField.getValue().toLowerCase();

        if (filter.isEmpty()) {
            grid.setItems(allActive);
        } else {
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