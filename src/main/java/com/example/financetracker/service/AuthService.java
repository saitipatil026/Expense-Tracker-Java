package com.example.financetracker.service;

import com.example.financetracker.dto.LoginRequest;
import com.example.financetracker.dto.LoginResponse;
import com.example.financetracker.dto.RegisterRequest;
import com.example.financetracker.model.User;
import com.example.financetracker.repository.DummyUserRepository;
import com.example.financetracker.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * All authentication business logic lives here, not in the controller:
 * checking for duplicate usernames, hashing/verifying passwords, and
 * issuing JWTs. AuthController just receives HTTP requests and delegates.
 */
@Service
public class AuthService {

    private final DummyUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(DummyUserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
        }

        // Only the hash is ever stored - the plaintext password never
        // touches the repository or leaves this method.
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        userRepository.save(request.getUsername(), request.getEmail(), hashedPassword);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Never do encode(password).equals(storedHash) - BCrypt hashes
        // include a random salt, so the same password encodes differently
        // every time. matches() re-derives the hash using the salt that's
        // embedded in storedHash and compares against that.
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token);
    }
}
