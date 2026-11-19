package com.expensetracker.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.expensetracker.entity.BudgetAllocation;

public interface BudgetAllocationRepository extends JpaRepository<BudgetAllocation,Long> {
    @Query("select a from BudgetAllocation a join fetch a.category where a.budgetPlan.id=:planId order by a.category.name")
    List<BudgetAllocation> findAllByPlanId(@Param("planId") Long planId);
    @Modifying @Query("delete from BudgetAllocation a where a.budgetPlan.id=:planId")
    void deleteAllByPlanId(@Param("planId") Long planId);
}
