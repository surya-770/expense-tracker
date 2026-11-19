package com.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.BudgetDtos;
import com.expensetracker.dto.DashboardDtos;
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.dto.ReportDtos;
import com.expensetracker.entity.PaymentMethod;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.CsvUtil;

@Service
public class ReportService {
    private final DashboardService dashboard;private final BudgetService budgets;private final SavingsService savings;private final TransactionRepository transactions;private final UserRepository users;private final TransactionService transactionService;
    public ReportService(DashboardService dashboard,BudgetService budgets,SavingsService savings,TransactionRepository transactions,UserRepository users,TransactionService transactionService){this.dashboard=dashboard;this.budgets=budgets;this.savings=savings;this.transactions=transactions;this.users=users;this.transactionService=transactionService;}
    public DashboardDtos.Summary monthly(String email,int year,int month){return dashboard.summary(email,YearMonth.of(year,month));}
    public ReportDtos.YearlyReport yearly(String email,int year){
        YearMonth.of(year,1);List<DashboardDtos.MonthlyPoint> months=new ArrayList<>();BigDecimal income=BigDecimal.ZERO,expenses=BigDecimal.ZERO;
        for(int month=1;month<=12;month++){var summary=dashboard.summary(email,YearMonth.of(year,month));income=income.add(summary.income());expenses=expenses.add(summary.expenses());months.add(new DashboardDtos.MonthlyPoint(YearMonth.of(year,month).toString(),summary.income(),summary.expenses(),summary.monthlyNet()));}
        return new ReportDtos.YearlyReport(year,income,expenses,income.subtract(expenses),months);
    }
    @Transactional(readOnly=true) public List<DashboardDtos.CategoryPoint> categories(String email,LocalDate start,LocalDate end){validateRange(start,end);long userId=userId(email);return transactions.expenseDistribution(userId,start,end).stream().map(row->new DashboardDtos.CategoryPoint((String)row[0],(String)row[1],(BigDecimal)row[2])).toList();}
    public BudgetDtos.MonitoringView budget(String email,int year,int month){return budgets.monitoring(email,year,month);}
    public List<com.expensetracker.dto.SavingsDtos.GoalView> savings(String email){return savings.list(email);}
    @Transactional(readOnly=true)
    public ReportDtos.CustomReport custom(String email,LocalDate start,LocalDate end,TransactionType type,Long categoryId,PaymentMethod method){
        validateRange(start,end);long userId=userId(email);List<FinanceDtos.TransactionResponse> items=new ArrayList<>();int page=0;Page<Transaction> batch;BigDecimal income=BigDecimal.ZERO,expenses=BigDecimal.ZERO;
        do{batch=transactions.search(userId,type,categoryId,method,start,end,"",PageRequest.of(page++,500,Sort.by(Sort.Direction.DESC,"transactionDate")));for(Transaction t:batch.getContent()){var item=new FinanceDtos.TransactionResponse(t.getId(),t.getTransactionDate(),t.getDescription(),t.getTransactionType(),t.getCategory().getId(),t.getCategory().getName(),t.getSource(),t.getPaymentMethod(),t.getAmount());items.add(item);if(t.getTransactionType()==TransactionType.INCOME)income=income.add(t.getAmount());else expenses=expenses.add(t.getAmount());}}while(batch.hasNext());return new ReportDtos.CustomReport(start,end,income,expenses,income.subtract(expenses),items.size(),items);
    }
    public String export(String email,String type,int year,int month,LocalDate start,LocalDate end,TransactionType transactionType,Long categoryId,PaymentMethod method){
        return switch(type.toLowerCase()){
            case "transactions"->transactionService.exportCsv(email,transactionType,categoryId,method,start,end,"");
            case "income"->transactionService.exportCsv(email,TransactionType.INCOME,categoryId,null,start,end,"");
            case "expenses"->transactionService.exportCsv(email,TransactionType.EXPENSE,categoryId,method,start,end,"");
            case "monthly"->transactionService.exportCsv(email,null,null,null,YearMonth.of(year,month).atDay(1),YearMonth.of(year,month).atEndOfMonth(),"");
            case "yearly"->yearlyCsv(yearly(email,year));
            case "categories"->categoryCsv(categories(email,start,end));
            case "budget"->budgetCsv(budget(email,year,month));
            case "savings"->savingsCsv(savings(email));
            default->throw new IllegalArgumentException("Unsupported report type");
        };
    }
    private static String yearlyCsv(ReportDtos.YearlyReport report){StringBuilder out=new StringBuilder("\uFEFF").append(CsvUtil.row("month","income","expenses","net"));for(var m:report.months())out.append(CsvUtil.row(m.label(),m.income(),m.expenses(),m.net()));out.append(CsvUtil.row("TOTAL",report.income(),report.expenses(),report.netBalance()));return out.toString();}
    private static String categoryCsv(List<DashboardDtos.CategoryPoint> points){StringBuilder out=new StringBuilder("\uFEFF").append(CsvUtil.row("category","color","expenses"));for(var p:points)out.append(CsvUtil.row(p.category(),p.color(),p.amount()));return out.toString();}
    private static String budgetCsv(BudgetDtos.MonitoringView monitoring){StringBuilder out=new StringBuilder("\uFEFF").append(CsvUtil.row("category","planned","actual","remaining","utilization_percent","status"));if(!monitoring.planned())return out.append(CsvUtil.row("No budget planned")).toString();var plan=monitoring.plan();out.append(CsvUtil.row("OVERALL",plan.totalBudget(),plan.actual(),plan.remaining(),plan.utilization(),plan.status()));for(var a:plan.allocations())out.append(CsvUtil.row(a.category(),a.plannedAmount(),a.actual(),a.remaining(),a.utilization(),a.status()));return out.toString();}
    private static String savingsCsv(List<com.expensetracker.dto.SavingsDtos.GoalView> goals){StringBuilder out=new StringBuilder("\uFEFF").append(CsvUtil.row("goal","target","contributed","remaining","progress_percent","target_date","status"));for(var g:goals)out.append(CsvUtil.row(g.name(),g.targetAmount(),g.contributed(),g.remaining(),g.progress(),g.targetDate(),g.status()));return out.toString();}
    private void validateRange(LocalDate start,LocalDate end){if(start==null||end==null)throw new IllegalArgumentException("Start date and end date are required");if(start.isAfter(end))throw new IllegalArgumentException("Start date must be on or before end date");}
    private long userId(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found")).getId();}
}
