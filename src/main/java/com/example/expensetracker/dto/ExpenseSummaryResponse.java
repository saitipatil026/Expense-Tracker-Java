package com.example.expensetracker.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Shape of the JSON returned by GET /api/expenses/summary.
 */
public class ExpenseSummaryResponse {

    private BigDecimal totalAmount;
    private Map<String, BigDecimal> categoryTotals;

    public ExpenseSummaryResponse() {
    }

    public ExpenseSummaryResponse(BigDecimal totalAmount, Map<String, BigDecimal> categoryTotals) {
        this.totalAmount = totalAmount;
        this.categoryTotals = categoryTotals;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Map<String, BigDecimal> getCategoryTotals() {
        return categoryTotals;
    }

    public void setCategoryTotals(Map<String, BigDecimal> categoryTotals) {
        this.categoryTotals = categoryTotals;
    }
}
