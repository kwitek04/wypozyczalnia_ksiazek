package com.example.application.views.ksiazki;

import com.example.application.data.entity.*;
import com.example.application.data.service.CrmService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import java.io.ByteArrayInputStream;
import com.vaadin.flow.component.textfield.TextArea;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KsiazkiForm extends FormLayout {
    // Pola dla DaneKsiazki (część stała)
    private Div wycofanieInfo = new Div();
    MemoryBuffer buffer = new MemoryBuffer();
    Upload upload = new Upload(buffer);
    Image previewImage = new Image();
    TextField isbn = new TextField("ISBN");
    TextField tytul = new TextField("Tytuł");
    TextField wydawnictwo = new TextField("Wydawnictwo");
    IntegerField rokWydania = new IntegerField("Rok wydania");
    TextArea opis = new TextArea("Opis książki");
    MultiSelectComboBox<Autor> autorzy = new MultiSelectComboBox<>("Autorzy");
    MultiSelectComboBox<Tlumacz> tlumacze = new MultiSelectComboBox<>("Tłumacze");

    // Pola dla Ksiazka (egzemplarz)
    ComboBox<StanFizyczny> stanFizyczny = new ComboBox<>("Stan fizyczny");
    TextField statusField = new TextField("Status");

    // Relacje
    ComboBox<Dziedzina> dziedzina = new ComboBox<>("Dziedzina");
    ComboBox<Poddziedzina> poddziedzina = new ComboBox<>("Poddziedzina");

    Button save = new Button("Zapisz");
    Button delete = new Button("Usuń");
    Button close = new Button("Anuluj");

    private final CrmService service;

    Binder<Ksiazka> binder = new BeanValidationBinder<>(Ksiazka.class);
    Binder<DaneKsiazki> daneBinder = new BeanValidationBinder<>(DaneKsiazki.class);

    public KsiazkiForm(CrmService service) {
        this.service = service;
        addClassName("ksiazka-form");

        wycofanieInfo.setVisible(false);
        wycofanieInfo.getStyle().set("background-color", "#ffe0e0")
                .set("color", "#d32f2f")
                .set("padding", "15px")
                .set("border-radius", "5px")
                .set("margin-bottom", "20px")
                .set("border", "1px solid #ffcdd2");
        setColspan(wycofanieInfo, 2);

        List<Dziedzina> dziedziny = service.findAllDziedziny();
        List<Autor> dostepniAutorzy = service.findAllAutorzy();
        List<Tlumacz> dostepniTlumacze = service.findAllTlumacze();

        isbn.setRequired(true);
        tytul.setRequired(true);
        wydawnictwo.setRequired(true);
        rokWydania.setRequiredIndicatorVisible(true);
        stanFizyczny.setRequired(true);
        stanFizyczny.setItems(StanFizyczny.values()); // Pobieramy wartości z Enuma
        stanFizyczny.setItemLabelGenerator(StanFizyczny::getNazwa); // Wyświetlamy ładne nazwy
        stanFizyczny.setPlaceholder("Wybierz stan...");
        statusField.setReadOnly(true);
        statusField.addThemeName("align-center");
        dziedzina.setRequired(true);
        poddziedzina.setRequired(false);
        autorzy.setRequired(true);

        opis.setPlaceholder("Podaj opis książki...");
        opis.setClearButtonVisible(true);
        opis.setHeight("150px");
        setColspan(opis, 2);

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(5 * 1024 * 1024); // Max 5MB
        upload.setDropLabel(new com.vaadin.flow.component.html.Span("Upuść okładkę tutaj (max 5MB)"));

        previewImage.setWidth("150px");
        previewImage.setVisible(false); // Domyślnie ukryte

        // Logika po załadowaniu pliku
        upload.addSucceededListener(event -> {
            try {
                // Pobieramy dane z buffora
                byte[] imageBytes = buffer.getInputStream().readAllBytes();

                // Zapisujemy w obiekcie, który jest aktualnie edytowany
                DaneKsiazki currentDane = daneBinder.getBean();
                if (currentDane != null) {
                    currentDane.setOkladka(imageBytes);
                    showImage(imageBytes); // Pokazujemy podgląd
                }
            } catch (IOException e) {
                Notification.show("Błąd podczas wczytywania obrazka");
            }
        });

        VerticalLayout uploadLayout = new VerticalLayout(upload, previewImage);
        uploadLayout.setPadding(false);
        uploadLayout.setSpacing(true);
        setColspan(uploadLayout, 2);

        autorzy.setItems(dostepniAutorzy);
        autorzy.setItemLabelGenerator(autor -> autor.getImie() + " " + autor.getNazwisko());
        autorzy.setPlaceholder("Wpisz autora i wciśnij ENTER...");
        autorzy.setHelperText("Aby dodać nowego autora, wpisz imię i nazwisko, a następnie wciśnij Enter.");

        autorzy.setAllowCustomValue(true);

        autorzy.addCustomValueSetListener(e -> {
            String wpisanaWartosc = e.getDetail();
            // Prosta logika: dzielimy tekst po pierwszej spacji
            // np. "Adam Mickiewicz" -> imie="Adam", nazwisko="Mickiewicz"
            String[] czesci = wpisanaWartosc.trim().split(" ", 2);

            String imie = czesci[0];
            String nazwisko = czesci.length > 1 ? czesci[1] : ""; // Jeśli brak nazwiska, pusty string

            if (!imie.isEmpty()) {
                Autor nowyAutor = new Autor(imie, nazwisko);
                service.saveAutor(nowyAutor); // Zapis do bazy

                // Odświeżenie listy w ComboBoxie
                Set<Autor> aktualnieWybrani = new HashSet<>(autorzy.getValue());
                aktualnieWybrani.add(nowyAutor);

                // Przeładowujemy listę z bazy żeby mieć pewność
                autorzy.setItems(service.findAllAutorzy());
                // Zaznaczamy stare + ten nowy
                autorzy.setValue(aktualnieWybrani);

                Notification.show("Dodano autora: " + imie + " " + nazwisko);
            }
        });

        tlumacze.setItems(dostepniTlumacze);
        tlumacze.setItemLabelGenerator(t -> t.getImie() + " " + t.getNazwisko());
        tlumacze.setPlaceholder("Dodaj tłumacza (opcjonalne)...");
        tlumacze.setClearButtonVisible(true);
        tlumacze.setAllowCustomValue(true); // Pozwalamy dodawać nowych

        tlumacze.addCustomValueSetListener(e -> {
            String wpisanaWartosc = e.getDetail();
            String[] czesci = wpisanaWartosc.trim().split(" ", 2);
            String imie = czesci[0];
            String nazwisko = czesci.length > 1 ? czesci[1] : "";

            if (!imie.isEmpty()) {
                Tlumacz nowy = new Tlumacz(imie, nazwisko);
                service.saveTlumacz(nowy);

                Set<Tlumacz> aktualnieWybrani = new HashSet<>(tlumacze.getValue());
                aktualnieWybrani.add(nowy);

                tlumacze.setItems(service.findAllTlumacze());
                tlumacze.setValue(aktualnieWybrani);
                Notification.show("Dodano tłumacza: " + imie + " " + nazwisko);
            }
        });

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

        daneBinder.forField(autorzy)
                .asRequired("Przynajmniej jeden autor jest wymagany")
                .bind(DaneKsiazki::getAutorzy, DaneKsiazki::setAutorzy);

        daneBinder.forField(tlumacze).bind(DaneKsiazki::getTlumacze, DaneKsiazki::setTlumacze);

        // Na końcu:
        binder.bindInstanceFields(this);
        daneBinder.bindInstanceFields(this);

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

        add(wycofanieInfo, uploadLayout, isbn, tytul, autorzy, tlumacze, wydawnictwo, rokWydania, opis,
                dziedzina, poddziedzina, stanFizyczny, statusField,
                createButtonsLayout());
    }

    private void showImage(byte[] imageBytes) {
        if (imageBytes != null && imageBytes.length > 0) {
            StreamResource resource = new StreamResource("cover", () -> new ByteArrayInputStream(imageBytes));
            previewImage.setSrc(resource);
            previewImage.setVisible(true);
        } else {
            previewImage.setVisible(false);
        }
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

        upload.clearFileList();
        previewImage.setVisible(false);
        wycofanieInfo.setVisible(false);
        wycofanieInfo.removeAll();

        // Resetujemy stan przycisków na aktywny
        save.setEnabled(true);
        delete.setEnabled(true);
        stanFizyczny.setReadOnly(false);
        
        if (ksiazka != null && ksiazka.getStatus() != null) {
            statusField.setValue(ksiazka.getStatus().getName());
        } else {
            statusField.setValue("");
        }

        // --- SPRAWDZAMY CZY WYCOFANA ---
        if (ksiazka != null && StatusKsiazki.WYCOFANA.equals(ksiazka.getStatus())) {
            // 1. Blokujemy przyciski
            save.setEnabled(false);
            delete.setEnabled(false);

            // 2. Pobieramy informacje o powodzie
            Wycofanie wycofanie = service.findWycofanieByKsiazka(ksiazka);
            if (wycofanie != null) {
                wycofanieInfo.setVisible(true);
                wycofanieInfo.add(new Span("Książka została wycofana"));
                wycofanieInfo.add(new Div(new Span("Data: " + wycofanie.getDataWycofania())));
                wycofanieInfo.add(new Div(new Span("Przez: " + wycofanie.getPracownik().getImie() + " " + wycofanie.getPracownik().getNazwisko())));

                Span powodSpan = new Span("Powód: " + wycofanie.getPowod());
                powodSpan.getStyle().set("font-weight", "bold");
                wycofanieInfo.add(new Div(powodSpan));
            } else {
                // Fallback, jeśli z jakiegoś powodu nie ma rekordu w tabeli Wycofanie
                wycofanieInfo.setVisible(true);
                wycofanieInfo.add(new Span("⚠️ Książka ma status WYCOFANA (brak szczegółów w historii). Edycja zablokowana."));
            }
        }

        // 1. Logika kaskady: Najpierw przygotuj listy rozwijane!
        if (ksiazka != null && ksiazka.getPoddziedzina() != null) {
            // Pobieramy dziedzinę z poddziedziny zapisanej w książce
            Dziedzina parentDziedzina = ksiazka.getPoddziedzina().getDziedzina();

            // Ustawiamy wartość w polu Dziedzina (żeby użytkownik widział co wybrano)
            dziedzina.setValue(parentDziedzina);

            // Ładujemy poddziedziny do drugiego ComboBoxa
            // ZANIM binder spróbuje ustawić tam wartość
            if (parentDziedzina != null) {
                poddziedzina.setItems(parentDziedzina.getPoddziedziny());
                poddziedzina.setEnabled(true);
            }
        } else {
            // Jeśli nowa książka lub brak poddziedziny
            if (ksiazka != null && ksiazka.getId() == null) {
                dziedzina.clear(); // Czyścimy przy nowej
                poddziedzina.clear();
                poddziedzina.setItems(Collections.emptyList());
                poddziedzina.setEnabled(false);
            }
        }

        // 2. Bindowanie głównej encji (Ksiazka)
        binder.setBean(ksiazka);

        if (ksiazka != null && ksiazka.getDaneKsiazki() != null) {
            daneBinder.setBean(ksiazka.getDaneKsiazki());
            // Jeśli książka ma już okładkę w bazie - pokaż ją
            showImage(ksiazka.getDaneKsiazki().getOkladka());
        } else {
            daneBinder.setBean(null);
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