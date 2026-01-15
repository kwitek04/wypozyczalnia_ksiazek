package com.example.application.views.ksiazki;

import com.example.application.data.entity.Autor;
import com.example.application.data.entity.DaneKsiazki;
import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.StatusKsiazki;
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
    KsiazkiForm form;

    public KsiazkiView(CrmService service) {
        this.service = service;
        addClassName("ksiazka-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassName("content");
        content.setSizeFull();
        return content;
    }

    private void configureForm() {
        form = new KsiazkiForm(service);
        form.setWidth("25em");
        form.addListener(KsiazkiForm.SaveEvent.class, this::saveKsiazka);
        form.addListener(KsiazkiForm.DeleteEvent.class, this::deleteKsiazka);
        form.addListener(KsiazkiForm.CloseEvent.class, e -> closeEditor());
    }

    private void configureGrid() {
        grid.addClassNames("ksiazka-grid");
        grid.setSizeFull();
        grid.setColumns();
        grid.addColumn(ksiazka -> ksiazka.getDaneKsiazki().getTytul()).setHeader("Tytuł").setSortable(true);
        grid.addColumn(ksiazka -> ksiazka.getDaneKsiazki().getAutorzy().stream()
                        .map(autor -> autor.getImie() + " " + autor.getNazwisko())
                        .collect(Collectors.joining(", ")))
                .setHeader("Autorzy");

        grid.addColumn(ksiazka -> ksiazka.getDaneKsiazki().getIsbn()).setHeader("ISBN");


        grid.addColumn(ksiazka -> ksiazka.getPoddziedzina() != null ?
                        ksiazka.getPoddziedzina().getDziedzina().getNazwa() : "-")
                .setHeader("Dziedzina");

        grid.addColumn(ksiazka -> ksiazka.getStatus().getName()).setHeader("Status").setSortable(true);
        grid.addColumn(Ksiazka::getStanFizyczny).setHeader("Stan fizyczny");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editKsiazka(event.getValue()));
    }

    private Component getToolbar() {
        filterText.setPlaceholder("Wyszukaj po tytule...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addKsiazkaButton = new Button("Dodaj książkę");
        addKsiazkaButton.addClickListener(click -> addKsiazka());

        var toolbar = new HorizontalLayout(filterText, addKsiazkaButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void saveKsiazka(KsiazkiForm.SaveEvent event) {
        service.saveKsiazka(event.getKsiazka());
        updateList();
        closeEditor();
    }

    private void deleteKsiazka(KsiazkiForm.DeleteEvent event) {
        service.deleteKsiazka(event.getKsiazka());
        updateList();
        closeEditor();
    }

    public void editKsiazka(Ksiazka ksiazka) {
        if (ksiazka == null) {
            closeEditor();
        } else {
            form.setKsiazka(ksiazka);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void addKsiazka() {
        grid.asSingleSelect().clear();
        Ksiazka nowaKsiazka = new Ksiazka();
        nowaKsiazka.setDaneKsiazki(new DaneKsiazki());
        nowaKsiazka.setStatus(StatusKsiazki.DOSTEPNA);
        editKsiazka(nowaKsiazka);
    }

    private void closeEditor() {
        form.setKsiazka(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(service.findAllKsiazki(filterText.getValue()));
    }
}