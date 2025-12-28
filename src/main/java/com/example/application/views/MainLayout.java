package com.example.application.views;

import com.example.application.security.SecurityService;
import com.example.application.views.dziedziny.DziedzinaView;
import com.example.application.views.ksiazki.KsiazkiView;
import com.example.application.views.mojekonto.MojeKontoView;
import com.example.application.views.pracownicy.PracownicyView;
import com.example.application.views.uzytkownicy.UzytkownicyView;
import com.example.application.views.oczekujacekonta.OczekujaceKontaView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Wypożyczalnia książek");
        logo.addClassNames("text-xl", "m-m");

        // KLUCZ: Pozycjonowanie absolutne logo względem paska nawigacji
        logo.getStyle().set("position", "absolute");
        logo.getStyle().set("left", "50%");
        logo.getStyle().set("transform", "translateX(-50%)");
        logo.getStyle().set("margin", "0");

        // Kontener na przyciski po prawej
        Div buttons = new Div();
        if (authContext.isAuthenticated()) {
            Button logout = new Button("Wyloguj się", new Icon(VaadinIcon.SIGN_OUT), e -> authContext.logout());
            logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            buttons.add(logout);
        } else {
            Button login = new Button("Zaloguj się", e -> getUI().ifPresent(ui -> ui.navigate("login")));
            // Zamiast: Button register = new Button("Załóż konto");
            Button register = new Button("Załóż konto", e -> getUI().ifPresent(ui -> ui.navigate("rejestracja")));
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            register.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            register.getStyle().set("margin-left", "10px");
            buttons.add(login, register);
        }

        // Tworzymy layout nagłówka
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), buttons);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();

        // Rozpychamy toggle i buttons na skrajne końce
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassNames("py-0", "px-m");

        // Dodajemy logo bezpośrednio do navbar, obok layoutu z przyciskami
        addToNavbar(header, logo);
    }

    private void createDrawer() {
        VerticalLayout menu = new VerticalLayout();

        // Strona główna - dostępna dla każdego
        RouterLink homeLink = new RouterLink("Strona główna", HomeView.class);
        homeLink.setHighlightCondition(HighlightConditions.sameLocation());
        menu.add(homeLink);
        RouterLink myAccountLink = new RouterLink("Moje Konto", MojeKontoView.class);
        menu.add(myAccountLink);

        // Sprawdzamy rolę w sposób bezpieczny dla Vaadina
        // Ważne: hasRole automatycznie szuka przedrostka ROLE_,
        // więc wpisujemy samo "ADMIN"
        if (authContext.isAuthenticated() && authContext.hasRole("KIEROWNIK")) {
            menu.add(new RouterLink("Lista pracowników", PracownicyView.class));
            menu.add(new RouterLink("Dziedziny i poddziedziny", DziedzinaView.class));
        }
        if (authContext.isAuthenticated() && authContext.hasRole("KIEROWNIK") || authContext.hasRole("BIBLIOTEKARZ")) {
            menu.add(new RouterLink("Lista użytkowników", UzytkownicyView.class));
            menu.add(new RouterLink("Konta do aktywacji", OczekujaceKontaView.class));
            menu.add(new RouterLink("Lista książek", KsiazkiView.class));
        }

        addToDrawer(menu);
    }
}