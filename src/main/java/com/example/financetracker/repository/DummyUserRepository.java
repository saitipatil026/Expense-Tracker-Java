package com.example.financetracker.repository;

import com.example.financetracker.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stand-in for a real database repository. Holds users in a plain List in
 * memory - data resets every time the app restarts.
 *
 * Deliberately has NO authentication logic (no hashing, no JWT). Its only
 * job is storing and retrieving User objects, so that later it can be
 * swapped for a Spring Data JPA repository (backed by JpaRepository<User,
 * Long>) without AuthService or AuthController needing to change.
 */
@Repository
public class DummyUserRepository {

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public User save(String username, String email, String passwordHash) {
        User user = new User(idCounter.getAndIncrement(), username, email, passwordHash);
        users.add(user);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}
