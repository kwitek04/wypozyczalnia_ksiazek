package com.example.application.views.mojekonto;

import com.example.application.data.entity.Pracownicy;
import com.example.application.data.entity.Uzytkownicy;
import com.example.application.data.repository.PracownicyRepository;
import com.example.application.data.repository.UzytkownicyRepository;
import com.example.application.security.SecurityService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

@PermitAll
@Route(value = "konto", layout = MainLayout.class)
@PageTitle("Moje Konto | Biblioteka")
public class MojeKontoView extends VerticalLayout {

    public MojeKontoView(SecurityService securityService,
                         PracownicyRepository pracownicyRepository,
                         UzytkownicyRepository uzytkownicyRepository) {

        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        String email = authenticatedUser.getUsername();
        String fullName = "";
        String phone = "";
        String role = "";

        Pracownicy p = pracownicyRepository.findByEmail(email);
        if (p != null) {
            fullName = p.getImie() + " " + p.getNazwisko();
            phone = p.getNrTelefonu();
            role = "Pracownik (" + p.getRoleAsString() + ")";
        } else {
            Uzytkownicy u = uzytkownicyRepository.findByEmail(email);
            if (u != null) {
                fullName = u.getImie() + " " + u.getNazwisko();
                phone = u.getNrTelefonu();
                role = "Czytelnik";
            }
        }

        Avatar avatar = new Avatar(fullName);
        avatar.setWidth("100px");
        avatar.setHeight("100px");


        avatar.getStyle().set("font-weight", "bold");
        avatar.getStyle().set("font-size", "1.2rem");

        H2 header = new H2(fullName);

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setAlignItems(Alignment.CENTER);
        infoLayout.setSpacing(false);

        infoLayout.add(
                createDetailRow(VaadinIcon.USER, "Rola: ", role),
                createDetailRow(VaadinIcon.ENVELOPE, "Email: ", email),
                createDetailRow(VaadinIcon.PHONE, "Telefon: ", phone)
        );

        add(avatar, header, infoLayout);
    }

    private HorizontalLayout createDetailRow(VaadinIcon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout(icon.create(), new Span(label), new Span(value));
        row.setAlignItems(Alignment.CENTER);
        row.getStyle().set("margin-top", "10px");
        return row;
    }
}