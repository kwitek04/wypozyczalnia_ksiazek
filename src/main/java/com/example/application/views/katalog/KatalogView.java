package com.example.application.views.katalog;

import com.example.application.data.entity.Autor;
import com.example.application.data.entity.Dziedzina;
import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Poddziedzina;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.security.SecurityService;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@AnonymousAllowed
@Route(value = "katalog", layout = MainLayout.class)
@PageTitle("Katalog | Biblioteka")
public class KatalogView extends VerticalLayout {

    private final CrmService service;
    private final SecurityService securityService;
    private Uzytkownicy currentUser;

    // Komponenty interfejsu
    private final TreeGrid<Object> categoryTree = new TreeGrid<>();
    private final Grid<Autor> authorGrid = new Grid<>(Autor.class);
    private final Grid<Ksiazka> bookGrid = new Grid<>(Ksiazka.class);
    private final TextField authorFilter = new TextField();

    public KatalogView(CrmService service, SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
        loadCurrentUser();
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("katalog-view");

        // 1. Konfiguracja lewego panelu (Kategorie i Autorzy)
        configureCategoryTree();
        configureAuthorGrid();

        // Kontener na lewą stronę (zmienna zawartość)
        Div leftPanelContent = new Div();
        leftPanelContent.setSizeFull();
        leftPanelContent.add(categoryTree);

        // Zakładki
        Tab tabKategorie = new Tab(VaadinIcon.FOLDER.create(), new Span(" Kategorie"));
        Tab tabAutorzy = new Tab(VaadinIcon.USER.create(), new Span(" Autorzy"));

        Tabs tabs = new Tabs(tabKategorie, tabAutorzy);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(event -> {
            leftPanelContent.removeAll();
            if (event.getSelectedTab().equals(tabKategorie)) {
                leftPanelContent.add(categoryTree);
                updateBookList(null);
                categoryTree.asSingleSelect().clear();
            } else {
                VerticalLayout authorLayout = new VerticalLayout(authorFilter, authorGrid);
                authorLayout.setPadding(false);
                authorLayout.setSpacing(true);
                authorLayout.setSizeFull();
                authorFilter.setWidthFull();

                leftPanelContent.add(authorLayout);
                updateBookList(null);
                authorGrid.asSingleSelect().clear();
            }
        });

        // Lewy Layout (Tabs + Content) - STAŁA SZEROKOŚĆ
        VerticalLayout leftSide = new VerticalLayout(tabs, leftPanelContent);
        leftSide.setPadding(false);
        leftSide.setSpacing(false);
        leftSide.setHeightFull();
        leftSide.setWidth("350px");     // Sztywna szerokość
        leftSide.setMinWidth("350px");  // Blokada zmniejszania
        leftSide.setMaxWidth("350px");  // Blokada zwiększania
        // Dodajemy ramkę z prawej strony, żeby oddzielić od książek
        leftSide.getStyle().set("border-right", "1px solid #e0e0e0");

        // 2. Konfiguracja prawej strony (Lista książek)
        configureBookGrid();

        // 3. Główny układ (HorizontalLayout zamiast SplitLayout)
        HorizontalLayout mainLayout = new HorizontalLayout(leftSide, bookGrid);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false); // Brak odstępów między panelem a gridem
        // bookGrid zajmie całą pozostałą przestrzeń
        mainLayout.setFlexGrow(1, bookGrid);

        add(mainLayout);

