package com.example.expensetracker.service;

import com.example.expensetracker.dto.CreateExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummaryResponse;
import com.example.expensetracker.dto.UpdateExpenseRequest;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public ExpenseResponse create(CreateExpenseRequest request, String username) {
        User user = resolveUser(username);
        Expense expense = new Expense(
                request.getDescription(),
                request.getAmount(),
                request.getCategory(),
                request.getDate(),
                user
        );
        return toResponse(expenseRepository.save(expense));
    }

    public List<ExpenseResponse> getAll(String username) {
        User user = resolveUser(username);
        return expenseRepository.findByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getById(Long id, String username) {
        return toResponse(findOwnedExpense(id, username));
    }

    public ExpenseResponse update(Long id, UpdateExpenseRequest request, String username) {
        Expense expense = findOwnedExpense(id, username);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        return toResponse(expenseRepository.save(expense));
    }

    public void delete(Long id, String username) {
        expenseRepository.delete(findOwnedExpense(id, username));
    }

    public ExpenseSummaryResponse summary(String username) {
        User user = resolveUser(username);
        List<Expense> expenses = expenseRepository.findByUser(user);

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return new ExpenseSummaryResponse(total, categoryTotals);
    }

    /**
     * Looks up the expense by id AND owner in a single query. If the expense
     * belongs to someone else, this returns the exact same 404 as if the ID
     * didn't exist — the endpoint never reveals that another user's expense
     * exists, which is what actually prevents ID-guessing attacks.
     */
    private Expense findOwnedExpense(Long id, String username) {
        User user = resolveUser(username);
        return expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );
    }
}
