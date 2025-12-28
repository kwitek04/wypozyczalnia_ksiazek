package com.example.application.views.dziedziny;

import com.example.application.data.entity.Dziedzina;
import com.example.application.data.entity.Poddziedzina;
import com.example.application.data.service.CrmService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class PoddziedzinaDialog extends Dialog {
    private final CrmService service;
    private final Dziedzina dziedzina;
    private Grid<Poddziedzina> grid = new Grid<>(Poddziedzina.class);
    private Runnable onUpdate;

    public PoddziedzinaDialog(CrmService service, Dziedzina dziedzina, Runnable onUpdate) {
        this.service = service;
        this.dziedzina = dziedzina;
        this.onUpdate = onUpdate;

        setHeaderTitle("Poddziedziny dla: " + dziedzina.getNazwa());
        setWidth("600px");
        setHeight("600px");

        // Konfiguracja Grida
        grid.setColumns("nazwa");

        // KOLUMNA USUWANIA - teraz poprawnie wewnątrz konstruktora
        grid.addComponentColumn(poddziedzina -> {
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            deleteBtn.addClickListener(e -> {
                ConfirmDialog confirm = new ConfirmDialog();
                confirm.setHeader("Usunąć poddziedzinę?");
                confirm.setText("Czy na pewno chcesz usunąć poddziedzinę '" + poddziedzina.getNazwa() + "'?");

                confirm.setCancelable(true);
                confirm.setConfirmText("Usuń");
                confirm.setConfirmButtonTheme("error primary");

                confirm.addConfirmListener(event -> {
                    // 1. Wołamy serwis (który teraz zajmie się i listą, i bazą)
                    service.deletePoddziedzina(poddziedzina);

                    // 2. Odświeżamy widoki
                    updateGrid();
                    onUpdate.run();
                });

                confirm.open();
            });
            return deleteBtn;
        }).setHeader("").setFlexGrow(0).setWidth("80px");

        // Formularz dodawania
        TextField nowaPoddziedzina = new TextField("Nazwa poddziedziny");
        Button addBtn = new Button("Dodaj", e -> {
            if (!nowaPoddziedzina.isEmpty()) {
                service.savePoddziedzina(new Poddziedzina(nowaPoddziedzina.getValue(), dziedzina));
                nowaPoddziedzina.clear();
                updateGrid();
                onUpdate.run();
            }
        });

        HorizontalLayout form = new HorizontalLayout(nowaPoddziedzina, addBtn);
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        add(form, grid);

        Button closeBtn = new Button("Zamknij", e -> close());
        getFooter().add(closeBtn);

        updateGrid();
    }

    private void updateGrid() {
        // Pobieramy świeżą listę bezpośrednio z bazy dla tej konkretnej dziedziny
        grid.setItems(service.findPoddziedzinyByDziedzina(dziedzina));
    }
}