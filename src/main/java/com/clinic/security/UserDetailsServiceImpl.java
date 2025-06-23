package com.clinic.security;

import com.clinic.model.User;
import com.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Implementacja interfejsu UserDetailsService, używana przez Spring Security
 * do pobierania danych użytkownika na podstawie nazwy użytkownika (tutaj email).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Ładuje dane użytkownika na podstawie jego emaila.
     * @param email Email użytkownika.
     * @return Obiekt UserDetails reprezentujący użytkownika.
     * @throws UsernameNotFoundException Jeśli użytkownik o podanym emailu nie został znaleziony.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Pobierz użytkownika z bazy danych
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika o emailu: " + email));

        // Utwórz listę uprawnień (ról) dla użytkownika
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));

        // Zwróć obiekt Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
