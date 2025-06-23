package com.clinic.controller;

import com.clinic.model.Patient;
import com.clinic.model.Visit;
import com.clinic.model.User;
import com.clinic.service.PatientService;
import com.clinic.service.UserService;
import com.clinic.service.VisitService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler RESTowy do zarządzania wizytami.
 * Zapewnia endpointy dla planowania, aktualizacji, anulowania, ukończenia wizyt
 * oraz pobierania historii wizyt dla pacjentów i lekarzy.
 */
@RestController
@RequestMapping("/api/visits")
@Data
@AllArgsConstructor
public class VisitController {

    private final VisitService visitService;
    private final PatientService patientService;
    private final UserService userService;

    /**
     * Pobiera wszystkie wizyty. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * @return Lista wizyt.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<List<Visit>> getAllVisits() {
        List<Visit> visits = visitService.getAllVisits();
        return ResponseEntity.ok(visits);
    }

    /**
     * Pobiera wizytę po ID.
     * ADMIN, LEKARZ, RECEPCJONISTA mają dostęp do każdej wizyty.
     * PACJENT ma dostęp tylko do swoich wizyt.
     * @param id ID wizyty.
     * @return Wizyta lub status 404/403.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Visit> getVisitById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<Visit> visit = visitService.getVisitById(id);

        if (visit.isPresent()) {
            User loggedInUser = userService.getUserByEmail(currentPrincipalEmail)
                    .orElseThrow(() -> new IllegalStateException("Zalogowany użytkownik nie został znaleziony w bazie danych."));

            // Jeśli użytkownik jest ADMIN, LEKARZ lub RECEPCJONISTA, ma dostęp
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_LEKARZ") ||
                    a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
                return ResponseEntity.ok(visit.get());
            }
            // Jeśli użytkownik jest PACJENTEM, musi to być jego własna wizyta
            else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
                if (visit.get().getPatient().getEmail().equals(currentPrincipalEmail)) {
                    return ResponseEntity.ok(visit.get());
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
     * Planuje nową wizytę. Dostępne dla ADMIN, RECEPCJONISTA, PACJENT.
     * Pacjent może planować tylko dla siebie.
     * @param patientId ID pacjenta.
     * @param doctorId ID lekarza.
     * @param visitDateTime Data i godzina wizyty w formacie ISO_LOCAL_DATE_TIME (np. "2023-10-26T10:00:00").
     * @return Utworzona wizyta.
     */
    @PostMapping("/schedule")
    public ResponseEntity<Visit> scheduleVisit(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam String visitDateTime) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        // Jeśli PACJENT, upewnij się, że planuje dla siebie
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
            Optional<Patient> loggedInPatient = patientService.getPatientByEmail(currentPrincipalEmail);
            if (loggedInPatient.isEmpty() || !loggedInPatient.get().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Pacjent może planować tylko dla siebie
            }
        } else if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(visitDateTime);
            Visit scheduledVisit = visitService.scheduleVisit(patientId, doctorId, dateTime);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduledVisit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Aktualizuje dane wizyty. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * Lekarz może aktualizować swoje wizyty.
     * @param id ID wizyty.
     * @param visitDetails Obiekt Visit z zaktualizowanymi danymi.
     * @return Zaktualizowana wizyta.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<Visit> updateVisit(@PathVariable Long id, @RequestBody Visit visitDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<Visit> existingVisit = visitService.getVisitById(id);
        if (existingVisit.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Jeśli LEKARZ, upewnij się, że aktualizuje swoją wizytę
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LEKARZ"))) {
            if (!existingVisit.get().getDoctor().getEmail().equals(currentPrincipalEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Lekarz może aktualizować tylko swoje wizyty
            }
        }
        // Admin i recepcjonista mogą aktualizować każdą wizytę
        else if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return visitService.updateVisit(id, visitDetails)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Anuluje wizytę. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA, PACJENT.
     * Pacjent może anulować tylko swoje wizyty.
     * Lekarz może anulować swoje wizyty.
     * @param id ID wizyty do anulowania.
     * @return Anulowana wizyta.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Visit> cancelVisit(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<Visit> existingVisit = visitService.getVisitById(id);
        if (existingVisit.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Sprawdzenie uprawnień
        boolean isAuthorized = false;
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
            isAuthorized = true; // Admin i recepcjonista zawsze mogą anulować
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
            if (existingVisit.get().getPatient().getEmail().equals(currentPrincipalEmail)) {
                isAuthorized = true; // Pacjent może anulować swoją wizytę
            }
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LEKARZ"))) {
            if (existingVisit.get().getDoctor().getEmail().equals(currentPrincipalEmail)) {
                isAuthorized = true; // Lekarz może anulować swoją wizytę
            }
        }

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return visitService.cancelVisit(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Oznacza wizytę jako ukończoną. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * Lekarz może oznaczyć swoje wizyty jako ukończone.
     * @param id ID wizyty do oznaczenia.
     * @return Ukończona wizyta.
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<Visit> completeVisit(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<Visit> existingVisit = visitService.getVisitById(id);
        if (existingVisit.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Jeśli LEKARZ, upewnij się, że oznacza swoją wizytę jako ukończoną
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LEKARZ"))) {
            if (!existingVisit.get().getDoctor().getEmail().equals(currentPrincipalEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        // Admin i recepcjonista mogą oznaczyć każdą wizytę
        else if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return visitService.completeVisit(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Usuwa wizytę. Dostępne tylko dla ADMIN.
     * @param id ID wizyty do usunięcia.
     * @return Status 204 No Content lub 404 Not Found.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        try {
            visitService.deleteVisit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pobiera historię wizyt dla danego pacjenta.
     * ADMIN, LEKARZ, RECEPCJONISTA mają dostęp do każdej historii.
     * PACJENT ma dostęp tylko do swojej historii.
     * @param patientId ID pacjenta.
     * @return Lista wizyt pacjenta.
     */
    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<Visit>> getPatientVisitHistory(@PathVariable Long patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        // Sprawdź, czy zalogowany PACJENT próbuje pobrać swoją historię
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
            Optional<Patient> loggedInPatient = patientService.getPatientByEmail(currentPrincipalEmail);
            if (loggedInPatient.isEmpty() || !loggedInPatient.get().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Pacjent nie ma dostępu do cudzych historii
            }
        } else if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_LEKARZ") ||
                a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Visit> history = visitService.getPatientVisitHistory(patientId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pobiera historię wizyt dla danego lekarza. Dostępne dla ADMIN, RECEPCJONISTA.
     * Lekarz ma dostęp tylko do swoich wizyt.
     * @param doctorId ID lekarza.
     * @return Lista wizyt lekarza.
     */
    @GetMapping("/doctor/{doctorId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<List<Visit>> getDoctorVisitHistory(@PathVariable Long doctorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        // Jeśli LEKARZ, upewnij się, że pobiera swoją historię
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LEKARZ"))) {
            if (!visitService.getDoctorVisitHistory(doctorId).stream()
                    .anyMatch(v -> v.getDoctor().getEmail().equals(currentPrincipalEmail))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        try {
            List<Visit> history = visitService.getDoctorVisitHistory(doctorId);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
