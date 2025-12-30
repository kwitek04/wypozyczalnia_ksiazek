package com.example.application.views;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Tlumacz;
import com.example.application.data.service.CrmService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Set;
import java.util.stream.Collectors;

@AnonymousAllowed
@Route(value = "", layout = MainLayout.class)
@PageTitle("Strona Główna | Biblioteka")
public class HomeView extends VerticalLayout {

    private final CrmService service;
    private final Grid<Ksiazka> grid = new Grid<>(Ksiazka.class);
    private final TextField searchField = new TextField();

    public HomeView(CrmService service) {
        this.service = service;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        addClassName("home-view");

        // Pasek wyszukiwania
        add(createSearchBar());

        // Konfiguracja Grida
        configureGrid();
        add(grid);

        // Ładowanie danych
        updateList();
    }

    private HorizontalLayout createSearchBar() {
        searchField.setPlaceholder("Szukaj po tytule, autorze, ISBN...");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("60%");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        Button searchButton = new Button("Szukaj", e -> updateList());
        searchButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        searchField.addKeyPressListener(Key.ENTER, e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, searchButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.CENTER);
        toolbar.setAlignItems(Alignment.BASELINE);
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("katalog-grid");
        grid.setSizeFull();
        grid.removeAllColumns();

        // TWORZYMY JEDNĄ "SUPER KOLUMNĘ" Z WŁASNYM UKŁADEM
        grid.addComponentColumn(ksiazka -> createBookCard(ksiazka));

        // Usuwamy nagłówek tabeli
        grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_NO_BORDER);
    }

    private HorizontalLayout createBookCard(Ksiazka ksiazka) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("book-card");
        card.setSpacing(true);
        card.setPadding(true);
        card.setWidthFull();
        card.setAlignItems(Alignment.START);

        // 1. ZDJĘCIE
        Image coverImage;
        byte[] okladka = ksiazka.getDaneKsiazki().getOkladka();

        if (okladka != null && okladka.length > 0) {
            // Tworzymy zasób z bajtów pobranych z bazy
            StreamResource resource = new StreamResource("cover_" + ksiazka.getId(), () -> new java.io.ByteArrayInputStream(okladka));
            coverImage = new Image(resource, "Okładka książki");
        } else {
            // Placeholder, jeśli brak okładki
            coverImage = new Image("https://placehold.co/100x150?text=Brak+okładki", "Brak okładki");
        }

        // Style dla zdjęcia (bez zmian)
        coverImage.setWidth("100px");
        coverImage.setHeight("150px");
        coverImage.getStyle().set("border-radius", "5px");
        coverImage.getStyle().set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");
        coverImage.getStyle().set("object-fit", "cover"); // Ważne: przycina zdjęcie, żeby nie było rozciągnięte

        // 2. DANE KSIĄŻKI (Środek)
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        // Tytuł
        H3 tytul = new H3(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin-top", "0");
        tytul.getStyle().set("margin-bottom", "5px"); // Lekki odstęp pod tytułem
        tytul.getStyle().set("color", "#2c3e50");

        // Autorzy
        String autorzyStr = "Brak autora";
        if (ksiazka.getDaneKsiazki().getAutorzy() != null && !ksiazka.getDaneKsiazki().getAutorzy().isEmpty()) {
            autorzyStr = ksiazka.getDaneKsiazki().getAutorzy().stream()
                    .map(a -> a.getImie() + " " + a.getNazwisko())
                    .collect(Collectors.joining(", "));
        }
        Span autor = new Span(autorzyStr);
        autor.getStyle().set("font-weight", "bold");
        autor.getStyle().set("color", "#7f8c8d");

        // --- ZMIANA TUTAJ ---
        // Linia 1: Wydawnictwo i Rok
        Span wydawnictwoRok = new Span(
                ksiazka.getDaneKsiazki().getWydawnictwo() + " • " +
                        ksiazka.getDaneKsiazki().getRokWydania()
        );
        wydawnictwoRok.getStyle().set("font-size", "0.9em");
        wydawnictwoRok.getStyle().set("color", "#95a5a6");

        // Linia 2: ISBN (teraz w nowej linii)
        Span isbn = new Span("ISBN: " + ksiazka.getDaneKsiazki().getIsbn());
        isbn.getStyle().set("font-size", "0.9em");
        isbn.getStyle().set("color", "#95a5a6");
        // ---------------------

        Span tlumaczSpan = new Span();
        Set<Tlumacz> tlumaczeList = ksiazka.getDaneKsiazki().getTlumacze();

        if (tlumaczeList != null && !tlumaczeList.isEmpty()) {
            String tlumaczeStr = tlumaczeList.stream()
                    .map(t -> t.getImie() + " " + t.getNazwisko())
                    .collect(Collectors.joining(", "));

            tlumaczSpan.setText("Tłumacz: " + tlumaczeStr);
            tlumaczSpan.getStyle().set("font-size", "0.9em");
            tlumaczSpan.getStyle().set("color", "#95a5a6"); // Nieco ciemniejszy szary
        }

        // Kategoria
        String kategoriaStr = "-";
        if (ksiazka.getPoddziedzina() != null) {
            kategoriaStr = ksiazka.getPoddziedzina().getDziedzina().getNazwa() + " > " + ksiazka.getPoddziedzina().getNazwa();
        }
        Span kategoria = new Span(kategoriaStr);
        kategoria.getElement().getThemeList().add("badge");
        kategoria.getElement().getThemeList().add("contrast");
        kategoria.getStyle().set("margin-top", "10px");

        // Dodajemy wszystko do układu (kolejność ma znaczenie)
        details.add(tytul, autor, wydawnictwoRok, isbn, tlumaczSpan, kategoria);

        // 3. STATUS (Prawy dolny róg)
        Div spacer = new Div();
        card.setFlexGrow(1, spacer);

        Span statusBadge = createStatusBadge(ksiazka);

        VerticalLayout rightSide = new VerticalLayout(spacer, statusBadge);
        rightSide.setSpacing(false);
        rightSide.setPadding(false);
        rightSide.setHeight("150px");
        rightSide.setAlignItems(Alignment.END);
        rightSide.setJustifyContentMode(JustifyContentMode.END);

        card.add(coverImage, details, spacer, rightSide);
        return card;
    }

    private Span createStatusBadge(Ksiazka ksiazka) {
        String statusName = ksiazka.getStatus().getName();
        boolean dostepna = "Dostępna".equalsIgnoreCase(statusName);

        String text = dostepna ? "Dostępna" : "Niedostępna";
        Span badge = new Span(text);

        badge.getElement().getThemeList().add("badge");
        if (dostepna) {
            badge.getElement().getThemeList().add("success");
        } else {
            badge.getElement().getThemeList().add("error");
        }

        badge.getStyle().set("font-size", "0.9em");
        badge.getStyle().set("padding", "0.5em 1em");

        return badge;
    }

    private void updateList() {
        grid.setItems(service.findKsiazkiBySearch(searchField.getValue()));
    }
}