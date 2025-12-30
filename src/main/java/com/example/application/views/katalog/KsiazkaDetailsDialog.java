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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;

public class KsiazkaDetailsDialog extends Dialog {

    public KsiazkaDetailsDialog(Ksiazka ksiazka) {
        setHeaderTitle("Szczegóły książki");

        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getHeader().add(closeButton);

        setWidth("800px");
        setMaxWidth("90vw");

        // --- LEWA KOLUMNA ---
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setWidth("35%");
        leftColumn.setAlignItems(FlexComponent.Alignment.CENTER);

        Image coverImage = createCoverImage(ksiazka);
        coverImage.setWidth("100%");
        coverImage.getStyle().set("border-radius", "8px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        Span statusBadge = new Span(ksiazka.getStatus().getName());
        statusBadge.getElement().getThemeList().add("badge " + (ksiazka.getStatus().getName().equals("Dostępna") ? "success" : "error"));
        statusBadge.getStyle().set("margin-top", "15px").set("font-size", "1.1em").set("padding", "0.5em 1em");

        leftColumn.add(coverImage, statusBadge);

        // --- PRAWA KOLUMNA ---
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setWidth("65%");
        rightColumn.getStyle().set("overflow-wrap", "anywhere");

        H2 tytul = new H2(ksiazka.getDaneKsiazki().getTytul());
        tytul.getStyle().set("margin-top", "0");

        String autorzyStr = ksiazka.getDaneKsiazki().getAutorzy().stream()
                .map(a -> a.getImie() + " " + a.getNazwisko())
                .collect(Collectors.joining(", "));
        H4 autor = new H4(autorzyStr);
        autor.getStyle().set("color", "#555").set("margin-top", "0");

        Span details = new Span(
                "Wydawnictwo: " + ksiazka.getDaneKsiazki().getWydawnictwo() + " | " +
                        "Rok: " + ksiazka.getDaneKsiazki().getRokWydania() + " | " +
                        "ISBN: " + ksiazka.getDaneKsiazki().getIsbn()
        );
        details.getStyle().set("font-size", "0.9em").set("color", "#777");

        Paragraph opis = new Paragraph(ksiazka.getDaneKsiazki().getOpis() != null ?
                ksiazka.getDaneKsiazki().getOpis() : "Brak opisu dla tej pozycji.");
        opis.getStyle().set("text-align", "justify").set("line-height", "1.6");

        // --- PRZYCISKI (POPRAWIONE SKALOWANIE) ---
        Button btnWypozycz = new Button("Wypożycz", VaadinIcon.BOOK.create());
        btnWypozycz.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnWypozycz.setEnabled(ksiazka.getStatus().getName().equals("Dostępna"));
        // Usunięto setWidthFull() z samego przycisku, żeby nie wymuszał 100%

        Button btnRezerwuj = new Button("Zarezerwuj", VaadinIcon.CALENDAR_CLOCK.create());
        btnRezerwuj.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        // Usunięto setWidthFull()

        HorizontalLayout actions = new HorizontalLayout(btnWypozycz, btnRezerwuj);
        actions.setWidthFull(); // Kontener zajmuje całą szerokość
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "auto");

        // KLUCZOWA ZMIANA: FlexGrow
        // To mówi: "Podzielcie się dostępnym miejscem po równo (1:1)"
        // Dzięki temu przyciski zawsze wypełnią kontener, ale nigdy go nie rozsadzą.
        actions.setFlexGrow(1, btnWypozycz, btnRezerwuj);

        rightColumn.add(tytul, autor, details, opis, actions);
        rightColumn.setFlexGrow(1, opis);

        HorizontalLayout layout = new HorizontalLayout(leftColumn, rightColumn);
        layout.setSizeFull();
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

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