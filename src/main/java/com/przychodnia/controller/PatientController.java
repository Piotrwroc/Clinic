package com.przychodnia.controller;

import com.przychodnia.model.Patient;
import com.przychodnia.model.User;
import com.przychodnia.service.PatientService;
import com.przychodnia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Kontroler RESTowy do zarządzania danymi pacjentów.
 * Zapewnia endpointy dla operacji CRUD oraz specyficznych zapytań dotyczących pacjentów.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final UserService userService; // Potrzebne do pobrania informacji o zalogowanym użytkowniku

    /**
     * Pobiera wszystkich pacjentów. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * @return Lista pacjentów.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Pobiera pacjenta po ID.
     * ADMIN, LEKARZ, RECEPCJONISTA mają dostęp do każdego pacjenta.
     * PACJENT ma dostęp tylko do swoich danych.
     * @param id ID pacjenta.
     * @return Pacjent lub status 404/403.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<Patient> patient = patientService.getPatientById(id);

        if (patient.isPresent()) {
            User loggedInUser = userService.getUserByEmail(currentPrincipalEmail)
                    .orElseThrow(() -> new IllegalStateException("Zalogowany użytkownik nie został znaleziony w bazie danych."));

            // Jeśli użytkownik jest ADMIN, LEKARZ lub RECEPCJONISTA, ma dostęp
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_LEKARZ") ||
                    a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
                return ResponseEntity.ok(patient.get());
            }
            // Jeśli użytkownik jest PACJENTEM, musi to być jego własne ID
            else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
                if (patient.get().getEmail().equals(currentPrincipalEmail)) {
                    return ResponseEntity.ok(patient.get());
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Brak dostępu do cudzych danych
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Inna rola lub brak uprawnień
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Tworzy nowego pacjenta. Dostępne dla ADMIN, RECEPCJONISTA.
     * Pacjent nie może samodzielnie tworzyć konta pacjenta, a jedynie się zarejestrować (co tworzy konto użytkownika).
     * @param patient Obiekt pacjenta do utworzenia.
     * @return Utworzony pacjent.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCJONISTA')")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        try {
            Patient createdPatient = patientService.createPatient(patient);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Zwróć błąd jeśli np. email/PESEL zajęty
        }
    }

    /**
     * Aktualizuje dane pacjenta.
     * ADMIN, LEKARZ, RECEPCJONISTA mogą aktualizować każdego pacjenta.
     * PACJENT może aktualizować tylko swoje dane.
     * @param id ID pacjenta do aktualizacji.
     * @param patientDetails Zaktualizowane dane pacjenta.
     * @return Zaktualizowany pacjent lub status 404/403.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<Patient> existingPatient = patientService.getPatientById(id);
        if (existingPatient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User loggedInUser = userService.getUserByEmail(currentPrincipalEmail)
                .orElseThrow(() -> new IllegalStateException("Zalogowany użytkownik nie został znaleziony w bazie danych."));

        // Sprawdzenie uprawnień
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_LEKARZ") ||
                a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
            try {
                return patientService.updatePatient(id, patientDetails)
                        .map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
            if (existingPatient.get().getEmail().equals(currentPrincipalEmail)) {
                try {
                    return patientService.updatePatient(id, patientDetails)
                            .map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.notFound().build());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Usuwa pacjenta po ID. Dostępne tylko dla ADMIN.
     * @param id ID pacjenta do usunięcia.
     * @return Status 204 No Content lub 404 Not Found.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
