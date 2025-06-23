package com.clinic.controller;

import com.clinic.model.Doctor;
import com.clinic.model.Visit;
import com.clinic.service.DoctorService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kontroler RESTowy do zarządzania danymi lekarzy.
 * Zapewnia endpointy dla operacji CRUD oraz pobierania dostępnych terminów.
 */
@RestController
@RequestMapping("/api/doctors")
@Data
@AllArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Pobiera wszystkich lekarzy. Dostępne dla wszystkich zalogowanych użytkowników.
     * @return Lista lekarzy.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Wszyscy zalogowani użytkownicy mogą przeglądać listę lekarzy
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    /**
     * Pobiera lekarza po ID. Dostępne dla wszystkich zalogowanych użytkowników.
     * @param id ID lekarza.
     * @return Lekarz lub status 404.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Tworzy nowego lekarza. Dostępne tylko dla ADMIN.
     * @param doctor Obiekt lekarza do utworzenia.
     * @return Utworzony lekarz.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor createdDoctor = doctorService.createDoctor(doctor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDoctor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Aktualizuje dane lekarza. Dostępne dla ADMIN.
     * @param id ID lekarza do aktualizacji.
     * @param doctorDetails Zaktualizowane dane lekarza.
     * @return Zaktualizowany lekarz lub status 404.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        try {
            return doctorService.updateDoctor(id, doctorDetails)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Usuwa lekarza po ID. Dostępne tylko dla ADMIN.
     * @param id ID lekarza do usunięcia.
     * @return Status 204 No Content lub 404 Not Found.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pobiera dostępne terminy dla danego lekarza.
     * Wymaga podania ID lekarza.
     * Dostępne dla ADMIN, PACJENT, RECEPCJONISTA.
     * @param doctorId ID lekarza.
     * @return Lista dostępnych wizyt (terminów).
     */
    @GetMapping("/{doctorId}/available-terms")
    @PreAuthorize("hasAnyRole('ADMIN', 'PACJENT', 'RECEPCJONISTA')")
    public ResponseEntity<List<Visit>> getAvailableTerms(@PathVariable Long doctorId) {
        // Domyślnie szukamy terminów od teraz
        List<Visit> availableTerms = doctorService.getAvailableTerms(doctorId, LocalDateTime.now());
        return ResponseEntity.ok(availableTerms);
    }
}
