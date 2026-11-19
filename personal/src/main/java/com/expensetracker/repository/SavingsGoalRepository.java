package com.expensetracker.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.expensetracker.entity.SavingsGoal;
import com.expensetracker.entity.GoalStatus;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal,Long> {
    List<SavingsGoal> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<SavingsGoal> findByIdAndUserId(Long id,Long userId);
    long countByUserIdAndStatus(Long userId,GoalStatus status);
}
