package com.example.financetracker.service;

import com.example.financetracker.dto.CreateExpenseRequest;
import com.example.financetracker.dto.ExpenseResponse;
import com.example.financetracker.dto.ExpenseSummaryResponse;
import com.example.financetracker.dto.UpdateExpenseRequest;
import com.example.financetracker.model.Expense;
import com.example.financetracker.repository.DummyExpenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final DummyExpenseRepository expenseRepository;

    public ExpenseService(DummyExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseResponse create(String username, CreateExpenseRequest request) {
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();
        Expense expense = expenseRepository.save(
                username, request.getDescription(), request.getAmount(), request.getCategory(), date);
        return toResponse(expense);
    }

    public List<ExpenseResponse> listForUser(String username) {
        return expenseRepository.findAllByOwner(username).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExpenseResponse getOne(String username, Long id) {
        Expense expense = findOwnedOrThrow(username, id);
        return toResponse(expense);
    }

    public ExpenseResponse update(String username, Long id, UpdateExpenseRequest request) {
        Expense expense = findOwnedOrThrow(username, id);

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate() != null ? request.getDate() : expense.getDate());

        return toResponse(expense);
    }

    public ExpenseSummaryResponse getSummary(String username) {
        List<Expense> userExpenses = expenseRepository.findAllByOwner(username);

        BigDecimal total = userExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = userExpenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory() : "Uncategorized",
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return new ExpenseSummaryResponse(total, byCategory);
    }

    public void delete(String username, Long id) {
        boolean removed = expenseRepository.deleteByIdAndOwner(id, username);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }
    }

    // Returns 404 (not 403) if the expense belongs to someone else - this
    // avoids confirming to an attacker that an ID exists at all.
    private Expense findOwnedOrThrow(String username, Long id) {
        return expenseRepository.findByIdAndOwner(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getDescription(),
                expense.getAmount(), expense.getCategory(), expense.getDate());
    }
}
