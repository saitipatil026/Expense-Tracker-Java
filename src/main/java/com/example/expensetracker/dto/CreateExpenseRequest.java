package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shape of the JSON body expected on POST /api/expenses.
 * "date" is sent as "yyyy-MM-dd" — Jackson converts it to LocalDate automatically.
 */
public class CreateExpenseRequest {

    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDate date;

    public CreateExpenseRequest() {
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
