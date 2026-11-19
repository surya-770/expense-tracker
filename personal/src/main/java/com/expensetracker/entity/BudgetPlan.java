package com.expensetracker.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity @Table(name="budget_plans")
public class BudgetPlan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(name="budget_month",nullable=false) private int month;
    @Column(name="budget_year",nullable=false) private int year;
    @Column(name="total_budget",nullable=false,precision=19,scale=2) private BigDecimal totalBudget;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected BudgetPlan() {}
    public BudgetPlan(User user,int month,int year,BigDecimal totalBudget){this.user=user;this.month=month;this.year=year;this.totalBudget=totalBudget;}
    public Long getId(){return id;} public User getUser(){return user;} public int getMonth(){return month;} public int getYear(){return year;} public BigDecimal getTotalBudget(){return totalBudget;}
    public void setTotalBudget(BigDecimal totalBudget){this.totalBudget=totalBudget;}
}
