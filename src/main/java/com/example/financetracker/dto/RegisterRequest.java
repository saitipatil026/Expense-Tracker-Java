package com.example.financetracker.dto;

/**
 * Shape of the JSON body for POST /auth/register.
 * Kept separate from User.java so the API contract doesn't leak internal
 * fields (like passwordHash) and can evolve independently of the model.
 */
public class RegisterRequest {

    private String username;
    private String email;
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
