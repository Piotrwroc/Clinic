package com.clinic.security;

import lombok.*;

/**
 * Klasa do przechowania danych odpowiedzi po udanej autentykacji.
 * Zawiera token JWT, który klient powinien przechowywać i wysyłać w kolejnych żądaniach.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String message; // Opcjonalna wiadomość
}
