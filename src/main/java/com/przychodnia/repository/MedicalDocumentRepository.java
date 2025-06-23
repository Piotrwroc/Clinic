package com.przychodnia.repository;

import com.przychodnia.model.MedicalDocument;
import com.przychodnia.model.Patient;
import com.przychodnia.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repozytorium do zarządzania encją MedicalDocument.
 * Rozszerza JpaRepository, zapewniając podstawowe operacje CRUD.
 */
@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {

    /**
     * Znajduje wszystkie dokumenty medyczne dla danego pacjenta.
     * @param patient Obiekt pacjenta.
     * @return Lista dokumentów medycznych pacjenta.
     */
    List<MedicalDocument> findByPatient(Patient patient);

    /**
     * Znajduje wszystkie dokumenty medyczne powiązane z daną wizytą.
     * @param visit Obiekt wizyty.
     * @return Lista dokumentów medycznych powiązanych z wizytą.
     */
    List<MedicalDocument> findByVisit(Visit visit);
}
