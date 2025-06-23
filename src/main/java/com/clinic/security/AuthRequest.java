package com.clinic.security;

// Usuń wszystkie importy Lomboka z tego pliku
// import lombok.*;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;


/**
 * Klasa do przechowania danych logowania (email i hasło) przesyłanych w żądaniu autentykacji.
 * Używana przez kontroler autentykacji.
 */
// Usuń adnotacje Lomboka z tej klasy
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class AuthRequest {
    private String email;
    private String password;

    // Dodaj jawny konstruktor bezargumentowy (NoArgsConstructor)
    public AuthRequest() {
    }

    // Dodaj jawny konstruktor ze wszystkimi argumentami (AllArgsConstructor)
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Dodaj jawne gettery dla pól email i password
    public String getEmail() {
        return email;
    }

    // Dodaj jawne settery dla pól email i password
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Opcjonalnie: możesz dodać tutaj również metody equals(), hashCode(), toString() ręcznie,
    // jeśli są używane w innych częściach kodu i potrzebujesz ich dla AuthRequest.
    // Na ogół dla DTO nie są aż tak krytyczne jak gettery/settery.
}