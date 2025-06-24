package com.clinic.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Encja reprezentująca pacjenta w systemie przychodni.
 * Zawiera podstawowe dane osobowe pacjenta oraz powiązania z wizytami i dokumentacją medyczną.
 */
@Entity
@Table(name = "patient") // Nazwa tabeli w bazie danych
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    /**
     * Unikalny identyfikator pacjenta. Generowany automatycznie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Imię pacjenta. Pole wymagane.
     */
    @Column(nullable = false)
    private String imie;

    /**
     * Nazwisko pacjenta. Pole wymagane.
     */
    @Column(nullable = false)
    private String nazwisko;

    /**
     * Data urodzenia pacjenta.
     */
    private LocalDate dataUrodzenia;

    /**
     * Adres e-mail pacjenta. Musi być unikalny i jest polem wymaganym.
     * Używany jako identyfikator użytkownika w systemie (jeśli pacjent ma konto).
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Numer telefonu pacjenta.
     */
    private String telefon;

    /**
     * Numer PESEL pacjenta. Unikalny identyfikator.
     */
    @Column(unique = true)
    private String pesel;

    /**
     * Adres zamieszkania pacjenta.
     */
    private String adres;

    /**
     * Relacja One-to-Many z Wizytami.
     * @JsonManagedReference oznacza, że to jest strona "zarządzająca" serializacją,
     * i będzie renderować listę wizyt. Nazwa "patient-visits" łączy ją z @JsonBackReference w Visit.
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("patient-visits")
    private List<Visit> visits;

    /**
     * Relacja One-to-Many z Dokumentami Medycznymi.
     * @JsonManagedReference oznacza, że to jest strona "zarządzająca" serializacją,
     * i będzie renderować listę dokumentów. Nazwa "patient-medicalDocuments" łączy ją z @JsonBackReference w MedicalDocument.
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("patient-medicalDocuments")
    private List<MedicalDocument> medicalDocuments;
}
