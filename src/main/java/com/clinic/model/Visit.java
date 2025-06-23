package com.clinic.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Encja reprezentująca wizytę pacjenta u lekarza.
 * Zawiera informacje o terminie, lekarzu, pacjencie i statusie wizyty.
 */
@Entity
@Table(name = "visit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    /**
     * Unikalny identyfikator wizyty. Generowany automatycznie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data i godzina wizyty. Pole wymagane.
     */
    @Column(nullable = false)
    private LocalDateTime visitDateTime;

    /**
     * Status wizyty (Zaplanowana, Ukończona, Anulowana).
     * Używa typu ENUM do mapowania wartości.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status;

    /**
     * Lekarz, do którego jest umówiona wizyta.
     * Relacja Many-to-One: wiele wizyt może być do jednego lekarza.
     * JoinColumn określa kolumnę klucza obcego w tabeli wizyt.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading dla lepszej wydajności
    @JoinColumn(name = "doctor_id", nullable = false) // Kolumna klucza obcego w tabeli 'visit'
    private Doctor doctor;

    /**
     * Pacjent, dla którego jest umówiona wizyta.
     * Relacja Many-to-One: wiele wizyt może być dla jednego pacjenta.
     */
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading
    @JoinColumn(name = "patient_id", nullable = false) // Kolumna klucza obcego w tabeli 'visit'
    private Patient patient;

    /**
     * Lista dokumentów medycznych powiązanych z tą wizytą.
     * Relacja One-to-Many: jedna wizyta może mieć wiele dokumentów medycznych.
     */
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalDocument> medicalDocuments;
}
