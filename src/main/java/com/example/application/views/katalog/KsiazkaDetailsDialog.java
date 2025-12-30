package com.example.application.views.katalog;

import com.example.application.data.entity.Ksiazka;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;

public class KsiazkaDetailsDialog extends Dialog {

    public KsiazkaDetailsDialog(Ksiazka ksiazka) {
        setHeaderTitle("Szczegóły książki");

        // Przycisk zamknij w nagłówku
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        setWidth("800px"); // Szerokie okno
        setMaxWidth("90vw"); // Responsywność na mobile

        // --- LEWA KOLUMNA (Okładka + Info techniczne) ---
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setWidth("35%");
        leftColumn.setAlignItems(VerticalLayout.Alignment.CENTER);

        Image coverImage = createCoverImage(ksiazka);
        coverImage.setWidth("100%");
        coverImage.getStyle().set("border-radius", "8px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        // Status
        Span statusBadge = new Span(ksiazka.getStatus().getName());
        statusBadge.getElement().getThemeList().add("badge " + (ksiazka.getStatus().getName().equals("Dostępna") ? "success" : "error"));
        statusBadge.getStyle().set("margin-top", "15px").set("font-size", "1.1em").set("padding", "0.5em 1em");

        leftColumn.add(coverImage, statusBadge);

        // --- PRAWA KOLUMNA (Opis i Akcje) ---
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setWidth("65%");

        H2 tytul = new H2(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin-top", "0");

        String autorzyStr = ksiazka.getDaneKsiazki().getAutorzy().stream()
                .map(a -> a.getImie() + " " + a.getNazwisko())
                .collect(Collectors.joining(", "));
        H4 autor = new H4(autorzyStr);
        autor.getStyle().set("color", "#555").set("margin-top", "0");

        // Szczegóły
        Span details = new Span(
                "Wydawnictwo: " + ksiazka.getDaneKsiazki().getWydawnictwo() + " | " +
                        "Rok: " + ksiazka.getDaneKsiazki().getRokWydania() + " | " +
                        "ISBN: " + ksiazka.getDaneKsiazki().getIsbn()
        );
        details.getStyle().set("font-size", "0.9em").set("color", "#777");

        // Opis (Nowe pole)
        Paragraph opis = new Paragraph(ksiazka.getDaneKsiazki().getOpis() != null ?
                ksiazka.getDaneKsiazki().getOpis() : "Brak opisu dla tej pozycji.");
        opis.getStyle().set("text-align", "justify").set("line-height", "1.6");

        // Przyciski akcji (na dole)
        Button btnWypozycz = new Button("Wypożycz", VaadinIcon.BOOK.create());
        btnWypozycz.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnWypozycz.setWidthFull();
        // Na razie tylko wizualne - brak logiki
        btnWypozycz.setEnabled(ksiazka.getStatus().getName().equals("Dostępna"));

        Button btnRezerwuj = new Button("Zarezerwuj", VaadinIcon.CALENDAR_CLOCK.create());
        btnRezerwuj.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnRezerwuj.setWidthFull();

        HorizontalLayout actions = new HorizontalLayout(btnWypozycz, btnRezerwuj);
        actions.setWidthFull();
        actions.getStyle().set("margin-top", "auto"); // Pchaj przyciski na sam dół

        rightColumn.add(tytul, autor, details, opis, actions);
        // Ważne: żeby actions było na dole, rightColumn musi mieć określoną wysokość lub flex-grow
        rightColumn.setFlexGrow(1, opis);

        // Główny układ
        HorizontalLayout layout = new HorizontalLayout(leftColumn, rightColumn);
        layout.setSizeFull();
        layout.setAlignItems(VerticalLayout.Alignment.STRETCH);

        add(layout);
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