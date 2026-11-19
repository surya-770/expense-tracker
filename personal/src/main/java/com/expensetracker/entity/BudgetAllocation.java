package com.expensetracker.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity @Table(name="budget_allocations")
public class BudgetAllocation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="budget_plan_id",nullable=false) private BudgetPlan budgetPlan;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id",nullable=false) private Category category;
    @Column(name="planned_amount",nullable=false,precision=19,scale=2) private BigDecimal plannedAmount;
    protected BudgetAllocation(){}
    public BudgetAllocation(BudgetPlan budgetPlan,Category category,BigDecimal plannedAmount){this.budgetPlan=budgetPlan;this.category=category;this.plannedAmount=plannedAmount;}
    public Long getId(){return id;} public BudgetPlan getBudgetPlan(){return budgetPlan;} public Category getCategory(){return category;} public BigDecimal getPlannedAmount(){return plannedAmount;}
}
