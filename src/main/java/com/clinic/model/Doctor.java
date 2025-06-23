package com.clinic.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import jakarta.persistence.*;
import java.util.List;

/**
 * Encja reprezentująca lekarza w systemie przychodni.
 * Zawiera dane osobowe lekarza, jego specjalizację oraz powiązania z wizytami.
 */
@Entity
@Table(name = "doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    /**
     * Unikalny identyfikator lekarza. Generowany automatycznie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Imię lekarza. Pole wymagane.
     */
    @Column(nullable = false)
    private String imie;

    /**
     * Nazwisko lekarza. Pole wymagane.
     */
    @Column(nullable = false)
    private String nazwisko;

    /**
     * Specjalizacja lekarza (np. "Pediatra", "Kardiolog"). Pole wymagane.
     */
    @Column(nullable = false)
    private String specjalizacja;

    /**
     * Adres e-mail lekarza. Musi być unikalny i jest polem wymaganym.
     * Używany jako identyfikator użytkownika w systemie.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Numer telefonu lekarza.
     */
    private String telefon;

    /**
     * Lista wizyt przypisanych do tego lekarza.
     * Relacja One-to-Many: jeden lekarz może mieć wiele wizyt.
     * mappedBy wskazuje pole w encji Wizyta, które mapuje tę relację.
     */
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits;
}
