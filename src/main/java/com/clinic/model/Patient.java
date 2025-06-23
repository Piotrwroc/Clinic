package com.clinic.model;

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
@Data // Lombok generuje gettery, settery, equals, hashCode, toString
@NoArgsConstructor // Lombok generuje konstruktor bezargumentowy
@AllArgsConstructor // Lombok generuje konstruktor z wszystkimi argumentami
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
     * Lista wizyt przypisanych do tego pacjenta.
     * Relacja One-to-Many: jeden pacjent może mieć wiele wizyt.
     * mappedBy wskazuje pole w encji Wizyta, które mapuje tę relację.
     * CascadeType.ALL oznacza, że operacje (np. usunięcie) na pacjencie kaskadowo wpłyną na wizyty.
     * orphanRemoval = true zapewnia usunięcie wizyt, które nie są już powiązane z żadnym pacjentem.
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits;

    /**
     * Lista dokumentów medycznych przypisanych do tego pacjenta.
     * Relacja One-to-Many: jeden pacjent może mieć wiele dokumentów medycznych.
     * mappedBy wskazuje pole w encji MedicalDocument, które mapuje tę relację.
     * CascadeType.ALL i orphanRemoval = true działają podobnie jak dla wizyt.
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalDocument> medicalDocuments;

    // Bez getterów, setterów, equals, hashCode, toString - generuje LOMBOK (@Data)
    // Konstruktor bezargumentowy i z wszystkimi argumentami generuje LOMBOK (@NoArgsConstructor, @AllArgsConstructor)
}
