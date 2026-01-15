package com.example.application.views.uzytkownicy;

import com.example.application.data.entity.Uzytkownicy;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.util.Locale;

public class UzytkownicyForm extends FormLayout {
    private Uzytkownicy uzytkownik;

    TextField imie = new TextField("Imię");
    TextField nazwisko = new TextField("Nazwisko");
    DatePicker dataUrodzenia = new DatePicker("Data urodzenia");
    TextField nrTelefonu = new TextField("Numer telefonu");
    EmailField email = new EmailField("Email");
    PasswordField password = new PasswordField("Hasło");

    Button save = new Button("Zapisz");
    Button delete = new Button("Usuń");
    Button close = new Button("Anuluj");
    Button lockBtn = new Button();

    Binder<Uzytkownicy> binder = new BeanValidationBinder<>(Uzytkownicy.class);

    public UzytkownicyForm() {
        addClassName("uzytkownicy-form");

        dataUrodzenia.setLocale(new Locale("pl", "PL"));

        binder.bindInstanceFields(this);

        add(imie, nazwisko, dataUrodzenia, nrTelefonu, email, password, lockBtn, createButtonsLayout());
        lockBtn.addClickListener(event -> toggleLock());
    }

    public void setUzytkownik(Uzytkownicy uzytkownik) {
        this.uzytkownik = uzytkownik;
        binder.readBean(uzytkownik);

        if (uzytkownik != null) {
            updateLockButton();
            lockBtn.setVisible(uzytkownik.isEnabled());
        }
    }

    private void updateLockButton() {
        if (uzytkownik.isLocked()) {
            lockBtn.setText("Odblokuj");
            lockBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            lockBtn.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        } else {
            lockBtn.setText("Zablokuj");
            lockBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            lockBtn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        }
        lockBtn.setWidthFull();
    }

    private void toggleLock() {
        if (uzytkownik != null) {
            uzytkownik.setLocked(!uzytkownik.isLocked());
            updateLockButton();
            fireEvent(new SaveEvent(this, uzytkownik));
            Notification.show(uzytkownik.isLocked() ? "Konto zablokowane" : "Konto odblokowane");
        }
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, uzytkownik)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(uzytkownik);
            fireEvent(new SaveEvent(this, uzytkownik));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public static abstract class UzytkownicyFormEvent extends ComponentEvent<UzytkownicyForm> {
        private Uzytkownicy uzytkownik;

        protected UzytkownicyFormEvent(UzytkownicyForm source, Uzytkownicy uzytkownik) {
            super(source, false);
            this.uzytkownik = uzytkownik;
        }

        public Uzytkownicy getUzytkownik() {
            return uzytkownik;
        }
    }

    public static class SaveEvent extends UzytkownicyFormEvent {
        SaveEvent(UzytkownicyForm source, Uzytkownicy uzytkownik) {
            super(source, uzytkownik);
        }
    }

    public static class DeleteEvent extends UzytkownicyFormEvent {
        DeleteEvent(UzytkownicyForm source, Uzytkownicy uzytkownik) {
            super(source, uzytkownik);
        }
    }

    public static class CloseEvent extends UzytkownicyFormEvent {
        CloseEvent(UzytkownicyForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}