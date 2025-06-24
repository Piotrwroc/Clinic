package com.clinic.security;

import lombok.*;

/**
 * Klasa do przechowania danych logowania (email i hasło) przesyłanych w żądaniu autentykacji.
 * Używana przez kontroler autentykacji.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String email;
    private String password;
}
