package com.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // Importuj tę adnotację

/**
 * Główna klasa aplikacji Spring Boot dla systemu Przychodni.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.clinic.repository") // Jawne skanowanie repozytoriów JPA
public class ClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicApplication.class, args);
    }

}