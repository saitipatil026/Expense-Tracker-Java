package com.example.financetracker.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A single protected endpoint used purely to prove the JWT flow works:
 * no token (or an invalid one) -> 401 from Spring Security before this
 * method ever runs. Valid token -> this method runs and can read the
 * authenticated username off the Authentication object that
 * JwtAuthenticationFilter placed in the SecurityContext.
 */
@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test(Authentication authentication) {
        return "Hello, " + authentication.getName() + "! You accessed a protected endpoint.";
    }
}
