package com.clinic.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Zmieniono na EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Klasa konfiguracyjna dla Spring Security.
 * Konfiguruje mechanizmy uwierzytelniania, autoryzacji i obsługę JWT.
 */
@Configuration
@EnableWebSecurity // Włącza wsparcie dla bezpieczeństwa webowego Spring Security
@EnableMethodSecurity(prePostEnabled = true) // Umożliwia użycie adnotacji @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Konfiguruje łańcuch filtrów bezpieczeństwa HTTP.
     * Definiuje zasady dostępu do URL, zarządzanie sesjami i dodaje filtr JWT.
     *
     * @param http Obiekt HttpSecurity do konfiguracji.
     * @return Skonfigurowany SecurityFilterChain.
     * @throws Exception w przypadku błędu konfiguracji.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Wyłączenie CSRF dla API RESTful (stanless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Bezstanowe zarządzanie sesjami
                .authorizeHttpRequests(auth -> auth
                        // Publiczne ścieżki (dostępne bez autentykacji)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Ścieżki wymagające określonych ról
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Tylko Administrator
                        .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA", "PACJENT") // Pacjent ma dostęp do swoich danych
                        .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA")
                        .requestMatchers("/api/visits/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA", "PACJENT") // Pacjent do swoich wizyt
                        .requestMatchers("/api/medical-documents/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA") // Pacjent do swoich dokumentów
                        // Dodaj /h2-console do publicznych ścieżek, jeśli używasz H2 Console
                        .requestMatchers("/h2-console/**").permitAll()
                        // Wszystkie inne żądania wymagają uwierzytelnienia
                        .anyRequest().authenticated()
                );

        // Dodanie niestandardowego filtra JWT przed filtrem UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        // Ważne: dla konsoli H2, która działa w ramkach, potrzebujesz wyłączyć zabezpieczenia ramek
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));


        return http.build();
    }

    /**
     * Bean dla PasswordEncoder, używający BCrypt do szyfrowania haseł.
     * @return Instancja BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean dla AuthenticationManager, który zarządza procesem uwierzytelniania.
     * @param authenticationConfiguration Konfiguracja autentykacji.
     * @return Instancja AuthenticationManager.
     * @throws Exception w przypadku błędu.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Bean dla niestandardowego filtra JWT.
     * @return Instancja JwtAuthenticationFilter.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }
}
