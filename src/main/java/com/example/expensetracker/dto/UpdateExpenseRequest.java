package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shape of the JSON body expected on PUT /api/expenses/{id}.
 * Same fields as CreateExpenseRequest — kept as a separate class because
 * "what a create needs" and "what an update needs" can diverge later
 * (e.g. partial updates) without the two meanings being tangled together.
 */
public class UpdateExpenseRequest {

    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDate date;

    public UpdateExpenseRequest() {
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
