package com.example.application.views.wypozyczenia;

import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.entity.Wypozyczenie;
import com.example.application.data.service.LibraryService;
import com.example.application.security.SecurityService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@RolesAllowed({"USER"})
@Route(value = "moje-wypozyczenia", layout = MainLayout.class)
@PageTitle("Moje Wypożyczenia | Biblioteka")
public class MojeWypozyczeniaView extends VerticalLayout {

    private final LibraryService service;
    private final SecurityService securityService;
    private final Grid<Wypozyczenie> grid = new Grid<>(Wypozyczenie.class);

    public MojeWypozyczeniaView(LibraryService service, SecurityService securityService) {
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

        grid.addColumn(wypozyczenie -> {
            return wypozyczenie.getWypozyczoneKsiazki().stream()
                    .map(wk -> wk.getKsiazka().getDaneKsiazki().getTytul())
                    .collect(Collectors.joining(", "));
        }).setHeader("Książka").setSortable(true).setAutoWidth(true);

        grid.addColumn(wypozyczenie -> {
            return wypozyczenie.getWypozyczoneKsiazki().stream()
                    .map(wk -> wk.getKsiazka().getDaneKsiazki().getIsbn())
                    .collect(Collectors.joining(", "));
        }).setHeader("ISBN").setSortable(true).setAutoWidth(true);


        grid.addColumn(Wypozyczenie::getDataWypozyczenia)
                .setHeader("Data wypożyczenia")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addComponentColumn(wypozyczenie -> {
            LocalDate termin = wypozyczenie.getTerminZwrotu();
            LocalDate oddano = wypozyczenie.getDataOddania();
            Span badge = new Span(termin.toString());

            if (oddano == null) {
                long daysOverdue = ChronoUnit.DAYS.between(termin, LocalDate.now());
                if (daysOverdue > 0) {
                    badge.getElement().getThemeList().add("badge error");
                } else {
                    badge.getElement().getThemeList().add("badge contrast");
                }
            } else {
                badge.getElement().getThemeList().add("badge success");
            }
            return badge;
        }).setHeader("Termin zwrotu").setSortable(true).setComparator(Wypozyczenie::getTerminZwrotu).setAutoWidth(true);

        grid.addColumn(w -> w.getDataOddania() != null ? w.getDataOddania().toString() : "-")
                .setHeader("Data oddania")
                .setSortable(true)
                .setComparator((w1, w2) -> {
                    if (w1.getDataOddania() == null && w2.getDataOddania() == null) return 0;
                    if (w1.getDataOddania() == null) return 1;
                    if (w2.getDataOddania() == null) return -1;
                    return w1.getDataOddania().compareTo(w2.getDataOddania());
                })
                .setAutoWidth(true);

        grid.addComponentColumn(wypozyczenie -> {
            if (wypozyczenie.getDataOddania() != null) {
                Span span = new Span("Zwrócono");
                span.getElement().getThemeList().add("badge success");
                return span;
            }
            else if (wypozyczenie.isZwrotZgloszony()) {
                Span span = new Span("Zgłoszono zwrot");
                span.getElement().getThemeList().add("badge warning");
                return span;
            }
            else {
                Span span = new Span("Wypożyczona");
                span.getElement().getThemeList().add("badge");
                return span;
            }
        })
                .setHeader("Status")
                .setSortable(true)
                .setAutoWidth(true)
                .setComparator((w1, w2) -> {
                    int waga1 = getStatusWeight(w1);
                    int waga2 = getStatusWeight(w2);

                    return Integer.compare(waga1, waga2);
                });

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addItemClickListener(event -> {
            Wypozyczenie wyp = event.getItem();
            WypozyczenieDetailsDialog dialog = new WypozyczenieDetailsDialog(wyp, service);

            dialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    updateList();
                }
            });

            dialog.open();
        });
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

    private int getStatusWeight(Wypozyczenie w) {
        if (w.getDataOddania() != null) {
            return 3; // Zwrócono
        } else if (w.isZwrotZgloszony()) {
            return 2; // Zgłoszono zwrot
        } else {
            return 1; // Wypożyczona
        }
    }
}