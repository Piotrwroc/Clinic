package com.przychodnia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.ComponentScan; // Usunięto redundantny import

/**
 * Główna klasa startowa aplikacji Spring Boot.
 * Adnotacja @SpringBootApplication automatycznie konfiguruje aplikację
 * (np. skanowanie komponentów, autokonfiguracja).
 */
@SpringBootApplication
// Adnotacja @SpringBootApplication automatycznie wykonuje skanowanie komponentów
// z pakietu, w którym znajduje się ta klasa (com.przychodnia) i jej podpakietów.
// Dlatego jawne ComponentScan(basePackages = {"com.przychodnia"}) jest zbędne.
public class PrzychodniaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrzychodniaApplication.class, args);
    }

}
