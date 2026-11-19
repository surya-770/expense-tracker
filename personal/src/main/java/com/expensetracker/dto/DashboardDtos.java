package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {}
    public record Summary(YearMonth period,String currency,BigDecimal income,BigDecimal expenses,BigDecimal monthlyNet,
            BigDecimal allTimeIncome,BigDecimal allTimeExpenses,BigDecimal netBalance,BigDecimal budgetRemaining,long activeSavingsGoals) {}
    public record MonthlyPoint(String label,BigDecimal income,BigDecimal expenses,BigDecimal net) {}
    public record CategoryPoint(String category,String color,BigDecimal amount) {}
    public record Charts(List<MonthlyPoint> monthly,List<CategoryPoint> expenseDistribution) {}
}
