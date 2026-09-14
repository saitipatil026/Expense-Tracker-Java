package com.example.financetracker.dto;

/** Shape of the JSON response for a successful POST /auth/login. */
public class LoginResponse {

    private final String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
