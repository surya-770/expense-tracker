package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ReportDtos {
    private ReportDtos(){}
    public record YearlyReport(int year,BigDecimal income,BigDecimal expenses,BigDecimal netBalance,List<DashboardDtos.MonthlyPoint> months){}
    public record CustomReport(LocalDate startDate,LocalDate endDate,BigDecimal income,BigDecimal expenses,BigDecimal net,
            long transactionCount,List<FinanceDtos.TransactionResponse> transactions){}
}