        // Załaduj dane na start
        updateBookList(null);
    }

    private void loadCurrentUser() {
        UserDetails userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            // Szukamy uzytkownika w bazie po emailu
            this.currentUser = service.findUzytkownikByEmail(userDetails.getUsername());
        } else {
            this.currentUser = null;
        }
    }

    // --- LEWY PANEL: KATEGORIE ---
    private void configureCategoryTree() {
        categoryTree.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        categoryTree.setSizeFull();

        TreeData<Object> treeData = new TreeData<>();
        List<Dziedzina> dziedziny = service.findAllDziedziny();
        dziedziny.sort(Comparator.comparing(Dziedzina::getNazwa));

        for (Dziedzina d : dziedziny) {
            treeData.addItem(null, d);
            List<Poddziedzina> poddziedziny = d.getPoddziedziny();
            poddziedziny.sort(Comparator.comparing(Poddziedzina::getNazwa));
            for (Poddziedzina p : poddziedziny) {
                treeData.addItem(d, p);
            }
        }
        categoryTree.setDataProvider(new TreeDataProvider<>(treeData));

        categoryTree.addHierarchyColumn(item -> {
            if (item instanceof Dziedzina) return ((Dziedzina) item).getNazwa();
            if (item instanceof Poddziedzina) return ((Poddziedzina) item).getNazwa();
            return item.toString();
        }).setHeader("Wybierz kategorię");

        categoryTree.asSingleSelect().addValueChangeListener(e -> updateBookList(e.getValue()));
    }

    // --- LEWY PANEL: AUTORZY (Z SORTOWANIEM) ---
    private void configureAuthorGrid() {
        authorGrid.setSizeFull();
        authorGrid.removeAllColumns();
        authorGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        // Wyświetlanie: Nazwisko Imię (czytelniej przy sortowaniu po nazwisku)
        authorGrid.addColumn(a -> a.getNazwisko() + " " + a.getImie()).setHeader("Nazwisko i Imię");

        // Pobieramy wszystkich autorów i SORTUJEMY
        List<Autor> allAuthors = service.findAllAutorzy();
        // Sortowanie: Najpierw Nazwisko, potem Imię
        allAuthors.sort(Comparator.comparing(Autor::getNazwisko)
                .thenComparing(Autor::getImie));

        authorGrid.setItems(allAuthors);

        // Filtrowanie autorów
        authorFilter.setPlaceholder("Szukaj autora...");
        authorFilter.setValueChangeMode(ValueChangeMode.LAZY);
        authorFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        authorFilter.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();
            List<Autor> filtered = allAuthors.stream()
                    .filter(a -> (a.getNazwisko() + " " + a.getImie()).toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            authorGrid.setItems(filtered);
        });

        authorGrid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                List<Ksiazka> books = service.findKsiazkiByAutor(e.getValue());
                bookGrid.setItems(books);
            }
        });
    }

    // --- PRAWY PANEL: KSIĄŻKI ---
    private void configureBookGrid() {
        bookGrid.addClassName("katalog-books");
        bookGrid.setSizeFull();
        bookGrid.removeAllColumns();
        bookGrid.addComponentColumn(this::createBookCard);
        bookGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_NO_BORDER);

        bookGrid.addItemClickListener(event -> {
            Ksiazka clickedBook = event.getItem();
            // Otwieramy dialog ze szczegółami
            new KsiazkaDetailsDialog(clickedBook, service, currentUser).open();
        });
    }

    private void updateBookList(Object selection) {
        List<Ksiazka> ksiazki;
        if (selection instanceof Dziedzina) {
            ksiazki = service.findKsiazkiByDziedzina((Dziedzina) selection);
        } else if (selection instanceof Poddziedzina) {
            ksiazki = service.findKsiazkiByPoddziedzina((Poddziedzina) selection);
        } else {
            ksiazki = service.findAllKsiazki(null);
        }
        bookGrid.setItems(ksiazki);
    }

    private HorizontalLayout createBookCard(Ksiazka ksiazka) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("book-card");
        card.setSpacing(true);
        card.setPadding(true);
        card.setWidthFull();
        card.setAlignItems(Alignment.START);

        // ZDJĘCIE
        Image coverImage;
        byte[] okladka = ksiazka.getDaneKsiazki().getOkladka();
        if (okladka != null && okladka.length > 0) {
            StreamResource resource = new StreamResource("cover_" + ksiazka.getId(), () -> new ByteArrayInputStream(okladka));
            coverImage = new Image(resource, "Okładka");
        } else {
            coverImage = new Image("https://placehold.co/100x150?text=Brak", "Brak");
        }
        coverImage.setWidth("100px");
        coverImage.setHeight("150px");
        coverImage.getStyle().set("border-radius", "5px").set("object-fit", "cover");
        coverImage.getStyle().set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

        // DANE
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);

        H3 tytul = new H3(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin", "0 0 5px 0").set("color", "#2c3e50");

        String autorzyStr = ksiazka.getDaneKsiazki().getAutorzy().stream()
                .map(a -> a.getImie() + " " + a.getNazwisko())
                .collect(Collectors.joining(", "));
        Span autor = new Span(autorzyStr);
        autor.getStyle().set("font-weight", "bold").set("color", "#7f8c8d");

        Span meta = new Span(ksiazka.getDaneKsiazki().getWydawnictwo() + " • " + ksiazka.getDaneKsiazki().getRokWydania());
        meta.getStyle().set("font-size", "0.9em").set("color", "#95a5a6");

        Span isbn = new Span("ISBN: " + ksiazka.getDaneKsiazki().getIsbn());
        isbn.getStyle().set("font-size", "0.9em").set("color", "#95a5a6");

        String katStr = "-";
        if (ksiazka.getPoddziedzina() != null) {
            katStr = ksiazka.getPoddziedzina().getDziedzina().getNazwa() + " > " + ksiazka.getPoddziedzina().getNazwa();
        }
        Span kategoria = new Span(katStr);
        kategoria.getElement().getThemeList().add("badge contrast");
        kategoria.getStyle().set("margin-top", "10px");

        details.add(tytul, autor, meta, isbn, kategoria);

        // STATUS
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
        badge.getElement().getThemeList().add("badge " + (dostepna ? "success" : "error"));
        badge.getStyle().set("padding", "0.5em 1em");
        return badge;
    }
}