package com.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class BudgetDtos {
    private BudgetDtos(){}
    public record AllocationRequest(@NotNull Long categoryId,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal plannedAmount){}
    public record PlanRequest(@Min(1) @Max(12) int month,@Min(2000) @Max(2200) int year,
            @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal totalBudget,
            List<@Valid AllocationRequest> allocations){}
    public record AllocationView(Long categoryId,String category,BigDecimal plannedAmount,BigDecimal actual,
            BigDecimal remaining,BigDecimal utilization,String status){}
    public record PlanView(Long id,int month,int year,BigDecimal totalBudget,BigDecimal actual,BigDecimal remaining,
            BigDecimal utilization,String status,List<AllocationView> allocations){}
    public record MonitoringView(int month,int year,boolean planned,PlanView plan){}
}
