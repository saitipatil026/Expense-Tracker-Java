package com.example.expensetracker.repository;

import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * No implementation needed — Spring Data JPA generates the SQL for these
 * from the method names alone, and JpaRepository already provides save(),
 * findById(), delete(), etc.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
