package com.example.application.views.dziedziny;

import com.example.application.data.entity.Dziedzina;
import com.example.application.data.entity.Poddziedzina;
import com.example.application.data.service.CrmService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
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
        setWidth("500px");
        setHeight("600px");

        grid.setColumns("nazwa");
        updateGrid();

        TextField nowaPoddziedzina = new TextField("Nazwa poddziedziny");
        Button addBtn = new Button("Dodaj", e -> {
            if (!nowaPoddziedzina.isEmpty()) {
                service.savePoddziedzina(new Poddziedzina(nowaPoddziedzina.getValue(), dziedzina));
                nowaPoddziedzina.clear();
                updateGrid();
                onUpdate.run(); // Odświeżamy też listę główną (licznik)
            }
        });

        HorizontalLayout form = new HorizontalLayout(nowaPoddziedzina, addBtn);
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        add(form, grid);

        Button closeBtn = new Button("Zamknij", e -> close());
        getFooter().add(closeBtn);
    }

    private void updateGrid() {
        grid.setItems(service.findPoddziedzinyByDziedzina(dziedzina));
    }
}