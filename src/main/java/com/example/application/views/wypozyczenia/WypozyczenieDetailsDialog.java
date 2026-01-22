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

import java.time.format.DateTimeFormatter;
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

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setWidth("35%");
        leftColumn.setAlignItems(FlexComponent.Alignment.CENTER);
        Image coverImage = createCoverImage(ksiazka);
        coverImage.setWidth("100%");
        coverImage.getStyle().set("border-radius", "8px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        leftColumn.add(coverImage);

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setWidth("65%");

        H2 tytul = new H2(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin-top", "0");

        String autorzy = ksiazka.getDaneKsiazki().getAutorzy().stream()
                .map(a -> a.getImie() + " " + a.getNazwisko())
                .reduce((a, b) -> a + ", " + b).orElse("");
        H4 autorLabel = new H4(autorzy);
        autorLabel.getStyle().set("color", "#555").set("margin-top", "0");

        VerticalLayout datesInfo = new VerticalLayout();
        datesInfo.setPadding(false);
        datesInfo.setSpacing(true);
        datesInfo.getStyle().set("background-color", "#f5f5f5").set("border-radius", "5px").set("padding", "15px");

        if (isStaff) {
            datesInfo.add(new Span("Wypożyczający: " + wypozyczenie.getUzytkownik().getImie() + " " + wypozyczenie.getUzytkownik().getNazwisko()));
        }
        datesInfo.add(new Span("Data wypożyczenia: " + wypozyczenie.getDataWypozyczenia()));

        Span terminLabel = new Span("Termin zwrotu: " + wypozyczenie.getTerminZwrotu());
        terminLabel.getStyle().set("font-weight", "bold");

        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), wypozyczenie.getTerminZwrotu());

        if (wypozyczenie.getDataOddania() == null) {
            long dniSpoznienia = ChronoUnit.DAYS.between(wypozyczenie.getTerminZwrotu(), LocalDate.now());
            if (dniSpoznienia > 0) {
                terminLabel.setText(terminLabel.getText() + " (Spóźnienie: " + dniSpoznienia + " dni!)");
                terminLabel.getStyle().set("color", "red");
            }
            if (!wypozyczenie.isNaliczonoKareZaZaginiecie()) {
                LocalDate deadLine = wypozyczenie.getTerminZwrotu().plusDays(7);
                String formattedDeadline = deadLine.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                double cena = ksiazka.getDaneKsiazki().getCena() != null ? ksiazka.getDaneKsiazki().getCena() : 40.0;
                double potencjalnaKara = cena * 5;

                Div warningBox = new Div();
                warningBox.getStyle()
                        .set("background-color", "#ffebee") // Jasny czerwony
                        .set("color", "#c62828") // Ciemny czerwony tekst
                        .set("border", "1px solid #ef9a9a")
                        .set("padding", "10px")
                        .set("border-radius", "5px")
                        .set("margin-top", "10px")
                        .set("font-size", "0.9em");

                Span warningIcon = new Span(VaadinIcon.WARNING.create());
                warningIcon.getStyle().set("margin-right", "5px");

                Span warningText = new Span("Jeśli nie zwrócisz książki do " + formattedDeadline +
                        ", zostanie nałożona na ciebie kara w wysokości " + String.format("%.2f zł", potencjalnaKara) + "!");

                warningBox.add(warningIcon, warningText);
                datesInfo.add(warningBox);
            }
        }
        else {
            datesInfo.add(new Span("Data oddania: " + wypozyczenie.getDataOddania()));
            Span zwroconoLabel = new Span("Zwrócono");
            zwroconoLabel.getElement().getThemeList().add("badge success");
            datesInfo.add(zwroconoLabel);
        }
        datesInfo.add(terminLabel);

        if (wypozyczenie.getDataOddania() == null && wypozyczenie.isZwrotZgloszony()) {
            Div separator = new Div();
            separator.setHeight("10px"); // Odstęp

            Span zgloszonoInfo = new Span("Zgłoszono zwrot - oczekiwanie na zatwierdzenie przez bibliotekarza");
            zgloszonoInfo.getElement().getThemeList().add("badge contrast primary");
            zgloszonoInfo.getStyle().set("width", "100%").set("text-align", "center");

            datesInfo.add(separator, zgloszonoInfo);
        }

        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.getStyle().set("margin-top", "20px");

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

        if (btnZatwierdzZwrot.isVisible()) {
            actions.add(btnZatwierdzZwrot);
        }

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

        if (btnZglosZwrot.isVisible()) {
            actions.add(btnZglosZwrot);
        }

        if (!isStaff && wypozyczenie.getDataOddania() == null && !wypozyczenie.isZwrotZgloszony()) {
            Button btnPrzedluz = new Button("Przedłuż o tydzień", VaadinIcon.TIMER.create());
            btnPrzedluz.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

            // Sprawdzanie warunków dostępności
            if (wypozyczenie.isPrzedluzone()) {
                btnPrzedluz.setEnabled(false);
                btnPrzedluz.setText("Już przedłużono");
            } else if (daysUntilDue > 3) {
                btnPrzedluz.setEnabled(false);
                btnPrzedluz.setTooltipText("Dostępne dopiero 3 dni przed terminem (od " + wypozyczenie.getTerminZwrotu().minusDays(3) + ")");
            } else if (daysUntilDue < 0) {
                btnPrzedluz.setEnabled(false);
                btnPrzedluz.setTooltipText("Nie można przedłużyć po terminie");
            } else {
                // Jest OK (<= 3 dni i >= 0 dni)
                btnPrzedluz.setEnabled(true);
                btnPrzedluz.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }

            btnPrzedluz.addClickListener(e -> {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Przedłużenie wypożyczenia");
                dialog.setText("Czy chcesz przedłużyć termin zwrotu o 7 dni?\nMożesz to zrobić tylko raz.");
                dialog.setConfirmText("Przedłuż");
                dialog.setCancelable(true);
                dialog.setCancelText("Anuluj");

                dialog.addConfirmListener(ev -> {
                    try {
                        service.przedluzWypozyczenie(wypozyczenie);
                        Notification.show("Termin wydłużony pomyślnie!", 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        close();
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                dialog.open();
            });

            actions.add(btnPrzedluz);
        }

        rightColumn.add(tytul, datesInfo, actions);
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