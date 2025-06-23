package com.przychodnia.service;

import com.przychodnia.model.Patient;
import com.przychodnia.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Serwis odpowiedzialny za logikę biznesową związaną z pacjentami.
 * Wykorzystuje PatientRepository do interakcji z bazą danych.
 */
@Service
@RequiredArgsConstructor // Lombok generuje konstruktor z wymaganymi zależnościami (final fields)
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Pobiera wszystkich pacjentów z bazy danych.
     * @return Lista wszystkich pacjentów.
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Pobiera pacjenta o podanym ID.
     * @param id ID pacjenta.
     * @return Opcjonalny obiekt Patient, jeśli pacjent został znaleziony.
     */
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    /**
     * Tworzy nowego pacjenta.
     * @param patient Obiekt pacjenta do zapisania.
     * @return Zapisany obiekt pacjenta.
     */
    @Transactional
    public Patient createPatient(Patient patient) {
        // Dodatkowa walidacja, np. czy email/PESEL jest już zajęty
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Pacjent z podanym adresem email już istnieje.");
        }
        if (patient.getPesel() != null && patientRepository.findByPesel(patient.getPesel()).isPresent()) {
            throw new IllegalArgumentException("Pacjent z podanym numerem PESEL już istnieje.");
        }
        return patientRepository.save(patient);
    }

    /**
     * Aktualizuje dane istniejącego pacjenta.
     * @param id ID pacjenta do zaktualizowania.
     * @param patientDetails Obiekt Patient zawierający zaktualizowane dane.
     * @return Zaktualizowany obiekt pacjenta, lub Optional.empty() jeśli pacjent nie istnieje.
     */
    @Transactional
    public Optional<Patient> updatePatient(Long id, Patient patientDetails) {
        return patientRepository.findById(id).map(patient -> {
            patient.setImie(patientDetails.getImie());
            patient.setNazwisko(patientDetails.getNazwisko());
            patient.setDataUrodzenia(patientDetails.getDataUrodzenia());
            patient.setTelefon(patientDetails.getTelefon());
            patient.setAdres(patientDetails.getAdres());
            // E-mail i PESEL powinny być zmieniane ostrożnie, aby nie naruszać unikalności.
            // W bardziej rozbudowanej aplikacji należałoby to obsłużyć z większą precyzją.
            if (!patient.getEmail().equals(patientDetails.getEmail())) {
                if (patientRepository.findByEmail(patientDetails.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Nowy adres email jest już zajęty.");
                }
                patient.setEmail(patientDetails.getEmail());
            }
            if (patientDetails.getPesel() != null && !patient.getPesel().equals(patientDetails.getPesel())) {
                if (patientRepository.findByPesel(patientDetails.getPesel()).isPresent()) {
                    throw new IllegalArgumentException("Nowy numer PESEL jest już zajęty.");
                }
                patient.setPesel(patientDetails.getPesel());
            }

            return patientRepository.save(patient);
        });
    }

    /**
     * Usuwa pacjenta o podanym ID.
     * @param id ID pacjenta do usunięcia.
     */
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Pacjent o podanym ID nie istnieje.");
        }
        patientRepository.deleteById(id);
    }

    /**
     * Znajduje pacjenta po adresie e-mail.
     * @param email Adres e-mail pacjenta.
     * @return Opcjonalny obiekt Patient.
     */
    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }
}
