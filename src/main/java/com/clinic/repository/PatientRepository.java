package com.clinic.repository;

import com.clinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium do zarządzania encją Patient.
 * Rozszerza JpaRepository, zapewniając podstawowe operacje CRUD.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Znajduje pacjenta po adresie e-mail.
     * @param email Adres e-mail pacjenta.
     * @return Opcjonalny obiekt Patient, jeśli znaleziono.
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Znajduje pacjenta po numerze PESEL.
     * @param pesel Numer PESEL pacjenta.
     * @return Opcjonalny obiekt Patient, jeśli znaleziono.
     */
    Optional<Patient> findByPesel(String pesel);
}
