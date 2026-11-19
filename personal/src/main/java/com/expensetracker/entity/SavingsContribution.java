package com.expensetracker.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity @Table(name="savings_contributions")
public class SavingsContribution {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="savings_goal_id",nullable=false) private SavingsGoal savingsGoal;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
    @Column(name="contribution_date",nullable=false) private LocalDate contributionDate;
    @Column(length=500) private String description;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected SavingsContribution(){}
    public SavingsContribution(SavingsGoal goal,BigDecimal amount,LocalDate date,String description){this.savingsGoal=goal;this.amount=amount;this.contributionDate=date;this.description=description;}
    public Long getId(){return id;}public SavingsGoal getSavingsGoal(){return savingsGoal;}public BigDecimal getAmount(){return amount;}public LocalDate getContributionDate(){return contributionDate;}public String getDescription(){return description;}
    public void update(BigDecimal amount,LocalDate date,String description){this.amount=amount;this.contributionDate=date;this.description=description;}
}
