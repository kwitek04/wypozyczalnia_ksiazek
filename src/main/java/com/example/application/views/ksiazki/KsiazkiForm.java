package com.example.application.views.ksiazki;

import com.example.application.data.entity.*;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class KsiazkiForm extends FormLayout {
    // Pola dla DaneKsiazki (część stała)
    TextField isbn = new TextField("ISBN-13");
    TextField tytul = new TextField("Tytuł");
    TextField wydawnictwo = new TextField("Wydawnictwo");
    IntegerField rokWydania = new IntegerField("Rok wydania");

    // Pola dla Ksiazka (egzemplarz)
    TextField stanFizyczny = new TextField("Stan fizyczny");
    ComboBox<StatusKsiazki> status = new ComboBox<>("Status");

    // Relacje
    ComboBox<Dziedzina> dziedzina = new ComboBox<>("Dziedzina");
    ComboBox<Poddziedzina> poddziedzina = new ComboBox<>("Poddziedzina");

    Button save = new Button("Zapisz");
    Button delete = new Button("Usuń");
    Button close = new Button("Anuluj");

    Binder<Ksiazka> binder = new BeanValidationBinder<>(Ksiazka.class);
    Binder<DaneKsiazki> daneBinder = new BeanValidationBinder<>(DaneKsiazki.class);

    public KsiazkiForm(List<Dziedzina> dziedziny) {
        addClassName("ksiazka-form");

        isbn.setRequired(true);
        tytul.setRequired(true);
        wydawnictwo.setRequired(true);
        rokWydania.setRequiredIndicatorVisible(true);
        stanFizyczny.setRequired(true);
        status.setRequired(true);
        dziedzina.setRequired(true);
        poddziedzina.setRequired(true);

        // 2. Konfiguracja Bindera dla pól obowiązkowych
        // Dla każdego pola musisz dodać asRequired()

        binder.forField(stanFizyczny)
                .asRequired("Pole jest wymagane")
                .bind(Ksiazka::getStanFizyczny, Ksiazka::setStanFizyczny);

        // To samo dla DaneKsiazki
        daneBinder.forField(isbn)
                .asRequired("ISBN musi mieć 13 znaków")
                .withValidator(s -> s.length() == 13, "ISBN musi mieć dokładnie 13 znaków")
                .bind(DaneKsiazki::getIsbn, DaneKsiazki::setIsbn);

        daneBinder.forField(tytul)
                .asRequired("Pole jest wymagane")
                .bind(DaneKsiazki::getTytul, DaneKsiazki::setTytul);

        daneBinder.forField(wydawnictwo)
                .asRequired("Pole jest wymagane")
                .bind(DaneKsiazki::getWydawnictwo, DaneKsiazki::setWydawnictwo);

        daneBinder.forField(rokWydania)
                .asRequired("Pole jest wymagane")
                .bind(DaneKsiazki::getRokWydania, DaneKsiazki::setRokWydania);

        // Na końcu:
        binder.bindInstanceFields(this);
        daneBinder.bindInstanceFields(this);

        // Konfiguracja list
        status.setItems(StatusKsiazki.values());
        status.setItemLabelGenerator(StatusKsiazki::getName);

        dziedzina.setItems(dziedziny);
        dziedzina.setItemLabelGenerator(Dziedzina::getNazwa);

        poddziedzina.setItemLabelGenerator(Poddziedzina::getNazwa);
        poddziedzina.setEnabled(false); // Blokujemy, póki dziedzina nie jest wybrana

        // LOGIKA KASKADOWA: Wybór dziedziny filtruje poddziedziny
        dziedzina.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                poddziedzina.setItems(e.getValue().getPoddziedziny());
                poddziedzina.setEnabled(true);
            } else {
                poddziedzina.setEnabled(false);
                poddziedzina.clear();
            }
        });

        // Mapowanie pól
        binder.bindInstanceFields(this);
        daneBinder.bindInstanceFields(this);

        add(isbn, tytul, wydawnictwo, rokWydania,
                dziedzina, poddziedzina, stanFizyczny, status,
                createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            // Próbujemy zapisać dane z pól do obiektów
            binder.writeBean(binder.getBean());
            daneBinder.writeBean(daneBinder.getBean());

            // Jeśli nie rzuciło błędu (ValidationException), wysyłamy zdarzenie
            fireEvent(new SaveEvent(this, binder.getBean()));
        } catch (ValidationException e) {
            // Jeśli pola są puste, Vaadin sam podświetli je na czerwono
        }
    }

    public void setKsiazka(Ksiazka ksiazka) {
        binder.setBean(ksiazka);
        if (ksiazka != null && ksiazka.getDaneKsiazki() != null) {
            daneBinder.setBean(ksiazka.getDaneKsiazki());
            if (ksiazka.getPoddziedzina() != null) {
                dziedzina.setValue(ksiazka.getPoddziedzina().getDziedzina());
                poddziedzina.setValue(ksiazka.getPoddziedzina());
            }
        }
    }

    // Eventy (SaveEvent, DeleteEvent, CloseEvent - analogicznie jak w PracownicyForm)
    public static abstract class KsiazkaFormEvent extends ComponentEvent<KsiazkiForm> {
        private Ksiazka ksiazka;
        protected KsiazkaFormEvent(KsiazkiForm source, Ksiazka ksiazka) {
            super(source, false);
            this.ksiazka = ksiazka;
        }
        public Ksiazka getKsiazka() { return ksiazka; }
    }

    public static class SaveEvent extends KsiazkaFormEvent {
        SaveEvent(KsiazkiForm source, Ksiazka ksiazka) { super(source, ksiazka); }
    }
    public static class DeleteEvent extends KsiazkaFormEvent {
        DeleteEvent(KsiazkiForm source, Ksiazka ksiazka) { super(source, ksiazka); }
    }
    public static class CloseEvent extends KsiazkaFormEvent {
        CloseEvent(KsiazkiForm source) { super(source, null); }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}