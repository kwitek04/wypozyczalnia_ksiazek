package com.example.application.views;

import com.example.application.security.SecurityService;
import com.example.application.views.dziedziny.DziedzinaView;
import com.example.application.views.kary.KaryView;
import com.example.application.views.ksiazki.KsiazkiView;
import com.example.application.views.mojekonto.MojeKontoView;
import com.example.application.views.pracownicy.PracownicyView;
import com.example.application.views.statystyki.StatystykiView;
import com.example.application.views.uzytkownicy.UzytkownicyView;
import com.example.application.views.oczekujacekonta.OczekujaceKontaView;
import com.example.application.views.wycofanie.KsiazkiDoWycofaniaView;
import com.example.application.views.wypozyczenia.MojeWypozyczeniaView;
import com.example.application.views.wypozyczenia.ZarzadzanieWypozyczeniamiView;
import com.example.application.views.kontrolastanu.KontrolaStanuView;
import com.example.application.views.odlozenie.KsiazkiDoOdlozeniaView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
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
import com.example.application.views.rezerwacje.MojeRezerwacjeView;

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

        logo.getStyle().set("position", "absolute");
        logo.getStyle().set("left", "50%");
        logo.getStyle().set("transform", "translateX(-50%)");
        logo.getStyle().set("margin", "0");

        Div buttons = new Div();
        if (authContext.isAuthenticated()) {
            Button logout = new Button("Wyloguj się", new Icon(VaadinIcon.SIGN_OUT), e -> authContext.logout());
            logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            buttons.add(logout);
        } else {
            Button login = new Button("Zaloguj się", e -> getUI().ifPresent(ui -> ui.navigate("login")));
            Button register = new Button("Załóż konto", e -> getUI().ifPresent(ui -> ui.navigate("rejestracja")));
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            register.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            register.getStyle().set("margin-left", "10px");
            buttons.add(login, register);
        }

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), buttons);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();

        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassNames("py-0", "px-m");

        addToNavbar(header, logo);
    }

    private void createDrawer() {
        VerticalLayout menu = new VerticalLayout();

        // Gość i wszyscy inni użytkownicy
        menu.add(new RouterLink("Strona główna", com.example.application.views.HomeView.class));
        menu.add(new RouterLink("Katalog", com.example.application.views.katalog.KatalogView.class));

        // Uzytkownicy zalogowani i pracownicy
        if (authContext.isAuthenticated()) {
            menu.add(new RouterLink("Moje konto", MojeKontoView.class));
        }

        // Użytkownicy zalogowani
        if (authContext.isAuthenticated() && authContext.hasRole("USER")) {
            menu.add(new RouterLink("Moje wypożyczenia", MojeWypozyczeniaView.class));
            menu.add(new RouterLink("Moje rezerwacje", MojeRezerwacjeView.class));
            menu.add(new RouterLink("Kary i opłaty", KaryView.class));
        }

        // Bibliotekarz
        if (authContext.isAuthenticated() && (authContext.hasRole("BIBLIOTEKARZ"))) {
            menu.add(new RouterLink("Zarządzanie Książkami", KsiazkiView.class));
            menu.add(new RouterLink("Zarządzanie Użytkownikami", UzytkownicyView.class));
            menu.add(new RouterLink("Zarządzanie wypożyczeniami", ZarzadzanieWypozyczeniamiView.class));
            menu.add(new RouterLink("Konta do aktywacji", OczekujaceKontaView.class));
        }

        // Kierownik
        if (authContext.isAuthenticated() && authContext.hasRole("KIEROWNIK")) {
            menu.add(new RouterLink("Zarządzanie pracownikami", PracownicyView.class));
            menu.add(new RouterLink("Książki do wycofania", KsiazkiDoWycofaniaView.class));
            menu.add(new RouterLink("Statystyki globalne wypożyczalni", StatystykiView.class));
            menu.add(new RouterLink("Dziedziny i poddziedziny", DziedzinaView.class));
        }

        // Magazynier
        if (authContext.isAuthenticated() && (authContext.hasRole("MAGAZYNIER"))) {
            menu.add(new RouterLink("Kontrola stanu książek", KontrolaStanuView.class));
            menu.add(new RouterLink("Książki do odłożenia", KsiazkiDoOdlozeniaView.class));
        }

        addToDrawer(menu);
    }
}