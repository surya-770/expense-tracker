package com.expensetracker.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity @Table(name="savings_goals")
public class SavingsGoal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false,length=120) private String name;
    @Column(name="target_amount",nullable=false,precision=19,scale=2) private BigDecimal targetAmount;
    @Column(name="target_date") private LocalDate targetDate;
    @Column(length=500) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private GoalStatus status=GoalStatus.ACTIVE;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected SavingsGoal(){}
    public SavingsGoal(User user,String name,BigDecimal targetAmount,LocalDate targetDate,String description){this.user=user;this.name=name;this.targetAmount=targetAmount;this.targetDate=targetDate;this.description=description;}
    public Long getId(){return id;}public User getUser(){return user;}public String getName(){return name;}public BigDecimal getTargetAmount(){return targetAmount;}public LocalDate getTargetDate(){return targetDate;}public String getDescription(){return description;}public GoalStatus getStatus(){return status;}
    public void update(String name,BigDecimal targetAmount,LocalDate targetDate,String description){this.name=name;this.targetAmount=targetAmount;this.targetDate=targetDate;this.description=description;}public void setStatus(GoalStatus status){this.status=status;}
}
