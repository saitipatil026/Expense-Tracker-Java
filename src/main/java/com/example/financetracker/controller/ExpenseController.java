package com.example.financetracker.controller;

import com.example.financetracker.dto.CreateExpenseRequest;
import com.example.financetracker.dto.ExpenseResponse;
import com.example.financetracker.dto.ExpenseSummaryResponse;
import com.example.financetracker.dto.UpdateExpenseRequest;
import com.example.financetracker.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * No explicit auth check needed here - SecurityConfig already requires a
 * valid JWT for anything outside /auth/**, so every method below only
 * runs once JwtAuthenticationFilter has confirmed the token and populated
 * Authentication. That object's name is the username, which scopes every
 * operation to "this user's own expenses" via ExpenseService.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(Authentication authentication,
                                                   @RequestBody CreateExpenseRequest request) {
        ExpenseResponse response = expenseService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ExpenseResponse> list(Authentication authentication) {
        return expenseService.listForUser(authentication.getName());
    }

    // Mapped as a literal "/summary" segment, not "/{id}" - Spring matches
    // the exact literal path ahead of the {id} variable pattern, so this
    // and getOne() below never collide even though both are GET under
    // the same base path.
    @GetMapping("/summary")
    public ExpenseSummaryResponse summary(Authentication authentication) {
        return expenseService.getSummary(authentication.getName());
    }

    @GetMapping("/{id}")
    public ExpenseResponse getOne(Authentication authentication, @PathVariable Long id) {
        return expenseService.getOne(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(Authentication authentication, @PathVariable Long id,
                                   @RequestBody UpdateExpenseRequest request) {
        return expenseService.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        expenseService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
