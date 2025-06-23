// src/main/java/com/przychodnia/security/JwtTokenProvider.java
package com.przychodnia.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Dodano import
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static io.jsonwebtoken.Jwts.*;

/**
 * Klasa odpowiedzialna za generowanie, walidację i parsowanie tokenów JWT.
 */
@Component
public class JwtTokenProvider {

    // Wartość klucza tajnego pobierana z application.properties
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // Czas ważności tokena (w milisekundach)
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private Key key;

    /**
     * Metoda inicjalizująca klucz po wstrzyknięciu wartości jwtSecret.
     * Klucz musi być wystarczająco długi (min. 256 bitów)
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generuje token JWT dla danego uwierzytelnienia.
     * @param authentication Obiekt Authentication zawierający dane użytkownika i jego role.
     * @return Wygenerowany token JWT.
     */
    public String generateToken(Authentication authentication) {
        String email = authentication.getName(); // Email użytkownika
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Mapowanie ról na Stringi oddzielone przecinkami
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return builder()
                .setSubject(email) // Podmiotem jest email użytkownika
                .claim("roles", roles) // Dodanie ról jako custom claim
                .setIssuedAt(new Date()) // Data wystawienia
                .setExpiration(expiryDate) // Data wygaśnięcia
                .signWith(key, SignatureAlgorithm.HS512) // Podpisanie kluczem HMAC SHA-512
                .compact();
    }

    /**
     * Pobiera dane uwierzytelnienia z tokena JWT.
     * @param token Token JWT.
     * @param userDetailsService Serwis do ładowania danych użytkownika.
     * @return Obiekt Authentication.
     */
    public Authentication getAuthentication(String token, UserDetailsService userDetailsService) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();
        String roles = claims.get("roles", String.class);

        // Tworzenie listy GrantedAuthority z ról
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(roles.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    /**
     * Waliduje token JWT.
     * @param authToken Token JWT do walidacji.
     * @return true, jeśli token jest prawidłowy, false w przeciwnym razie.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // Logowanie błędu: Invalid JWT signature
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            // Logowanie błędu: Invalid JWT token
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            // Logowanie błędu: Expired JWT token
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            // Logowanie błędu: Unsupported JWT token
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            // Logowanie błędu: JWT claims string is empty.
            System.err.println("JWT claims string is empty.");
        }
        return false;
    }

    /**
     * Zwraca klucz używany do podpisywania i weryfikacji JWT.
     * Potrzebne w JwtAuthenticationFilter.
     * @return Klucz prywatny.
     */
    public Key getKey() { // Zmieniono widoczność na publiczną
        return this.key;
    }
}
