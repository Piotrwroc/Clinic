package com.przychodnia.controller;

import com.przychodnia.model.MedicalDocument;
import com.przychodnia.model.Patient;
import com.przychodnia.model.User;
import com.przychodnia.service.MedicalDocumentService;
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
 * Kontroler RESTowy do zarządzania dokumentacją medyczną.
 * Zapewnia endpointy dla operacji CRUD dokumentów oraz ich pobierania.
 */
@RestController
@RequestMapping("/api/medical-documents")
@RequiredArgsConstructor
public class MedicalDocumentController {

    private final MedicalDocumentService medicalDocumentService;
    private final PatientService patientService;
    private final UserService userService;

    /**
     * Pobiera wszystkie dokumenty medyczne. Dostępne dla ADMIN.
     * @return Lista dokumentów medycznych.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MedicalDocument>> getAllMedicalDocuments() {
        List<MedicalDocument> documents = medicalDocumentService.getAllMedicalDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Pobiera dokument medyczny po ID.
     * ADMIN, LEKARZ, RECEPCJONISTA mają dostęp do każdego dokumentu.
     * PACJENT ma dostęp tylko do swoich dokumentów.
     * @param id ID dokumentu.
     * @return Dokument medyczny lub status 404/403.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalDocument> getMedicalDocumentById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        Optional<MedicalDocument> document = medicalDocumentService.getMedicalDocumentById(id);

        if (document.isPresent()) {
            User loggedInUser = userService.getUserByEmail(currentPrincipalEmail)
                    .orElseThrow(() -> new IllegalStateException("Zalogowany użytkownik nie został znaleziony w bazie danych."));

            // Jeśli użytkownik jest ADMIN, LEKARZ lub RECEPCJONISTA, ma dostęp
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_LEKARZ") ||
                    a.getAuthority().equals("ROLE_RECEPCJONISTA"))) {
                return ResponseEntity.ok(document.get());
            }
            // Jeśli użytkownik jest PACJENTEM, musi to być jego własny dokument
            else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
                if (document.get().getPatient().getEmail().equals(currentPrincipalEmail)) {
                    return ResponseEntity.ok(document.get());
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Brak dostępu do cudzych danych
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tworzy nowy dokument medyczny. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * @param patientId ID pacjenta.
     * @param visitId Opcjonalne ID wizyty.
     * @param name Nazwa dokumentu.
     * @param content Treść dokumentu.
     * @return Utworzony dokument.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<MedicalDocument> createMedicalDocument(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long visitId,
            @RequestParam String name,
            @RequestParam String content) {
        try {
            MedicalDocument createdDocument = medicalDocumentService.createMedicalDocument(patientId, visitId, name, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Aktualizuje treść dokumentu medycznego. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * Lekarz może aktualizować dokumenty dla swoich pacjentów (w uproszczeniu, nie ma tu ścisłej kontroli "przynależności" lekarza do dokumentu).
     * @param id ID dokumentu.
     * @param updatedContent Nowa treść.
     * @return Zaktualizowany dokument.
     */
    @PutMapping("/{id}/content")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<MedicalDocument> updateMedicalDocumentContent(@PathVariable Long id, @RequestBody String updatedContent) {
        try {
            return medicalDocumentService.updateMedicalDocumentContent(id, updatedContent)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Usuwa dokument medyczny. Dostępne tylko dla ADMIN.
     * @param id ID dokumentu.
     * @return Status 204 No Content lub 404 Not Found.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedicalDocument(@PathVariable Long id) {
        try {
            medicalDocumentService.deleteMedicalDocument(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pobiera dokumenty medyczne dla danego pacjenta.
     * ADMIN, LEKARZ, RECEPCJONISTA mają dostęp do wszystkich.
     * PACJENT ma dostęp tylko do swoich dokumentów.
     * @param patientId ID pacjenta.
     * @return Lista dokumentów.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalDocument>> getMedicalDocumentsForPatient(@PathVariable Long patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalEmail = authentication.getName();

        // Sprawdź uprawnienia podobnie jak w innych kontrolerach
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACJENT"))) {
            Optional<Patient> loggedInPatient = patientService.getPatientByEmail(currentPrincipalEmail);
            if (loggedInPatient.isEmpty() || !loggedInPatient.get().getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (!(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                a.getAuthority().equals("ROLE_LEKARZ") || // Poprawiona linia: usunięto zagnieżdżenie a.getAuthorities()
                a.getAuthority().equals("ROLE_RECEPCJONISTA")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        try {
            List<MedicalDocument> documents = medicalDocumentService.getMedicalDocumentsForPatient(patientId);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pobiera dokumenty medyczne powiązane z daną wizytą. Dostępne dla ADMIN, LEKARZ, RECEPCJONISTA.
     * @param visitId ID wizyty.
     * @return Lista dokumentów.
     */
    @GetMapping("/visit/{visitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEKARZ', 'RECEPCJONISTA')")
    public ResponseEntity<List<MedicalDocument>> getMedicalDocumentsForVisit(@PathVariable Long visitId) {
        try {
            List<MedicalDocument> documents = medicalDocumentService.getMedicalDocumentsForVisit(visitId);
            return ResponseEntity.ok(documents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
