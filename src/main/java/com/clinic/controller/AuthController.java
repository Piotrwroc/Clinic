package com.clinic.controller;

import com.clinic.model.Role;
import com.clinic.security.AuthRequest;
import com.clinic.security.AuthResponse;
import com.clinic.security.JwtTokenProvider;
import com.clinic.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Kontroler odpowiedzialny za obsługę żądań autentykacji (rejestracja i logowanie).
 */
@RestController
@RequestMapping("/api/auth")
@Data
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    /**
     * Endpoint do logowania użytkowników.
     * Po udanej autentykacji, generuje token JWT i zwraca go klientowi.
     * @param authRequest Obiekt zawierający email i hasło.
     * @return ResponseEntity z tokenem JWT w przypadku sukcesu lub błędem.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Uwierzytelnienie użytkownika za pomocą AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            // Ustawienie uwierzytelnienia w kontekście bezpieczeństwa Springa
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generowanie tokena JWT
            String jwt = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new AuthResponse(jwt, "Zalogowano pomyślnie!"));
        } catch (Exception e) {
            // Zwróć błąd w przypadku nieudanej autentykacji (np. złe dane logowania)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Błąd autentykacji: " + e.getMessage());
        }
    }

    /**
     * Endpoint do rejestracji nowego użytkownika.
     * @param authRequest Obiekt zawierający email i hasło.
     * @return ResponseEntity z wiadomością o sukcesie lub błędzie.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Domyślnie rejestrujemy jako PACJENT. Administrator może później zmienić rolę.
            // W rzeczywistym systemie, rejestracja lekarzy/adminów byłaby oddzielnym procesem.
            userService.registerUser(authRequest.getEmail(), authRequest.getPassword(), Role.ROLE_PACJENT);
            return ResponseEntity.status(HttpStatus.CREATED).body("Użytkownik zarejestrowany pomyślnie jako PACJENT.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas rejestracji użytkownika: " + e.getMessage());
        }
    }

    /**
     * Endpoint do rejestracji lekarzy przez administratora.
     * Wymaga roli ADMIN.
     * @param authRequest Dane do rejestracji lekarza.
     * @return ResponseEntity z wiadomością o sukcesie lub błędzie.
     */
    @PostMapping("/register/doctor")
    // @PreAuthorize("hasRole('ADMIN')") // Tylko admin może rejestrować lekarzy
    public ResponseEntity<?> registerDoctor(@Valid @RequestBody AuthRequest authRequest) {
        try {
            userService.registerUser(authRequest.getEmail(), authRequest.getPassword(), Role.ROLE_LEKARZ);
            return ResponseEntity.status(HttpStatus.CREATED).body("Lekarz zarejestrowany pomyślnie.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas rejestracji lekarza: " + e.getMessage());
        }
    }

    /**
     * Endpoint do rejestracji administratorów.
     * Powinien być używany tylko podczas inicjalizacji systemu lub przez istniejącego administratora.
     * @param authRequest Dane do rejestracji administratora.
     * @return ResponseEntity z wiadomością o sukcesie lub błędzie.
     */
    @PostMapping("/register/admin")
    // @PreAuthorize("hasRole('ADMIN')") // Opcjonalnie: tylko admin może rejestrować innych adminów
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AuthRequest authRequest) {
        try {
            userService.registerUser(authRequest.getEmail(), authRequest.getPassword(), Role.ROLE_ADMIN);
            return ResponseEntity.status(HttpStatus.CREATED).body("Administrator zarejestrowany pomyślnie.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas rejestracji administratora: " + e.getMessage());
        }
    }

    /**
     * Endpoint do rejestracji recepcjonistów.
     * Wymaga roli ADMIN.
     * @param authRequest Dane do rejestracji recepcjonisty.
     * @return ResponseEntity z wiadomością o sukcesie lub błędzie.
     */
    @PostMapping("/register/receptionist")
    // @PreAuthorize("hasRole('ADMIN')") // Tylko admin może rejestrować recepcjonistów
    public ResponseEntity<?> registerReceptionist(@Valid @RequestBody AuthRequest authRequest) {
        try {
            userService.registerUser(authRequest.getEmail(), authRequest.getPassword(), Role.ROLE_RECEPCJONISTA);
            return ResponseEntity.status(HttpStatus.CREATED).body("Recepcjonista zarejestrowany pomyślnie.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd podczas rejestracji recepcjonisty: " + e.getMessage());
        }
    }
}
