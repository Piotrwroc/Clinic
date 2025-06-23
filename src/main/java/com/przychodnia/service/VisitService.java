package com.przychodnia.service;

import com.przychodnia.model.Doctor;
import com.przychodnia.model.Patient;
import com.przychodnia.model.Visit;
import com.przychodnia.model.VisitStatus;
import com.przychodnia.repository.DoctorRepository;
import com.przychodnia.repository.PatientRepository;
import com.przychodnia.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serwis odpowiedzialny za logikę biznesową związaną z wizytami.
 * Zarządza procesem umawiania, odwoływania i edytowania wizyt.
 */
@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Pobiera wszystkie wizyty z bazy danych.
     * @return Lista wszystkich wizyt.
     */
    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    /**
     * Pobiera wizytę o podanym ID.
     * @param id ID wizyty.
     * @return Opcjonalny obiekt Visit, jeśli wizyta została znaleziona.
     */
    public Optional<Visit> getVisitById(Long id) {
        return visitRepository.findById(id);
    }

    /**
     * Planuje nową wizytę.
     * Sprawdza dostępność lekarza w danym terminie.
     * @param patientId ID pacjenta.
     * @param doctorId ID lekarza.
     * @param visitDateTime Data i godzina wizyty.
     * @return Zapisana wizyta.
     * @throws IllegalArgumentException Jeśli pacjent/lekarz nie istnieje lub termin jest zajęty.
     */
    @Transactional
    public Visit scheduleVisit(Long patientId, Long doctorId, LocalDateTime visitDateTime) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacjent o podanym ID nie istnieje."));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Lekarz o podanym ID nie istnieje."));

        // Sprawdzenie dostępności lekarza
        // Zakładamy, że wizyty trwają 30 minut. Należy dostosować do faktycznych wymagań.
        LocalDateTime endDateTime = visitDateTime.plusMinutes(30);
        List<Visit> conflictingVisits = visitRepository.findByDoctorAndVisitDateTimeBetween(doctor, visitDateTime.minusMinutes(29), endDateTime.minusMinutes(1));

        // Filtrowanie tylko wizyt, które są nadal zaplanowane
        boolean isConflicting = conflictingVisits.stream()
                .anyMatch(v -> v.getStatus().equals(VisitStatus.SCHEDULED));

        if (isConflicting) {
            throw new IllegalArgumentException("Lekarz jest już zajęty w tym terminie.");
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setVisitDateTime(visitDateTime);
        visit.setStatus(VisitStatus.SCHEDULED); // Nowa wizyta zawsze ma status "zaplanowana"

        return visitRepository.save(visit);
    }

    /**
     * Aktualizuje dane istniejącej wizyty.
     * @param id ID wizyty do zaktualizowania.
     * @param visitDetails Obiekt Visit zawierający zaktualizowane dane.
     * @return Zaktualizowany obiekt wizyty, lub Optional.empty() jeśli wizyta nie istnieje.
     * @throws IllegalArgumentException Jeśli termin jest zajęty po zmianie.
     */
    @Transactional
    public Optional<Visit> updateVisit(Long id, Visit visitDetails) {
        return visitRepository.findById(id).map(visit -> {
            // Sprawdzenie dostępności terminu tylko jeśli data/czas lub lekarz się zmieniły
            if (!visit.getVisitDateTime().equals(visitDetails.getVisitDateTime()) ||
                    !visit.getDoctor().getId().equals(visitDetails.getDoctor().getId())) {

                LocalDateTime endDateTime = visitDetails.getVisitDateTime().plusMinutes(30);
                List<Visit> conflictingVisits = visitRepository.findByDoctorAndVisitDateTimeBetween(
                        visitDetails.getDoctor(), visitDetails.getVisitDateTime().minusMinutes(29), endDateTime.minusMinutes(1)
                );

                boolean isConflicting = conflictingVisits.stream()
                        .anyMatch(v -> !v.getId().equals(id) && v.getStatus().equals(VisitStatus.SCHEDULED)); // Wykluczamy aktualizowaną wizytę

                if (isConflicting) {
                    throw new IllegalArgumentException("Nowy termin jest zajęty dla wybranego lekarza.");
                }
            }

            visit.setVisitDateTime(visitDetails.getVisitDateTime());
            visit.setStatus(visitDetails.getStatus());
            // Zmiana pacjenta lub lekarza wymaga pobrania encji z bazy
            if (visitDetails.getPatient() != null && !visit.getPatient().getId().equals(visitDetails.getPatient().getId())) {
                patientRepository.findById(visitDetails.getPatient().getId())
                        .ifPresent(visit::setPatient);
            }
            if (visitDetails.getDoctor() != null && !visit.getDoctor().getId().equals(visitDetails.getDoctor().getId())) {
                doctorRepository.findById(visitDetails.getDoctor().getId())
                        .ifPresent(visit::setDoctor);
            }

            return visitRepository.save(visit);
        });
    }

    /**
     * Zmienia status wizyty na "Anulowana".
     * @param id ID wizyty do anulowania.
     * @return Zaktualizowany obiekt wizyty.
     * @throws IllegalArgumentException Jeśli wizyta nie istnieje lub jest już ukończona/anulowana.
     */
    @Transactional
    public Optional<Visit> cancelVisit(Long id) {
        return visitRepository.findById(id).map(visit -> {
            if (visit.getStatus().equals(VisitStatus.COMPLETED)) {
                throw new IllegalArgumentException("Nie można anulować wizyty, która już się odbyła.");
            }
            visit.setStatus(VisitStatus.CANCELLED);
            return visitRepository.save(visit);
        });
    }

    /**
     * Zmienia status wizyty na "Ukończona".
     * @param id ID wizyty do oznaczenia jako ukończona.
     * @return Zaktualizowany obiekt wizyty.
     * @throws IllegalArgumentException Jeśli wizyta nie istnieje lub jest już ukończona/anulowana.
     */
    @Transactional
    public Optional<Visit> completeVisit(Long id) {
        return visitRepository.findById(id).map(visit -> {
            if (visit.getStatus().equals(VisitStatus.COMPLETED)) {
                throw new IllegalArgumentException("Wizyta już jest oznaczona jako ukończona.");
            }
            if (visit.getStatus().equals(VisitStatus.CANCELLED)) {
                throw new IllegalArgumentException("Nie można ukończyć anulowanej wizyty.");
            }
            visit.setStatus(VisitStatus.COMPLETED);
            return visitRepository.save(visit);
        });
    }

    /**
     * Usuwa wizytę o podanym ID.
     * @param id ID wizyty do usunięcia.
     */
    @Transactional
    public void deleteVisit(Long id) {
        if (!visitRepository.existsById(id)) {
            throw new IllegalArgumentException("Wizyta o podanym ID nie istnieje.");
        }
        visitRepository.deleteById(id);
    }

    /**
     * Pobiera historię wizyt dla danego pacjenta.
     * @param patientId ID pacjenta.
     * @return Lista wizyt pacjenta.
     * @throws IllegalArgumentException Jeśli pacjent nie istnieje.
     */
    public List<Visit> getPatientVisitHistory(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Pacjent o podanym ID nie istnieje."));
        return visitRepository.findByPatientOrderByVisitDateTimeAsc(patient);
    }

    /**
     * Pobiera historię wizyt dla danego lekarza.
     * @param doctorId ID lekarza.
     * @return Lista wizyt lekarza.
     * @throws IllegalArgumentException Jeśli lekarz nie istnieje.
     */
    public List<Visit> getDoctorVisitHistory(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Lekarz o podanym ID nie istnieje."));
        return visitRepository.findByDoctorOrderByVisitDateTimeAsc(doctor);
    }
}
