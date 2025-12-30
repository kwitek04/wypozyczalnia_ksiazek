package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.application.data.entity.Pracownicy;
import com.example.application.data.repository.PracownicyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "flowcrmtutorial")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        // Dodajemy ikonę (favicon)
        settings.addFavIcon("icon", "icons/icon.png", "192x192");
        // Opcjonalnie dodajemy też jako 'shortcut icon' dla starszych przeglądarek
        settings.addLink("shortcut icon", "icons/icon.png");

        // Możesz tu też ustawić tytuł domyślny strony
        settings.setPageTitle("Wypożyczalnia Książek");
    }
}

