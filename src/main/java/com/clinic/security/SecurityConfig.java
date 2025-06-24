package com.clinic.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
                // Wyłączenie CSRF dla API RESTful (stateless) - ważne dla JWT
                .csrf(AbstractHttpConfigurer::disable)
                // Bezstanowe zarządzanie sesjami - kluczowe dla JWT, aby nie używać sesji serwera
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpointy autentykacji (login i register) dostępne dla wszystkich
                        .requestMatchers("/api/auth/**").permitAll()
                        // Konsola H2 - dostępna dla wszystkich (z regułą wyłączenia ramek poniżej)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Endpoint błędu - powinien być dostępny dla wszystkich, aby wyświetlać komunikaty o błędach
                        .requestMatchers("/error").permitAll() // DODANA LINIA
                        // Możesz dodać inne publiczne zasoby, np. statyczne pliki
                        // .requestMatchers("/public/**", "/images/**").permitAll()

                        // --- Ścieżki wymagające autoryzacji opartej na rolach ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA", "PACJENT")
                        .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA")
                        .requestMatchers("/api/visits/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA", "PACJENT")
                        .requestMatchers("/api/medical-documents/**").hasAnyRole("ADMIN", "LEKARZ", "RECEPCJONISTA")
                        // Wszystkie inne żądania wymagają uwierzytelnienia
                        .anyRequest().authenticated()
                );

        // Dodanie niestandardowego filtra JWT przed filtrem UsernamePasswordAuthenticationFilter
        // Ten filtr będzie odpowiedzialny za parsowanie tokenów JWT i ustawianie kontekstu bezpieczeństwa
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Ważne: Dla konsoli H2, która działa w ramkach (iframe), potrzebujesz wyłączyć zabezpieczenia ramek
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    /**
     * Bean dla PasswordEncoder, używający BCrypt do szyfrowania haseł.
     * Zalecane jest użycie silnego algorytmu haszującego.
     * @return Instancja BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean dla AuthenticationManager, który zarządza procesem uwierzytelniania.
     * Jest potrzebny do ręcznego uwierzytelniania użytkowników (np. w endpointcie logowania).
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
     * Filtr ten jest odpowiedzialny za wyodrębnianie i walidację tokenów JWT
     * z nagłówków żądań i ustawianie uwierzytelnienia w Spring Security Context.
     * @return Instancja JwtAuthenticationFilter.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }
}
