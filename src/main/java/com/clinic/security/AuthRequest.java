package com.clinic.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Klasa do przechowania danych logowania (email i hasło) przesyłanych w żądaniu autentykacji.
 * Używana przez kontroler autentykacji.
 */
@Data // Lombok generuje gettery, settery, equals, hashCode, toString
@NoArgsConstructor // Lombok generuje konstruktor bezargumentowy
@AllArgsConstructor // Lombok generuje konstruktor z wszystkimi argumentami
public class AuthRequest {
    private String email;
    private String password;
}
