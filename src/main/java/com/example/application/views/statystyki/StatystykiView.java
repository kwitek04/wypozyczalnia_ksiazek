package com.example.application.views.statystyki;

import com.example.application.data.entity.StatusKsiazki;
import com.example.application.data.service.LibraryService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;

@RolesAllowed("KIEROWNIK")
@Route(value = "statystyki", layout = MainLayout.class)
@PageTitle("Statystyki | Biblioteka")
public class StatystykiView extends VerticalLayout {

    private final LibraryService service;

    private final DatePicker startDate = new DatePicker("Data od");
    private final DatePicker endDate = new DatePicker("Data do");

    private final Span totalUsersCount = new Span("0");
    private final Span totalEmployeesCount = new Span("0");
    private final Span totalBooksCount = new Span("0");

    private final Span periodRentalsCount = new Span("0");
    private final Span periodReturnsCount = new Span("0");
    private final Span activeUsersCount = new Span("0");

    private final VerticalLayout statusChartContainer = new VerticalLayout();
    private final VerticalLayout activityChartContainer = new VerticalLayout();

    public StatystykiView(LibraryService service) {
        this.service = service;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("statystyki-view");

        add(new H2("Statystyki wypożyczalni"));

        add(new H4("Statystyki globalne"));
        HorizontalLayout globalCards = new HorizontalLayout();
        globalCards.setWidthFull();
        globalCards.add(
                createCard("Liczba użytkowników", totalUsersCount, VaadinIcon.USERS, "#6200EA"),
                createCard("Liczba pracowników", totalEmployeesCount, VaadinIcon.USER_STAR, "#009688"),
                createCard("Liczba książek", totalBooksCount, VaadinIcon.BOOK, "#2962FF")
        );
        add(globalCards);

        startDate.setValue(LocalDate.now().minusDays(30));
        endDate.setValue(LocalDate.now());

        Button refreshBtn = new Button("Odśwież", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> updateStats());

        H4 periodHeader = new H4("Statystyki czasowe");
        periodHeader.getStyle().set("margin-top", "30px");

        HorizontalLayout toolbar = new HorizontalLayout(startDate, endDate, refreshBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);

        add(periodHeader, toolbar);

        HorizontalLayout periodCards = new HorizontalLayout();
        periodCards.setWidthFull();
        periodCards.add(
                createCard("Wypożyczenia w wybranym okresie", periodRentalsCount, VaadinIcon.ARROW_CIRCLE_UP, "blue"),
                createCard("Zwroty w wybranym okresie", periodReturnsCount, VaadinIcon.ARROW_CIRCLE_DOWN, "green"),
                createCard("Aktywni użytkownicy w wybranym okresie", activeUsersCount, VaadinIcon.GROUP, "orange")
        );
        add(periodCards);

        HorizontalLayout chartsLayout = new HorizontalLayout();
        chartsLayout.setWidthFull();
        chartsLayout.setSpacing(true);
        chartsLayout.getStyle().set("margin-top", "20px");

        statusChartContainer.setWidth("50%");
        styleContainer(statusChartContainer);

        activityChartContainer.setWidth("50%");
        styleContainer(activityChartContainer);

        chartsLayout.add(statusChartContainer, activityChartContainer);
        add(chartsLayout);

        updateStats();
    }

    private void updateStats() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        if (start == null || end == null || start.isAfter(end)) {
            Notification.show("Nieprawidłowy zakres dat");
            return;
        }

        long usersTotal = service.countAllUsers();
        long employeesTotal = service.countAllEmployees();
        long booksTotal = service.countAllBooks();

        long wypozyczenia = service.countWypozyczeniaWOkresie(start, end);
        long zwroty = service.countZwrotyWOkresie(start, end);
        long activeUsers = service.countActiveUsersInPeriod(start, end);

        totalUsersCount.setText(String.valueOf(usersTotal));
        totalEmployeesCount.setText(String.valueOf(employeesTotal));
        totalBooksCount.setText(String.valueOf(booksTotal));

        periodRentalsCount.setText(String.valueOf(wypozyczenia));
        periodReturnsCount.setText(String.valueOf(zwroty));
        activeUsersCount.setText(String.valueOf(activeUsers));

        statusChartContainer.removeAll();
        statusChartContainer.add(new H4("Struktura zbioru książek"));

        long sDostepna = service.countKsiazkiByStatus(StatusKsiazki.DOSTEPNA);
        long sWypozyczona = service.countKsiazkiByStatus(StatusKsiazki.WYPOZYCZONA);
        long sZarezerwowana = service.countKsiazkiByStatus(StatusKsiazki.ZAREZERWOWANA);
        long sDoOdlozenia = service.countKsiazkiByStatus(StatusKsiazki.DO_ODLOZENIA);
        long sWKontroli = service.countKsiazkiByStatus(StatusKsiazki.W_KONTROLI);
        long sWRenowacji = service.countKsiazkiByStatus(StatusKsiazki.W_RENOWACJI);
        long sWycofana = service.countKsiazkiByStatus(StatusKsiazki.WYCOFANA);

        long total = booksTotal;

        if (total > 0) {
            statusChartContainer.add(createBar("Dostępne", sDostepna, total, "var(--lumo-success-color)"));
            statusChartContainer.add(createBar("Wypożyczone", sWypozyczona, total, "var(--lumo-error-color)"));
            statusChartContainer.add(createBar("Zarezerwowane", sZarezerwowana, total, "var(--lumo-primary-color)"));
            statusChartContainer.add(createBar("Do odłożenia na półkę", sDoOdlozenia, total, "#6200EA"));
            statusChartContainer.add(createBar("W kontroli", sWKontroli, total, "#FFD600"));
            statusChartContainer.add(createBar("W renowacji", sWRenowacji, total, "#FF6D00"));
            statusChartContainer.add(createBar("Wycofane", sWycofana, total, "var(--lumo-contrast-50pct)"));
        } else {
            statusChartContainer.add(new Span("Brak książek w systemie."));
        }

        activityChartContainer.removeAll();
        activityChartContainer.add(new H4("Wypożyczenia i zwroty"));
        long maxActivity = Math.max(wypozyczenia, zwroty);
        if (maxActivity == 0) maxActivity = 1;

        activityChartContainer.add(createBar("Wypożyczenia", wypozyczenia, maxActivity, "blue"));
        activityChartContainer.add(createBar("Zwroty", zwroty, maxActivity, "green"));
    }

    private void styleContainer(VerticalLayout layout) {
        layout.getStyle().set("background", "white")
                .set("padding", "20px")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");
    }

    private VerticalLayout createBar(String label, long value, long max, String color) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        HorizontalLayout info = new HorizontalLayout();
        info.setWidthFull();
        info.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        info.add(new Span(label));
        info.add(new Span(String.valueOf(value)));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue((double) value / max);
        progressBar.getStyle().set("--lumo-primary-color", color);
        progressBar.setHeight("10px");

        layout.add(info, progressBar);
        return layout;
    }

    private VerticalLayout createCard(String title, Span countSpan, VaadinIcon icon, String colorName) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("30%");

        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("border-left", "5px solid " + colorName);

        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", colorName).set("font-size", "1.5rem");

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-size", "0.9rem").set("color", "#666");

        countSpan.getStyle().set("font-size", "2.5rem").set("font-weight", "bold").set("color", "#333");

        HorizontalLayout header = new HorizontalLayout(iconSpan, titleSpan);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        card.add(header, countSpan);
        return card;
    }
}