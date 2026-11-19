package com.expensetracker.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.expensetracker.entity.SavingsContribution;

public interface SavingsContributionRepository extends JpaRepository<SavingsContribution,Long> {
    @Query("select coalesce(sum(c.amount),0) from SavingsContribution c where c.savingsGoal.id=:goalId")
    BigDecimal totalForGoal(@Param("goalId") Long goalId);
    List<SavingsContribution> findAllBySavingsGoalIdOrderByContributionDateDescIdDesc(Long goalId);
    Optional<SavingsContribution> findByIdAndSavingsGoalId(Long id,Long goalId);
}
