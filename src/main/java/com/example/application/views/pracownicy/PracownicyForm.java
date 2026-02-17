package com.example.application.views.pracownicy;

import com.example.application.data.entity.Pracownik;
import com.example.application.data.entity.Rola;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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

import java.util.List;

public class PracownicyForm extends FormLayout {
    private Pracownik pracownik;
    TextField imie = new TextField("Imie");
    TextField nazwisko = new TextField("Nazwisko");
    TextField nrTelefonu = new TextField("Numer telefonu");
    EmailField email = new EmailField("Email");
    PasswordField password = new PasswordField("Hasło");
    MultiSelectComboBox<Rola> role = new MultiSelectComboBox<>("Role");
    private Button lockBtn = new Button();

    Button save = new Button("Zapisz");
    Button delete = new Button("Usuń");
    Button close = new Button("Anuluj");
    Binder<Pracownik> binder = new BeanValidationBinder<>(Pracownik.class);

    public void setPracownicy(Pracownik pracownik) {
        this.pracownik = pracownik;
        binder.readBean(pracownik);

        if (pracownik != null) {
            this.setEnabled(true);
            lockBtn.setEnabled(true);
            lockBtn.setVisible(pracownik.getId() != null);

            updateStatusButton();
        }
    }

    private void updateStatusButton() {
        if (pracownik.isEnabled()) {
            lockBtn.setText("Zablokuj");
            lockBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            lockBtn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        } else {
            lockBtn.setText("Odblokuj");
            lockBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
            lockBtn.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        }
        lockBtn.setWidthFull();
    }

    private void toggleStatus() {
        if (pracownik != null) {
            pracownik.setEnabled(!pracownik.isEnabled());
            updateStatusButton();
            fireEvent(new SaveEvent(this, pracownik));
            Notification.show(pracownik.isEnabled() ? "Pracownik odblokowany" : "Pracownik zablokowany");
        }
    }

    public PracownicyForm(List<Rola> dostepneRole) {
        addClassName("pracownik-form");

        role.setItems(dostepneRole);
        role.setItemLabelGenerator(Rola::getName);
        role.setPlaceholder("Wybierz role...");

        binder.bindInstanceFields(this);

        add(imie, nazwisko, nrTelefonu, email, password, role, lockBtn, createButtonsLayout());
        lockBtn.addClickListener(event -> toggleStatus());
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, pracownik)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));
        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(pracownik);
            fireEvent(new SaveEvent(this, pracownik));
        } catch (ValidationException e) {
            Notification.show("Sprawdź poprawność danych w formularzu")
                    .addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
        }
    }

    public static abstract class PracownicyFormEvent extends ComponentEvent<PracownicyForm> {
        private Pracownik pracownik;

        protected PracownicyFormEvent(PracownicyForm source, Pracownik pracownik) {
            super(source, false);
            this.pracownik = pracownik;
        }

        public Pracownik getPracownicy() {
            return pracownik;
        }
    }

    public static class SaveEvent extends PracownicyFormEvent {
        SaveEvent(PracownicyForm source, Pracownik pracownik) {
            super(source, pracownik);
        }
    }

    public static class DeleteEvent extends PracownicyFormEvent {
        DeleteEvent(PracownicyForm source, Pracownik pracownik) {
            super(source, pracownik);
        }

    }

    public static class CloseEvent extends PracownicyFormEvent {
        CloseEvent(PracownicyForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}