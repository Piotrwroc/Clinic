package com.clinic.service;

import com.clinic.model.Patient;
import com.clinic.model.User;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Dodaj ten import
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository; // Potrzebne do powiązania pacjenta z użytkownikiem

    @Transactional
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    @Transactional
    public Patient createPatient(Patient patient) {
        // Dodatkowa logika np. sprawdzenie unikalności emaila/PESELu
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Pacjent z podanym adresem email już istnieje.");
        }
        if (patient.getPesel() != null && patientRepository.findByPesel(patient.getPesel()).isPresent()) {
            throw new IllegalArgumentException("Pacjent z podanym numerem PESEL już istnieje.");
        }
        return patientRepository.save(patient);
    }

    @Transactional
    public Optional<Patient> updatePatient(Long id, Patient patientDetails) {
        return patientRepository.findById(id)
                .map(patient -> {
                    patient.setImie(patientDetails.getImie());
                    patient.setNazwisko(patientDetails.getNazwisko());
                    patient.setDataUrodzenia(patientDetails.getDataUrodzenia());
                    patient.setEmail(patientDetails.getEmail());
                    patient.setTelefon(patientDetails.getTelefon());
                    patient.setPesel(patientDetails.getPesel());
                    patient.setAdres(patientDetails.getAdres());
                    return patientRepository.save(patient);
                });
    }

    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Pacjent o podanym ID nie istnieje: " + id);
        }
        patientRepository.deleteById(id);
    }

    @Transactional
    public Optional<Patient> getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }
}
