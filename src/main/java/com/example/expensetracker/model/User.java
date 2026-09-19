package com.example.expensetracker.model;

import jakarta.persistence.*;

/**
 * A registered user, backed by a real table in the SQLite database file
 * instead of a List. Table is explicitly named "users" — USER is a
 * reserved word in several SQL dialects, so naming it explicitly avoids
 * any ambiguity if the database is ever swapped again later.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    // Stores the BCrypt HASH only — the raw password is never persisted.
    @Column(nullable = false)
    private String passwordHash;

    public User() {
        // required by JPA/Hibernate
    }

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
