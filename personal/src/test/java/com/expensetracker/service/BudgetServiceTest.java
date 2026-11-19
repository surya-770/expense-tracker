package com.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.expensetracker.dto.BudgetDtos;
import com.expensetracker.entity.BudgetPlan;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.repository.BudgetAllocationRepository;
import com.expensetracker.repository.BudgetPlanRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;

class BudgetServiceTest {
    private BudgetPlanRepository plans;private BudgetAllocationRepository allocations;private CategoryRepository categories;private TransactionRepository transactions;private UserRepository users;private BudgetService service;
    @BeforeEach void setUp(){
        plans=org.mockito.Mockito.mock(BudgetPlanRepository.class);allocations=org.mockito.Mockito.mock(BudgetAllocationRepository.class);categories=org.mockito.Mockito.mock(CategoryRepository.class);transactions=org.mockito.Mockito.mock(TransactionRepository.class);users=org.mockito.Mockito.mock(UserRepository.class);service=new BudgetService(plans,allocations,categories,transactions,users);
        User user=new User("Test User","test@example.com","hash","USD");ReflectionTestUtils.setField(user,"id",7L);when(users.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(plans.findByUserIdAndYearAndMonth(7L,2026,11)).thenReturn(Optional.empty());when(categories.findAvailableForUser(7L)).thenReturn(List.of());
        when(plans.save(any(BudgetPlan.class))).thenAnswer(invocation->{BudgetPlan plan=invocation.getArgument(0);ReflectionTestUtils.setField(plan,"id",12L);return plan;});
        when(allocations.findAllByPlanId(12L)).thenReturn(List.of());when(transactions.expenseTotalsByCategory(any(),any(),any())).thenReturn(List.of());
    }
    @Test void rejectsAllocationsAboveOverallBudgetBeforeSaving(){
        var request=new BudgetDtos.PlanRequest(11,2026,new BigDecimal("100.00"),List.of(new BudgetDtos.AllocationRequest(1L,new BigDecimal("60.00")),new BudgetDtos.AllocationRequest(2L,new BigDecimal("50.00"))));
        assertThrows(IllegalArgumentException.class,()->service.create("test@example.com",request));verify(plans,never()).save(any());
    }
    @Test void preventsDuplicateMonthlyPlan(){
        when(plans.findByUserIdAndYearAndMonth(7L,2026,11)).thenReturn(Optional.of(new BudgetPlan(new User("Test User","x","hash","USD"),11,2026,new BigDecimal("100.00"))));
        assertThrows(ConflictException.class,()->service.create("test@example.com",new BudgetDtos.PlanRequest(11,2026,new BigDecimal("100.00"),List.of())));
    }
    @Test void labelsEightyPercentAsWarningAndOverBudgetAtOneHundred(){
        when(transactions.sumByUserAndTypeForPeriod(7L,com.expensetracker.entity.TransactionType.EXPENSE,java.time.LocalDate.of(2026,11,1),java.time.LocalDate.of(2026,11,30))).thenReturn(new BigDecimal("80.00"));
        var warning=service.create("test@example.com",new BudgetDtos.PlanRequest(11,2026,new BigDecimal("100.00"),List.of()));assertEquals("WARNING",warning.status());assertEquals(new BigDecimal("20.00"),warning.remaining());
        when(plans.findByUserIdAndYearAndMonth(7L,2026,11)).thenReturn(Optional.empty());when(transactions.sumByUserAndTypeForPeriod(7L,com.expensetracker.entity.TransactionType.EXPENSE,java.time.LocalDate.of(2026,11,1),java.time.LocalDate.of(2026,11,30))).thenReturn(new BigDecimal("100.00"));
        var over=service.create("test@example.com",new BudgetDtos.PlanRequest(11,2026,new BigDecimal("100.00"),List.of()));assertEquals("OVER_BUDGET",over.status());
    }
}
