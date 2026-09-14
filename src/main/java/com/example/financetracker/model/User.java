package com.example.financetracker.model;

/**
 * Represents a user stored in the dummy in-memory repository.
 * Note: only the BCrypt hash is ever stored - never the plaintext password.
 */
public class User {

    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;

    public User(Long id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
