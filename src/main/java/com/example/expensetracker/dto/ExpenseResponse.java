package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shape of the JSON returned for a single expense.
 * Deliberately does NOT include the owning User — the client already knows
 * who it's authenticated as, and there's no reason to expose one user's
 * internal id/email while looking at their own expense list.
 */
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDate date;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, String description, BigDecimal amount, String category, LocalDate date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
