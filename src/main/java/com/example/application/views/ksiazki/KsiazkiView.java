package com.example.application.views.ksiazki;

import com.example.application.data.entity.Autor;
import com.example.application.data.entity.Ksiazka;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.stream.Collectors;

@PermitAll
@Route(value = "ksiazki", layout = MainLayout.class)
@PageTitle("Książki | Biblioteka")
public class KsiazkiView extends VerticalLayout {
    private final CrmService service;
    Grid<Ksiazka> grid = new Grid<>(Ksiazka.class);
    TextField filterText = new TextField();

    public KsiazkiView(CrmService service) {
        this.service = service;
        addClassName("ksiazka-view");
        setSizeFull();
        configureGrid();

        add(getToolbar(), grid);
        updateList();
    }

    private void configureGrid() {
        grid.addClassNames("ksiazka-grid");
        grid.setSizeFull();

        // Czyścimy automatyczne kolumny, aby zdefiniować własne (z relacjami)
        grid.setColumns();

        // Kolumny z encji DaneKsiazki (przez relację OneToOne)
        grid.addColumn(ksiazka -> ksiazka.getDaneKsiazki().getTytul()).setHeader("Tytuł").setSortable(true);

        // Wyświetlanie autorów (relacja ManyToMany w DaneKsiazki)
        grid.addColumn(ksiazka -> ksiazka.getDaneKsiazki().getAutorzy().stream()
                        .map(autor -> autor.getImie() + " " + autor.getNazwisko())
                        .collect(Collectors.joining(", ")))
                .setHeader("Autorzy");

        grid.addColumn(ksiazka -> ksiazka.getDaneKsiazki().getIsbn()).setHeader("ISBN");

        // Poddziedzina i Dziedzina (przez relację ManyToOne)
        grid.addColumn(ksiazka -> ksiazka.getPoddziedzina() != null ?
                        ksiazka.getPoddziedzina().getDziedzina().getNazwa() : "-")
                .setHeader("Dziedzina");

        // Kolumny bezpośrednio z encji Ksiazka
        grid.addColumn(ksiazka -> ksiazka.getStatus().getName()).setHeader("Status").setSortable(true);
        grid.addColumn(Ksiazka::getStanFizyczny).setHeader("Stan fizyczny");
        
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private Component getToolbar() {
        filterText.setPlaceholder("Wyszukaj po tytule...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addKsiazkaButton = new Button("Dodaj książkę");
        // addKsiazkaButton.addClickListener(click -> addKsiazka()); // To zrobimy zaraz

        var toolbar = new HorizontalLayout(filterText, addKsiazkaButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void updateList() {
        grid.setItems(service.findAllKsiazki(filterText.getValue()));
    }
}