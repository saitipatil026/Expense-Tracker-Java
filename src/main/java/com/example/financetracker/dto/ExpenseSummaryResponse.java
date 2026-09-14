package com.example.financetracker.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Shape of the JSON response for GET /api/expenses/summary. */
public class ExpenseSummaryResponse {

    private final BigDecimal totalAmount;
    private final Map<String, BigDecimal> totalsByCategory;

    public ExpenseSummaryResponse(BigDecimal totalAmount, Map<String, BigDecimal> totalsByCategory) {
        this.totalAmount = totalAmount;
        this.totalsByCategory = totalsByCategory;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Map<String, BigDecimal> getTotalsByCategory() {
        return totalsByCategory;
    }
}
