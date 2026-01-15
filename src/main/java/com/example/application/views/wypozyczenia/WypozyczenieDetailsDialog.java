package com.example.application.views.wypozyczenia;

import com.example.application.data.entity.Ksiazka;
import com.example.application.data.entity.Wypozyczenie;
import com.example.application.data.service.LibraryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class WypozyczenieDetailsDialog extends Dialog {

    private final Wypozyczenie wypozyczenie;
    private final LibraryService service;

    public WypozyczenieDetailsDialog(Wypozyczenie wypozyczenie, LibraryService service) {
        this.wypozyczenie = wypozyczenie;
        this.service = service;

        Ksiazka ksiazka = wypozyczenie.getWypozyczoneKsiazki().isEmpty() ? null :
                wypozyczenie.getWypozyczoneKsiazki().get(0).getKsiazka();

        if (ksiazka == null) {
            close();
            return;
        }

        boolean isStaff = isCurrentUserStaff();

        setHeaderTitle("Zarządzanie wypożyczeniem");

        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        setWidth("700px");
        setMaxWidth("90vw");

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.START);

        // LEWA STRONA
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setWidth("35%");
        leftColumn.setAlignItems(FlexComponent.Alignment.CENTER);
        Image coverImage = createCoverImage(ksiazka);
        coverImage.setWidth("100%");
        coverImage.getStyle().set("border-radius", "8px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        leftColumn.add(coverImage);

        // PRAWA STRONA
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setWidth("65%");

        H2 tytul = new H2(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin-top", "0");

        String autorzy = ksiazka.getDaneKsiazki().getAutorzy().stream()
                .map(a -> a.getImie() + " " + a.getNazwisko())
                .reduce((a, b) -> a + ", " + b).orElse("");
        H4 autorLabel = new H4(autorzy);
        autorLabel.getStyle().set("color", "#555").set("margin-top", "0");

        // --- SEKCJA DAT ---
        VerticalLayout datesInfo = new VerticalLayout();
        datesInfo.setPadding(false);
        datesInfo.setSpacing(true); // Trochę odstępu między liniami
        datesInfo.getStyle().set("background-color", "#f5f5f5").set("border-radius", "5px").set("padding", "15px");

        // 1. Kto i kiedy
        if (isStaff) {
            datesInfo.add(new Span("Wypożyczający: " + wypozyczenie.getUzytkownik().getImie() + " " + wypozyczenie.getUzytkownik().getNazwisko()));
        }
        datesInfo.add(new Span("Data wypożyczenia: " + wypozyczenie.getDataWypozyczenia()));

        // 2. Termin zwrotu
        Span terminLabel = new Span("Termin zwrotu: " + wypozyczenie.getTerminZwrotu());
        terminLabel.getStyle().set("font-weight", "bold");

        if (wypozyczenie.getDataOddania() == null) {
            long dniSpoznienia = ChronoUnit.DAYS.between(wypozyczenie.getTerminZwrotu(), LocalDate.now());
            if (dniSpoznienia > 0) {
                terminLabel.setText(terminLabel.getText() + " (Spóźnienie: " + dniSpoznienia + " dni!)");
                terminLabel.getStyle().set("color", "red");
            }
        } else {
            datesInfo.add(new Span("Data oddania: " + wypozyczenie.getDataOddania()));
            Span zwroconoLabel = new Span("Zwrócono");
            zwroconoLabel.getElement().getThemeList().add("badge success");
            datesInfo.add(zwroconoLabel);
        }
        datesInfo.add(terminLabel);

        // 3. Status zgłoszenia (Na samym dole sekcji dat)
        if (wypozyczenie.getDataOddania() == null && wypozyczenie.isZwrotZgloszony()) {
            Div separator = new Div();
            separator.setHeight("10px"); // Odstęp

            Span zgloszonoInfo = new Span("Zgłoszono zwrot - oczekiwanie na zatwierdzenie przez bibliotekarza");
            zgloszonoInfo.getElement().getThemeList().add("badge contrast primary");
            zgloszonoInfo.getStyle().set("width", "100%").set("text-align", "center");

            datesInfo.add(separator, zgloszonoInfo);
        }

        // --- PRZYCISKI AKCJI ---


        // 1. BUTTON DLA PRACOWNIKA (Zatwierdź)
        Button btnZatwierdzZwrot = new Button("Zatwierdź zwrot", VaadinIcon.CHECK.create());
        btnZatwierdzZwrot.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnZatwierdzZwrot.setVisible(isStaff);

        btnZatwierdzZwrot.addClickListener(e -> {
            ConfirmDialog confirm = new ConfirmDialog();
            confirm.setHeader("Zatwierdź zwrot");
            confirm.setText("Czy fizycznie otrzymałeś książkę?");
            confirm.setConfirmText("Tak, otrzymałem");
            confirm.setConfirmButtonTheme("success primary");
            confirm.setCancelable(true);
            confirm.setCancelText("Anuluj");

            confirm.addConfirmListener(ev -> {
                service.zwrocKsiazke(wypozyczenie);
                Notification.show("Zwrot zatwierdzony!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
            });
            confirm.open();
        });

        // 2. BUTTON DLA UŻYTKOWNIKA (Zgłoś)
        Button btnZglosZwrot = new Button("Zgłoś zwrot", VaadinIcon.ARROW_RIGHT.create());
        btnZglosZwrot.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnZglosZwrot.setVisible(!isStaff && wypozyczenie.getDataOddania() == null && !wypozyczenie.isZwrotZgloszony());

        btnZglosZwrot.addClickListener(e -> {
            ConfirmDialog confirm = new ConfirmDialog();
            confirm.setHeader("Zgłoszenie zwrotu");
            confirm.setText("Czy na pewno chcesz zgłosić zwrot?");
            confirm.setConfirmText("Zgłoś");
            confirm.setConfirmButtonTheme("primary");
            confirm.setCancelable(true);
            confirm.setCancelText("Anuluj");

            confirm.addConfirmListener(ev -> {
                service.zglosZwrot(wypozyczenie);
                Notification.show("Zwrot zgłoszony.", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
            });
            confirm.open();
        });

        // 3. BUTTON PRZEDŁUŻ
        Button btnPrzedluz = new Button("Przedłuż termin", VaadinIcon.TIMER.create());
        btnPrzedluz.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // ZMIANA: Ukrywamy przycisk przedłużania dla pracownika (isStaff) oraz w innych przypadkach
        if (isStaff || wypozyczenie.getDataOddania() != null || wypozyczenie.isZwrotZgloszony()) {
            btnPrzedluz.setVisible(false);
        }

        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setFlexGrow(1, btnZatwierdzZwrot, btnZglosZwrot, btnPrzedluz);
        actions.getStyle().set("margin-top", "20px");

        if (btnZatwierdzZwrot.isVisible()) actions.add(btnZatwierdzZwrot);
        if (btnZglosZwrot.isVisible()) actions.add(btnZglosZwrot);
        if (btnPrzedluz.isVisible()) actions.add(btnPrzedluz);

        if (wypozyczenie.getDataOddania() != null) {
            actions.setVisible(false);
        }

        rightColumn.add(tytul, autorLabel, datesInfo, actions);
        layout.add(leftColumn, rightColumn);
        add(layout);
    }

    private boolean isCurrentUserStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_KIEROWNIK") ||
                        a.getAuthority().equals("ROLE_BIBLIOTEKARZ"));
    }

    private Image createCoverImage(Ksiazka ksiazka) {
        byte[] okladka = ksiazka.getDaneKsiazki().getOkladka();
        if (okladka != null && okladka.length > 0) {
            StreamResource resource = new StreamResource("cover_" + ksiazka.getId(), () -> new ByteArrayInputStream(okladka));
            return new Image(resource, "Okładka");
        } else {
            return new Image("https://placehold.co/200x300?text=Brak+okładki", "Brak");
        }
    }
}