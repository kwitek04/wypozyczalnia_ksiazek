package com.example.application.views.dziedziny;

import com.example.application.data.entity.Dziedzina;
import com.example.application.data.service.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

@RolesAllowed("KIEROWNIK")
@Route(value = "dziedziny", layout = MainLayout.class)
@PageTitle("Dziedziny | Biblioteka")
public class DziedzinaView extends VerticalLayout {
    private final CrmService service;
    Grid<Dziedzina> grid = new Grid<>(Dziedzina.class);
    TextField nazwaField = new TextField("Nowa dziedzina");
    Button addBtn = new Button("Dodaj");

    public DziedzinaView(CrmService service) {
        this.service = service;
        setSizeFull();

        configureGrid();

        HorizontalLayout toolbar = new HorizontalLayout(nazwaField, addBtn);
        toolbar.setAlignItems(Alignment.BASELINE);

        addBtn.addClickListener(e -> {
            try {
                String nazwa = nazwaField.getValue();
                if (nazwa != null && !nazwa.isEmpty()) {
                    Dziedzina nowa = new Dziedzina(nazwa);
                    service.saveDziedzina(nowa);
                    nazwaField.clear();
                    updateList();
                    com.vaadin.flow.component.notification.Notification.show("Dodano dziedzinę!");
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // To wypisze DOKŁADNY błąd w konsoli IntelliJ
                com.vaadin.flow.component.notification.Notification.show("Błąd: " + ex.getMessage(),
                        5000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
            }
        });

        add(toolbar, grid);
        updateList();
    }

    private void configureGrid() {
        grid.setColumns("nazwa");
        grid.addColumn(d -> d.getPoddziedziny() != null ? d.getPoddziedziny().size() : 0)
                .setHeader("Liczba poddziedzin");

        // KLIKNIĘCIE OTWIERA OKNO PODDZIEDZIN
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                openPoddziedzinaDialog(e.getValue());
                grid.asSingleSelect().clear();
            }
        });

        grid.addComponentColumn(dziedzina -> {
            Button deleteBtn = new Button(com.vaadin.flow.component.icon.VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR,
                    com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);

            deleteBtn.addClickListener(e -> {
                com.vaadin.flow.component.confirmdialog.ConfirmDialog dialog = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
                dialog.setHeader("Usunąć dziedzinę?");
                dialog.setText("Czy na pewno chcesz usunąć dziedzinę '" + dziedzina.getNazwa() + "'? " +
                        "Spowoduje to również usunięcie wszystkich jej poddziedzin.");

                dialog.setCancelable(true);
                dialog.setCancelText("Anuluj");

                dialog.setConfirmText("Usuń");
                dialog.setConfirmButtonTheme("error primary");

                dialog.addConfirmListener(event -> {
                    service.deleteDziedzina(dziedzina);
                    updateList();
                });

                dialog.open();
            });
            return deleteBtn;
        }).setHeader("").setFlexGrow(0).setWidth("80px");
    }

    private void openPoddziedzinaDialog(Dziedzina dziedzina) {
        new PoddziedzinaDialog(service, dziedzina, this::updateList).open();
    }

    private void updateList() {
        grid.setItems(service.findAllDziedziny());
    }
}
