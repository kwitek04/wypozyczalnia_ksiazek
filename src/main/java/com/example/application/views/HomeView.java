package com.example.application.views;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Tlumacz;
import com.example.application.data.entity.Uzytkownik;
import com.example.application.data.service.BookService;
import com.example.application.data.service.RentalService;
import com.example.application.data.service.UserService;
import com.example.application.security.SecurityService;
import com.example.application.views.katalog.KsiazkaDetailsDialog;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strona główna aplikacji.
 * Udostępnia wyszukiwarkę książek (po tytule, autorze lub ISBN)
 * oraz wyświetla listę dopasowanych pozycji w formie kart.
 */
@AnonymousAllowed
@Route(value = "", layout = MainLayout.class)
@PageTitle("Strona Główna | Biblioteka")
public class HomeView extends VerticalLayout {

    private final BookService bookService;
    private final UserService userService;
    private final RentalService rentalService;
    private final SecurityService securityService;

    private final Grid<Ksiazka> grid = new Grid<>(Ksiazka.class);
    private final TextField searchField = new TextField();
    private Uzytkownik currentUser;

    public HomeView(BookService bookService, UserService userService, RentalService rentalService, SecurityService securityService) {
        this.bookService = bookService;
        this.userService = userService;
        this.rentalService = rentalService;
        this.securityService = securityService;

        UserDetails userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            this.currentUser = userService.findUzytkownikByEmail(userDetails.getUsername());
        }

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        addClassName("home-view");

        add(createSearchBar());

        configureGrid();
        add(grid);

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

        grid.addComponentColumn(ksiazka -> createBookCard(ksiazka));

        grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_NO_BORDER);
        grid.addItemClickListener(event -> {
            Ksiazka clickedBook = event.getItem();
            new KsiazkaDetailsDialog(clickedBook, rentalService, currentUser).open();
        });
    }

    private HorizontalLayout createBookCard(Ksiazka ksiazka) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("book-card");
        card.setSpacing(true);
        card.setPadding(true);
        card.setWidthFull();
        card.setAlignItems(Alignment.START);

        Image coverImage;
        byte[] okladka = ksiazka.getDaneKsiazki().getOkladka();

        if (okladka != null && okladka.length > 0) {
            StreamResource resource = new StreamResource("cover_" + ksiazka.getId(), () -> new java.io.ByteArrayInputStream(okladka));
            coverImage = new Image(resource, "Okładka książki");
        } else {
            coverImage = new Image("https://placehold.co/100x150?text=Brak+okładki", "Brak okładki");
        }

        coverImage.setWidth("140px");
        coverImage.setHeight("210px");
        coverImage.getStyle().set("border-radius", "5px");
        coverImage.getStyle().set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");
        coverImage.getStyle().set("object-fit", "cover");
        coverImage.addClassName("book-cover-image");

        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);


        H3 tytul = new H3(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin-top", "0");
        tytul.getStyle().set("margin-bottom", "5px");

        String autorzyStr = "Brak autora";
        if (ksiazka.getDaneKsiazki().getAutorzy() != null && !ksiazka.getDaneKsiazki().getAutorzy().isEmpty()) {
            autorzyStr = ksiazka.getDaneKsiazki().getAutorzy().stream()
                    .map(a -> a.getImie() + " " + a.getNazwisko())
                    .collect(Collectors.joining(", "));
        }
        Span autor = new Span(autorzyStr);
        autor.getStyle().set("font-weight", "bold");
        autor.getStyle().set("color", "#7f8c8d");

        Span wydawnictwoRok = new Span(
                ksiazka.getDaneKsiazki().getWydawnictwo() + " • " +
                        ksiazka.getDaneKsiazki().getRokWydania()
        );
        wydawnictwoRok.getStyle().set("font-size", "0.9em");
        wydawnictwoRok.getStyle().set("color", "#95a5a6");

        Span isbn = new Span("ISBN: " + ksiazka.getDaneKsiazki().getIsbn());
        isbn.getStyle().set("font-size", "0.9em");
        isbn.getStyle().set("color", "#95a5a6");

        Span tlumaczSpan = new Span();
        Set<Tlumacz> tlumaczeList = ksiazka.getDaneKsiazki().getTlumacze();

        if (tlumaczeList != null && !tlumaczeList.isEmpty()) {
            String tlumaczeStr = tlumaczeList.stream()
                    .map(t -> t.getImie() + " " + t.getNazwisko())
                    .collect(Collectors.joining(", "));

            tlumaczSpan.setText("Tłumacz: " + tlumaczeStr);
            tlumaczSpan.getStyle().set("font-size", "0.9em");
            tlumaczSpan.getStyle().set("color", "#95a5a6");
        }

        String kategoriaStr = "-";
        if (ksiazka.getPoddziedzina() != null) {
            kategoriaStr = ksiazka.getPoddziedzina().getDziedzina().getNazwa() + " > " + ksiazka.getPoddziedzina().getNazwa();
        }
        Span kategoria = new Span(kategoriaStr);
        kategoria.getElement().getThemeList().add("badge");
        kategoria.getElement().getThemeList().add("contrast");
        kategoria.getStyle().set("margin-top", "10px");

        details.add(tytul, autor, wydawnictwoRok, isbn, tlumaczSpan, kategoria);

        Div spacer = new Div();
        card.setFlexGrow(1, spacer);

        Span statusBadge = createStatusBadge(ksiazka);

        VerticalLayout rightSide = new VerticalLayout(spacer, statusBadge);
        rightSide.setSpacing(false);
        rightSide.setPadding(false);
        rightSide.setHeight("210px");
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
        grid.setItems(bookService.findKsiazkiBySearch(searchField.getValue()));
    }
}