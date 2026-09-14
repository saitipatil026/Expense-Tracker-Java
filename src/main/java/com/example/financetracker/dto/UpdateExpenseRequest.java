package com.example.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Shape of the JSON body for PUT /api/expenses/{id}. Full replace, like standard REST PUT. */
public class UpdateExpenseRequest {

    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDate date;

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
