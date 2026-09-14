package com.example.financetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single expense entry, owned by exactly one user (by username).
 * Ownership never changes after creation, so it's final; the other
 * fields can be edited via PUT /api/expenses/{id}.
 */
public class Expense {

    private final Long id;
    private final String ownerUsername;
    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDate date;

    public Expense(Long id, String ownerUsername, String description,
                    BigDecimal amount, String category, LocalDate date) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
