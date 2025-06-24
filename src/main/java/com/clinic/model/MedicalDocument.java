package com.clinic.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Encja reprezentująca dokument medyczny pacjenta.
 * Może to być historia choroby, wyniki badań, itp.
 */
@Entity
@Table(name = "medical_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDocument {

    /**
     * Unikalny identyfikator dokumentu medycznego. Generowany automatycznie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nazwa dokumentu (np. "Wyniki badań krwi", "Karta informacyjna"). Pole wymagane.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Treść dokumentu medycznego. Może być długa.
     */
    @Lob // Oznacza, że to pole może przechowywać duże obiekty (CLOB/BLOB)
    @Column(nullable = false)
    private String content;

    /**
     * Data i czas utworzenia dokumentu. Pole wymagane.
     */
    @Column(nullable = false)
    private LocalDateTime creationDate;

    /**
     * Relacja Many-to-One z Pacjentem.
     * @JsonBackReference oznacza, że to jest strona "odwrotna" relacji.
     * Nie będzie renderowana podczas serializacji Dokumentu Medycznego, aby uniknąć cyklicznej pętli z Pacjentem.
     * Nazwa "patient-medicalDocuments" łączy ją z @JsonManagedReference w Patient.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference("patient-medicalDocuments")
    private Patient patient;

    /**
     * Relacja Many-to-One z Wizytą (opcjonalna).
     * @JsonBackReference oznacza, że to jest strona "odwrotna" relacji.
     * Nie będzie renderowana podczas serializacji Dokumentu Medycznego, aby uniknąć cyklicznej pętli z Wizytą.
     * Nazwa "visit-medicalDocuments" łączy ją z @JsonManagedReference w Visit.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    @JsonBackReference("visit-medicalDocuments") //
    private Visit visit;
}
