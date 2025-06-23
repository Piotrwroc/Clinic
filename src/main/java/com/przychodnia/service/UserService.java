package com.przychodnia.service;

import com.przychodnia.model.Role;
import com.przychodnia.model.User;
import com.przychodnia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Serwis odpowiedzialny za logikę biznesową związaną z użytkownikami.
 * Obsługuje rejestrację, logowanie (pośrednio przez Spring Security) i zarządzanie użytkownikami.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Do szyfrowania haseł

    /**
     * Rejestruje nowego użytkownika w systemie.
     * Hasło jest szyfrowane przed zapisaniem do bazy danych.
     * @param email Adres e-mail użytkownika.
     * @param password Hasło użytkownika.
     * @param role Rola użytkownika.
     * @return Zarejestrowany obiekt User.
     * @throws IllegalArgumentException Jeśli użytkownik z podanym adresem e-mail już istnieje.
     */
    @Transactional
    public User registerUser(String email, String password, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Użytkownik z podanym adresem email już istnieje.");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // Szyfrowanie hasła
        newUser.setRole(role);
        return userRepository.save(newUser);
    }

    /**
     * Pobiera użytkownika po ID.
     * @param id ID użytkownika.
     * @return Opcjonalny obiekt User.
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Pobiera użytkownika po adresie e-mail.
     * @param email Adres e-mail użytkownika.
     * @return Opcjonalny obiekt User.
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Pobiera wszystkich użytkowników z bazy danych.
     * @return Lista wszystkich użytkowników.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Aktualizuje rolę użytkownika.
     * @param id ID użytkownika.
     * @param newRole Nowa rola.
     * @return Zaktualizowany obiekt User.
     * @throws IllegalArgumentException Jeśli użytkownik nie istnieje.
     */
    @Transactional
    public Optional<User> updateUserRole(Long id, Role newRole) {
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            return userRepository.save(user);
        });
    }

    /**
     * Usuwa użytkownika o podanym ID.
     * @param id ID użytkownika do usunięcia.
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Użytkownik o podanym ID nie istnieje.");
        }
        userRepository.deleteById(id);
    }
}
