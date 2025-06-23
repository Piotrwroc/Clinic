package com.clinic.repository;

import com.clinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium do zarządzania encją User.
 * Rozszerza JpaRepository, zapewniając podstawowe operacje CRUD.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Znajduje użytkownika po adresie e-mail.
     * Jest to kluczowe dla uwierzytelniania w Spring Security.
     * @param email Adres e-mail użytkownika.
     * @return Opcjonalny obiekt User, jeśli znaleziono.
     */
    Optional<User> findByEmail(String email);
}
