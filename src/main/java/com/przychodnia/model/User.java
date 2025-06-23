package com.przychodnia.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

/**
 * Encja reprezentująca użytkownika systemu (personel przychodni lub pacjent z dostępem do konta).
 * Zawiera dane uwierzytelniające i rolę użytkownika.
 */
@Entity
@Table(name = "app_user") // Używamy "app_user" aby uniknąć konfliktu z 'User' z pakietu Spring Security
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unikalny identyfikator użytkownika. Generowany automatycznie.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Adres e-mail użytkownika, używany jako login. Musi być unikalny. Pole wymagane.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Zaszyfrowane hasło użytkownika. Pole wymagane.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Rola użytkownika w systemie (np. ADMIN, LEKARZ, PACJENT, RECEPCJONISTA).
     * Określa uprawnienia dostępu.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
