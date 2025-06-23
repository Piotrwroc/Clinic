package com.clinic.repository;

import com.clinic.model.Doctor;
import com.clinic.model.Patient;
import com.clinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium do zarządzania encją Visit.
 * Rozszerza JpaRepository, zapewniając podstawowe operacje CRUD.
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    /**
     * Znajduje wszystkie wizyty dla danego pacjenta, posortowane chronologicznie.
     * @param patient Obiekt pacjenta.
     * @return Lista wizyt pacjenta.
     */
    List<Visit> findByPatientOrderByVisitDateTimeAsc(Patient patient);

    /**
     * Znajduje wszystkie wizyty dla danego lekarza, posortowane chronologicznie.
     * @param doctor Obiekt lekarza.
     * @return Lista wizyt lekarza.
     */
    List<Visit> findByDoctorOrderByVisitDateTimeAsc(Doctor doctor);

    /**
     * Sprawdza, czy w danym przedziale czasowym lekarz ma już wizytę.
     * @param doctor Obiekt lekarza.
     * @param start Przedział czasowy (początek).
     * @param end Przedział czasowy (koniec).
     * @return Lista wizyt kolidujących z danym przedziałem.
     */
    List<Visit> findByDoctorAndVisitDateTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    /**
     * Znajduje wizyty dla danego lekarza, których status nie jest anulowany,
     * i których data jest po podanej dacie/czasie.
     * Używane do sprawdzania przyszłych dostępnych terminów (jeśli status nie jest 'CANCELLED').
     * @param doctor Lekarz.
     * @param dateTime Data i czas.
     * @return Lista wizyt.
     */
    List<Visit> findByDoctorAndStatusNotAndVisitDateTimeAfter(Doctor doctor, com.clinic.model.VisitStatus status, LocalDateTime dateTime);
}
