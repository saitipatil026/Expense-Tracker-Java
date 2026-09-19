package com.example.expensetracker.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * Reachable only with a valid "Authorization: Bearer <token>" header.
     * SecurityConfig blocks everything under /api/** unless JwtAuthenticationFilter
     * already proved the request's identity.
     */
    @GetMapping("/test")
    public Map<String, String> test(Authentication authentication) {
        return Map.of(
                "message", "You are authenticated!",
                "user", authentication.getName()
        );
    }
}
