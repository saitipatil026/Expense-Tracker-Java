package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);

    // The ownership check happens INSIDE the query itself: if the expense
    // exists but belongs to someone else, this returns empty — exactly the
    // same result as if the ID didn't exist at all. That's what stops a user
    // from finding another user's expense by guessing IDs.
    Optional<Expense> findByIdAndUser(Long id, User user);
}
