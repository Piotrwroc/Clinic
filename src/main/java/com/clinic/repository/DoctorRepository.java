package com.clinic.repository;

import com.clinic.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium do zarządzania encją Doctor.
 * Rozszerza JpaRepository, zapewniając podstawowe operacje CRUD.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Znajduje lekarza po adresie e-mail.
     * @param email Adres e-mail lekarza.
     * @return Opcjonalny obiekt Doctor, jeśli znaleziono.
     */
    Optional<Doctor> findByEmail(String email);
}
