package com.example.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed // Strona główna powinna być dostępna dla każdego przed zalogowaniem
@Route(value = "", layout = MainLayout.class) // Pusty ciąg "" oznacza stronę główną (localhost:8080/)
@PageTitle("Strona Główna | Biblioteka")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        add(new H2("Witaj w Systemie Bibliotecznym"));
        add(new Paragraph("Wybierz odpowiednią opcję z menu bocznego, aby kontynuować."));
    }
}