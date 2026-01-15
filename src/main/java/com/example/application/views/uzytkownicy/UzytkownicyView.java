package com.example.application.views.uzytkownicy;

import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.service.LibraryService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed({"KIEROWNIK", "BIBLIOTEKARZ"})
@Route(value = "uzytkownicy", layout = MainLayout.class)
@PageTitle("Lista użytkowników | Biblioteka")
public class UzytkownicyView extends VerticalLayout {
    Grid<Uzytkownicy> grid = new Grid<>(Uzytkownicy.class);
    TextField filterText = new TextField();
    UzytkownicyForm form;
    LibraryService service;

    public UzytkownicyView(LibraryService service) {
        this.service = service;
        addClassName("uzytkownicy-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("uzytkownicy-grid");
        grid.setSizeFull();
        grid.setColumns("imie", "nazwisko", "email", "nrTelefonu");

        grid.addColumn(u -> u.getDataUrodzenia() != null ? u.getDataUrodzenia().toString() : "")
                .setHeader("Data urodzenia").setSortable(true);;

        grid.addColumn(u -> {
            if (u.isLocked()) return "Zablokowane";
            if (!u.isEnabled()) return "Oczekujące";
            return "Aktywne";
        }).setHeader("Status konta").setSortable(true);;

        grid.addComponentColumn(uzytkownik -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> editUzytkownik(uzytkownik));
            return editBtn;
        }).setWidth("70px").setFlexGrow(0);

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        grid.asSingleSelect().addValueChangeListener(event -> editUzytkownik(event.getValue()));
    }

    private void configureForm() {
        form = new UzytkownicyForm();
        form.setWidth("25em");
        form.addListener(UzytkownicyForm.SaveEvent.class, this::saveUzytkownik);
        form.addListener(UzytkownicyForm.DeleteEvent.class, this::deleteUzytkownik);
        form.addListener(UzytkownicyForm.CloseEvent.class, e -> closeEditor());
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filtruj po nazwisku...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addBtn = new Button("Dodaj użytkownika");
        addBtn.addClickListener(click -> addUzytkownik());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addBtn);
        toolbar.addClassName("uzytkownicy-toolbar");
        return toolbar;
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void saveUzytkownik(UzytkownicyForm.SaveEvent event) {
        service.saveUzytkownik(event.getUzytkownik());
        updateList();
        closeEditor();
    }

    private void deleteUzytkownik(UzytkownicyForm.DeleteEvent event) {
        service.deleteUzytkownik(event.getUzytkownik());
        updateList();
        closeEditor();
    }

    public void editUzytkownik(Uzytkownicy uzytkownik) {
        if (uzytkownik == null) {
            closeEditor();
        } else {
            form.setUzytkownik(uzytkownik);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void addUzytkownik() {
        grid.asSingleSelect().clear();
        editUzytkownik(new Uzytkownicy());
    }

    private void closeEditor() {
        form.setUzytkownik(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(service.findAllUzytkownicy(filterText.getValue()));
    }
}