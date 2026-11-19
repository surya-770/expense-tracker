package com.expensetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.BudgetDtos;
import com.expensetracker.entity.BudgetAllocation;
import com.expensetracker.entity.BudgetPlan;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.repository.BudgetAllocationRepository;
import com.expensetracker.repository.BudgetPlanRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;

@Service
public class BudgetService {
    private static final BigDecimal ZERO=BigDecimal.ZERO.setScale(2);
    private final BudgetPlanRepository plans;private final BudgetAllocationRepository allocations;private final CategoryRepository categories;
    private final TransactionRepository transactions;private final UserRepository users;
    public BudgetService(BudgetPlanRepository plans,BudgetAllocationRepository allocations,CategoryRepository categories,TransactionRepository transactions,UserRepository users){this.plans=plans;this.allocations=allocations;this.categories=categories;this.transactions=transactions;this.users=users;}

    @Transactional
    public BudgetDtos.PlanView create(String email,BudgetDtos.PlanRequest request){
        var user=account(email);if(plans.findByUserIdAndYearAndMonth(user.getId(),request.year(),request.month()).isPresent())throw new ConflictException("A budget already exists for this month");
        validateAllocations(user.getId(),request);BudgetPlan plan=plans.save(new BudgetPlan(user,request.month(),request.year(),request.totalBudget()));
        allocations.saveAll(buildAllocations(plan,user.getId(),request.allocations()));return view(plan,user.getId());
    }
    @Transactional
    public BudgetDtos.PlanView update(String email,long id,BudgetDtos.PlanRequest request){
        var user=account(email);BudgetPlan plan=plans.findByIdAndUserId(id,user.getId()).orElseThrow(()->new IllegalArgumentException("Budget not found"));
        var collision=plans.findByUserIdAndYearAndMonth(user.getId(),request.year(),request.month());if(collision.isPresent()&&!collision.get().getId().equals(id))throw new ConflictException("A budget already exists for this month");
        validateAllocations(user.getId(),request);
        // Changing the period is not supported because a budget's identity is its user/month pair.
        if(plan.getYear()!=request.year()||plan.getMonth()!=request.month())throw new IllegalArgumentException("Budget month and year cannot be changed; create a plan for the other month instead");
        plan.setTotalBudget(request.totalBudget());
        plans.save(plan);allocations.deleteAllByPlanId(id);allocations.saveAll(buildAllocations(plan,user.getId(),request.allocations()));return view(plan,user.getId());
    }
    @Transactional(readOnly=true)
    public List<BudgetDtos.PlanView> list(String email,Integer year){var user=account(email);List<BudgetPlan> rows=year==null?plans.findAllByUserIdOrderByYearDescMonthDesc(user.getId()):plans.findAllByUserIdAndYearOrderByMonthDesc(user.getId(),year);return rows.stream().map(p->view(p,user.getId())).toList();}
    @Transactional(readOnly=true)
    public BudgetDtos.PlanView get(String email,long id){var user=account(email);BudgetPlan plan=plans.findByIdAndUserId(id,user.getId()).orElseThrow(()->new IllegalArgumentException("Budget not found"));return view(plan,user.getId());}
    @Transactional(readOnly=true)
    public BudgetDtos.MonitoringView monitoring(String email,int year,int month){var user=account(email);var plan=plans.findByUserIdAndYearAndMonth(user.getId(),year,month);return new BudgetDtos.MonitoringView(month,year,plan.isPresent(),plan.map(p->view(p,user.getId())).orElse(null));}

    private void validateAllocations(Long userId,BudgetDtos.PlanRequest request){
        List<BudgetDtos.AllocationRequest> items=request.allocations()==null?List.of():request.allocations();HashSet<Long> seen=new HashSet<>();BigDecimal total=ZERO;
        for(var item:items){if(!seen.add(item.categoryId()))throw new IllegalArgumentException("A category can only be allocated once per month");Category category=categories.findAvailableForUser(userId).stream().filter(c->c.getId().equals(item.categoryId())).findFirst().orElseThrow(()->new IllegalArgumentException("Budget category not found"));if(!category.getType().equals(TransactionType.EXPENSE.name()))throw new IllegalArgumentException("Budget allocations must use expense categories");total=total.add(item.plannedAmount());}
        if(total.compareTo(request.totalBudget())>0)throw new IllegalArgumentException("Category allocations cannot exceed the overall budget");
    }
    private List<BudgetAllocation> buildAllocations(BudgetPlan plan,Long userId,List<BudgetDtos.AllocationRequest> items){if(items==null)return List.of();Map<Long,Category> allowed=new HashMap<>();categories.findAvailableForUser(userId).forEach(c->allowed.put(c.getId(),c));List<BudgetAllocation> rows=new ArrayList<>();for(var item:items)rows.add(new BudgetAllocation(plan,allowed.get(item.categoryId()),item.plannedAmount()));return rows;}
    private BudgetDtos.PlanView view(BudgetPlan plan,Long userId){
        LocalDate start=LocalDate.of(plan.getYear(),plan.getMonth(),1),end=start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal actual=transactions.sumByUserAndTypeForPeriod(userId,TransactionType.EXPENSE,start,end);Map<Long,BigDecimal> actuals=new HashMap<>();for(Object[] row:transactions.expenseTotalsByCategory(userId,start,end))actuals.put(((Number)row[0]).longValue(),(BigDecimal)row[1]);
        BigDecimal remaining=plan.getTotalBudget().subtract(actual);List<BudgetDtos.AllocationView> items=allocations.findAllByPlanId(plan.getId()).stream().map(a->{BigDecimal spent=actuals.getOrDefault(a.getCategory().getId(),ZERO);BigDecimal left=a.getPlannedAmount().subtract(spent);return new BudgetDtos.AllocationView(a.getCategory().getId(),a.getCategory().getName(),a.getPlannedAmount(),spent,left,percent(spent,a.getPlannedAmount()),status(percent(spent,a.getPlannedAmount())));}).toList();
        BigDecimal utilization=percent(actual,plan.getTotalBudget());return new BudgetDtos.PlanView(plan.getId(),plan.getMonth(),plan.getYear(),plan.getTotalBudget(),actual,remaining,utilization,status(utilization),items);
    }
    private User account(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found"));}
    private static BigDecimal percent(BigDecimal actual,BigDecimal planned){if(planned.signum()==0)return ZERO;return actual.multiply(BigDecimal.valueOf(100)).divide(planned,2,RoundingMode.HALF_UP);}
    private static String status(BigDecimal percent){if(percent.compareTo(BigDecimal.valueOf(100))>=0)return "OVER_BUDGET";if(percent.compareTo(BigDecimal.valueOf(80))>=0)return "WARNING";return "NORMAL";}
}
