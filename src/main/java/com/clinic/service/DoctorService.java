package com.clinic.service;

import com.clinic.model.Doctor;
import com.clinic.model.Visit;
import com.clinic.model.VisitStatus;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serwis odpowiedzialny za logikę biznesową związaną z lekarzami.
 * Wykorzystuje DoctorRepository i VisitRepository do interakcji z bazą danych.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;

    /**
     * Pobiera wszystkich lekarzy z bazy danych.
     * @return Lista wszystkich lekarzy.
     */
    @Transactional
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Pobiera lekarza o podanym ID.
     * @param id ID lekarza.
     * @return Opcjonalny obiekt Doctor, jeśli lekarz został znaleziony.
     */
    @Transactional
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    /**
     * Tworzy nowego lekarza.
     * @param doctor Obiekt lekarza do zapisania.
     * @return Zapisany obiekt lekarza.
     */
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Lekarz z podanym adresem email już istnieje.");
        }
        return doctorRepository.save(doctor);
    }

    /**
     * Aktualizuje dane istniejącego lekarza.
     * @param id ID lekarza do zaktualizowania.
     * @param doctorDetails Obiekt Doctor zawierający zaktualizowane dane.
     * @return Zaktualizowany obiekt lekarza, lub Optional.empty() jeśli lekarz nie istnieje.
     */
    @Transactional
    public Optional<Doctor> updateDoctor(Long id, Doctor doctorDetails) {
        return doctorRepository.findById(id).map(doctor -> {
            doctor.setImie(doctorDetails.getImie());
            doctor.setNazwisko(doctorDetails.getNazwisko());
            doctor.setSpecjalizacja(doctorDetails.getSpecjalizacja());
            doctor.setTelefon(doctorDetails.getTelefon());
            if (!doctor.getEmail().equals(doctorDetails.getEmail())) {
                if (doctorRepository.findByEmail(doctorDetails.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Nowy adres email jest już zajęty.");
                }
                doctor.setEmail(doctorDetails.getEmail());
            }
            return doctorRepository.save(doctor);
        });
    }

    /**
     * Usuwa lekarza o podanym ID.
     * @param id ID lekarza do usunięcia.
     */
    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new IllegalArgumentException("Lekarz o podanym ID nie istnieje.");
        }
        doctorRepository.deleteById(id);
    }

    /**
     * Pobiera dostępne terminy dla danego lekarza w określonym przedziale czasowym.
     * @param doctorId ID lekarza.
     * @param from Czas początkowy, od którego szukamy terminów.
     * @return Lista wizyt lekarza, które nie są anulowane i są po danym czasie.
     */
    @Transactional
    public List<Visit> getAvailableTerms(Long doctorId, LocalDateTime from) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Lekarz o podanym ID nie istnieje."));

        return visitRepository.findByDoctorAndStatusNotAndVisitDateTimeAfter(doctor, VisitStatus.CANCELLED, from)
                .stream()
                .filter(visit -> visit.getStatus().equals(VisitStatus.SCHEDULED)) // Tylko zaplanowane
                .collect(Collectors.toList());
    }

    /**
     * Znajduje lekarza po adresie e-mail.
     * @param email Adres e-mail lekarza.
     * @return Opcjonalny obiekt Doctor.
     */
    @Transactional
    public Optional<Doctor> getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }
}
