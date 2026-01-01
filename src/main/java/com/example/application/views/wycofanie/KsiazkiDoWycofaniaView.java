package com.example.application.views.wycofanie;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Pracownicy;
import com.example.application.data.service.CrmService;
import com.example.application.security.SecurityService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

@RolesAllowed("KIEROWNIK")
@Route(value = "wycofanie", layout = MainLayout.class)
@PageTitle("Książki do wycofania | Zarządzanie")
public class KsiazkiDoWycofaniaView extends VerticalLayout {

    private final CrmService service;
    private final SecurityService securityService;
    private final Grid<Ksiazka> grid = new Grid<>(Ksiazka.class);

    public KsiazkiDoWycofaniaView(CrmService service, SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
        setSizeFull();
        setPadding(true);

        add(new H2("Książki zgłoszone do wycofania"));
        add(new Span("Poniższe pozycje zostały oznaczone przez magazyniera jako 'Do wycofania'. Wymagana decyzja kierownika."));

        configureGrid();
        add(grid);
        updateList();
    }

    private void configureGrid() {
        grid.addClassName("wycofanie-grid");
        grid.setSizeFull();
        grid.removeAllColumns();

        grid.addColumn(k -> k.getDaneKsiazki().getTytul()).setHeader("Tytuł").setAutoWidth(true);
        grid.addColumn(k -> k.getDaneKsiazki().getIsbn()).setHeader("ISBN");
        grid.addColumn(Ksiazka::getLicznikWypozyczen).setHeader("Licznik wyp.");

        // Kolumna Akcji
        grid.addComponentColumn(ksiazka -> {
            Button wycofajBtn = new Button("Wycofaj z biblioteki");
            wycofajBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            wycofajBtn.addClickListener(e -> {
                // Otwieramy dialog z powodem
                TextArea powodField = new TextArea("Powód wycofania");
                powodField.setPlaceholder("np. Zniszczona okładka, brak stron, zalanie...");
                powodField.setWidthFull();
                powodField.setMinHeight("100px");

                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Potwierdź wycofanie");
                dialog.setText("Czy na pewno chcesz trwale wycofać książkę \"" + ksiazka.getDaneKsiazki().getTytul() + "\"?");
                dialog.add(powodField);

                dialog.setCancelable(true);
                dialog.setCancelText("Anuluj");

                dialog.setConfirmText("Zatwierdź wycofanie");
                dialog.setConfirmButtonTheme("error primary");

                dialog.addConfirmListener(event -> {
                    if (powodField.getValue().isEmpty()) {
                        Notification.show("Musisz podać powód!", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return; // Nie zamyka dialogu jeśli brak powodu (w teorii, w ConfirmDialog Vaadina listener zamyka, więc walidacja jest trudniejsza, ale tu upraszczamy)
                    }

                    // Pobieramy zalogowanego kierownika
                    UserDetails userDetails = securityService.getAuthenticatedUser();
                    Pracownicy kierownik = service.findPracownikByEmail(userDetails.getUsername());

                    service.wycofajKsiazke(ksiazka, kierownik, powodField.getValue());

                    Notification.show("Książka została wycofana.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    updateList();
                });

                dialog.open();
            });

            return wycofajBtn;
        }).setHeader("Decyzja").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void updateList() {
        grid.setItems(service.findKsiazkiDoDecyzjiWycofania());
    }
}