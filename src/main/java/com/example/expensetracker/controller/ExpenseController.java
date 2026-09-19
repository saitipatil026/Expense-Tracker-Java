package com.example.expensetracker.controller;

import com.example.expensetracker.dto.CreateExpenseRequest;
import com.example.expensetracker.dto.ExpenseResponse;
import com.example.expensetracker.dto.ExpenseSummaryResponse;
import com.example.expensetracker.dto.UpdateExpenseRequest;
import com.example.expensetracker.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every method here pulls the caller's username from the Authentication
 * object that Spring Security populates from the validated JWT — never
 * from anything the client sends in the request body. That's what makes
 * the ownership checks in ExpenseService trustworthy.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@RequestBody CreateExpenseRequest request,
                                                   Authentication authentication) {
        ExpenseResponse response = expenseService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ExpenseResponse> getAll(Authentication authentication) {
        return expenseService.getAll(authentication.getName());
    }

    // Declared before "/{id}" on purpose, and matches independently of order
    // in modern Spring MVC — but keeping the literal path first here avoids
    // any doubt about "/summary" being swallowed by the {id} pattern.
    @GetMapping("/summary")
    public ExpenseSummaryResponse getSummary(Authentication authentication) {
        return expenseService.summary(authentication.getName());
    }

    @GetMapping("/{id}")
    public ExpenseResponse getById(@PathVariable Long id, Authentication authentication) {
        return expenseService.getById(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id,
                                   @RequestBody UpdateExpenseRequest request,
                                   Authentication authentication) {
        return expenseService.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        expenseService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
