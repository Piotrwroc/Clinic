package com.clinic.service;

import com.clinic.model.MedicalDocument;
import com.clinic.model.Patient;
import com.clinic.model.Visit;
import com.clinic.repository.MedicalDocumentRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serwis odpowiedzialny za logikę biznesową związaną z dokumentacją medyczną.
 * Umożliwia tworzenie, przechowywanie i pobieranie dokumentów medycznych.
 */
@Service
@RequiredArgsConstructor
public class MedicalDocumentService {

    private final MedicalDocumentRepository medicalDocumentRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    /**
     * Pobiera wszystkie dokumenty medyczne z bazy danych.
     * @return Lista wszystkich dokumentów medycznych.
     */
    public List<MedicalDocument> getAllMedicalDocuments() {
        return medicalDocumentRepository.findAll();
    }

    /**
     * Pobiera dokument medyczny o podanym ID.
     * @param id ID dokumentu medycznego.
     * @return Opcjonalny obiekt MedicalDocument, jeśli dokument został znaleziony.
     */
    public Optional<MedicalDocument> getMedicalDocumentById(Long id) {
        return medicalDocumentRepository.findById(id);
    }

    /**
     * Tworzy nowy dokument medyczny dla danego pacjenta i opcjonalnie wizyty.
     * @param patientId ID pacjenta, do którego należy dokument.
     * @param visitId Opcjonalne ID wizyty, z którą dokument jest powiązany.
     * @param name Nazwa dokumentu.
     * @param content Treść dokumentu.
     * @return Zapisany obiekt MedicalDocument.
     * @throws IllegalArgumentException Jeśli pacjent nie istnieje lub wizyta nie istnieje/nie należy do pacjenta.
     */
    @Transactional
    public MedicalDocument createMedicalDocument(Long patientId, Long visitId, String name, String content) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacjent o podanym ID nie istnieje."));

        Visit visit = null;
        if (visitId != null) {
            visit = visitRepository.findById(visitId)
                    .orElseThrow(() -> new IllegalArgumentException("Wizyta o podanym ID nie istnieje."));
            // Walidacja czy wizyta należy do tego pacjenta
            if (!visit.getPatient().getId().equals(patientId)) {
                throw new IllegalArgumentException("Wizyta o podanym ID nie należy do tego pacjenta.");
            }
        }

        MedicalDocument document = new MedicalDocument();
        document.setPatient(patient);
        document.setVisit(visit);
        document.setName(name);
        document.setContent(content);
        document.setCreationDate(LocalDateTime.now()); // Ustaw aktualną datę utworzenia

        return medicalDocumentRepository.save(document);
    }

    /**
     * Aktualizuje treść istniejącego dokumentu medycznego.
     * @param id ID dokumentu do zaktualizowania.
     * @param updatedContent Nowa treść dokumentu.
     * @return Zaktualizowany obiekt dokumentu, lub Optional.empty() jeśli dokument nie istnieje.
     */
    @Transactional
    public Optional<MedicalDocument> updateMedicalDocumentContent(Long id, String updatedContent) {
        return medicalDocumentRepository.findById(id).map(document -> {
            document.setContent(updatedContent);
            return medicalDocumentRepository.save(document);
        });
    }

    /**
     * Usuwa dokument medyczny o podanym ID.
     * @param id ID dokumentu medycznego do usunięcia.
     */
    @Transactional
    public void deleteMedicalDocument(Long id) {
        if (!medicalDocumentRepository.existsById(id)) {
            throw new IllegalArgumentException("Dokument medyczny o podanym ID nie istnieje.");
        }
        medicalDocumentRepository.deleteById(id);
    }

    /**
     * Pobiera wszystkie dokumenty medyczne dla danego pacjenta.
     * @param patientId ID pacjenta.
     * @return Lista dokumentów medycznych pacjenta.
     * @throws IllegalArgumentException Jeśli pacjent nie istnieje.
     */
    public List<MedicalDocument> getMedicalDocumentsForPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacjent o podanym ID nie istnieje."));
        return medicalDocumentRepository.findByPatient(patient);
    }

    /**
     * Pobiera wszystkie dokumenty medyczne powiązane z daną wizytą.
     * @param visitId ID wizyty.
     * @return Lista dokumentów medycznych powiązanych z wizytą.
     * @throws IllegalArgumentException Jeśli wizyta nie istnieje.
     */
    public List<MedicalDocument> getMedicalDocumentsForVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Wizyta o podanym ID nie istnieje."));
        return medicalDocumentRepository.findByVisit(visit);
    }
}
