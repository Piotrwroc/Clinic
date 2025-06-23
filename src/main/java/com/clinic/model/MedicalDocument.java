package com.clinic.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
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
     * Pacjent, do którego należy dokument.
     * Relacja Many-to-One: wiele dokumentów może być dla jednego pacjenta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Wizyta, z którą dokument jest powiązany (opcjonalnie).
     * Relacja Many-to-One: wiele dokumentów może być powiązanych z jedną wizytą.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id") // visit_id może być NULL
    private Visit visit;
}
