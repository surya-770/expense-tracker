package com.expensetracker.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.expensetracker.entity.BudgetPlan;

public interface BudgetPlanRepository extends JpaRepository<BudgetPlan,Long> {
    Optional<BudgetPlan> findByUserIdAndYearAndMonth(Long userId,int year,int month);
    Optional<BudgetPlan> findByIdAndUserId(Long id,Long userId);
    List<BudgetPlan> findAllByUserIdAndYearOrderByMonthDesc(Long userId,int year);
    List<BudgetPlan> findAllByUserIdOrderByYearDescMonthDesc(Long userId);
}
