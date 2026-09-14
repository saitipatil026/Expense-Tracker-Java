package com.example.financetracker.repository;

import com.example.financetracker.model.Expense;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Same pattern as DummyUserRepository: a plain in-memory List standing in
 * for a real database. Every lookup takes ownerUsername, so one user can
 * never read/edit/delete another user's expenses - that check happens
 * here rather than in the controller or service, since it's fundamentally
 * a data-access concern.
 */
@Repository
public class DummyExpenseRepository {

    private final List<Expense> expenses = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Expense save(String ownerUsername, String description, BigDecimal amount, String category, LocalDate date) {
        Expense expense = new Expense(idCounter.getAndIncrement(), ownerUsername, description, amount, category, date);
        expenses.add(expense);
        return expense;
    }

    public List<Expense> findAllByOwner(String ownerUsername) {
        return expenses.stream()
                .filter(e -> e.getOwnerUsername().equals(ownerUsername))
                .collect(Collectors.toList());
    }

    public Optional<Expense> findByIdAndOwner(Long id, String ownerUsername) {
        return expenses.stream()
                .filter(e -> e.getId().equals(id) && e.getOwnerUsername().equals(ownerUsername))
                .findFirst();
    }

    public boolean deleteByIdAndOwner(Long id, String ownerUsername) {
        return expenses.removeIf(e -> e.getId().equals(id) && e.getOwnerUsername().equals(ownerUsername));
    }
}
