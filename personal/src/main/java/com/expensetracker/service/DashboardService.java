package com.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.DashboardDtos;
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.entity.GoalStatus;
import com.expensetracker.repository.SavingsGoalRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;

@Service
public class DashboardService {
    private final TransactionRepository transactions;private final UserRepository users;
    private final BudgetService budgets;
    private final SavingsGoalRepository goals;
    private static final DateTimeFormatter LABEL=DateTimeFormatter.ofPattern("MMM yy");
    public DashboardService(TransactionRepository transactions,UserRepository users,BudgetService budgets,SavingsGoalRepository goals){this.transactions=transactions;this.users=users;this.budgets=budgets;this.goals=goals;}

    @Transactional(readOnly=true)
    public DashboardDtos.Summary summary(String email,YearMonth period){
        var user=account(email);LocalDate start=period.atDay(1),end=period.atEndOfMonth();
        BigDecimal income=transactions.sumByUserAndTypeForPeriod(user.getId(),TransactionType.INCOME,start,end);
        BigDecimal expenses=transactions.sumByUserAndTypeForPeriod(user.getId(),TransactionType.EXPENSE,start,end);
        BigDecimal allIncome=transactions.sumByUserAndType(user.getId(),TransactionType.INCOME);
        BigDecimal allExpenses=transactions.sumByUserAndType(user.getId(),TransactionType.EXPENSE);
        var monitoring=budgets.monitoring(email,period.getYear(),period.getMonthValue());BigDecimal budgetRemaining=monitoring.planned()?monitoring.plan().remaining():null;
        long activeGoals=goals.countByUserIdAndStatus(user.getId(),GoalStatus.ACTIVE);
        return new DashboardDtos.Summary(period,user.getCurrency(),income,expenses,income.subtract(expenses),allIncome,allExpenses,allIncome.subtract(allExpenses),budgetRemaining,activeGoals);
    }

    @Transactional(readOnly=true)
    public DashboardDtos.Charts charts(String email,YearMonth period){
        long userId=account(email).getId();YearMonth first=period.minusMonths(5);Map<YearMonth,BigDecimal[]> values=new HashMap<>();
        for(int i=0;i<6;i++)values.put(first.plusMonths(i),new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO});
        for(Object[] row:transactions.monthlyTotals(userId,first.atDay(1))){int year=((Number)row[0]).intValue(),month=((Number)row[1]).intValue();YearMonth key=YearMonth.of(year,month);BigDecimal[] pair=values.get(key);if(pair!=null){if("INCOME".equals(row[2]))pair[0]=(BigDecimal)row[3];else pair[1]=(BigDecimal)row[3];}}
        List<DashboardDtos.MonthlyPoint> monthly=new ArrayList<>();
        for(int i=0;i<6;i++){YearMonth key=first.plusMonths(i);BigDecimal[] pair=values.get(key);monthly.add(new DashboardDtos.MonthlyPoint(key.format(LABEL),pair[0],pair[1],pair[0].subtract(pair[1])));}
        List<DashboardDtos.CategoryPoint> distribution=transactions.expenseDistribution(userId,period.atDay(1),period.atEndOfMonth()).stream()
                .map(row->new DashboardDtos.CategoryPoint((String)row[0],(String)row[1],(BigDecimal)row[2])).toList();
        return new DashboardDtos.Charts(monthly,distribution);
    }

    @Transactional(readOnly=true)
    public List<FinanceDtos.TransactionResponse> recent(String email,int limit){
        if(limit<1||limit>20)throw new IllegalArgumentException("Limit must be between 1 and 20");
        long userId=account(email).getId();return transactions.search(userId,null,null,null,null,null,"",PageRequest.of(0,limit,Sort.by(Sort.Direction.DESC,"transactionDate").and(Sort.by(Sort.Direction.DESC,"id"))))
                .map(t->new FinanceDtos.TransactionResponse(t.getId(),t.getTransactionDate(),t.getDescription(),t.getTransactionType(),t.getCategory().getId(),t.getCategory().getName(),t.getSource(),t.getPaymentMethod(),t.getAmount())).getContent();
    }
    private com.expensetracker.entity.User account(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found"));}
}
