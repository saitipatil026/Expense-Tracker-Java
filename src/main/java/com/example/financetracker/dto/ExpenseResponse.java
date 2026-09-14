package com.example.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What gets sent back to the client. Leaves out ownerUsername on purpose -
 * the client already knows who it's logged in as, so echoing it back on
 * every expense would just be noise.
 */
public class ExpenseResponse {

    private final Long id;
    private final String description;
    private final BigDecimal amount;
    private final String category;
    private final LocalDate date;

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

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }
}
