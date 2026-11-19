package com.expensetracker.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

@Entity @Table(name="transactions")
public class Transaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="category_id",nullable=false) private Category category;
    @Enumerated(EnumType.STRING) @Column(name="transaction_type",nullable=false,length=8) private TransactionType transactionType;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
    @Column(name="transaction_date",nullable=false) private LocalDate transactionDate;
    @Column(length=100) private String source;
    @Enumerated(EnumType.STRING) @Column(name="payment_method",length=20) private PaymentMethod paymentMethod;
    @Column(length=500) private String description;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @UpdateTimestamp @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected Transaction() {}
    public Transaction(User user,Category category,TransactionType type,BigDecimal amount,LocalDate date,String source,PaymentMethod method,String description){this.user=user;this.category=category;this.transactionType=type;this.amount=amount;this.transactionDate=date;this.source=source;this.paymentMethod=method;this.description=description;}
    public Long getId(){return id;} public User getUser(){return user;} public Category getCategory(){return category;} public TransactionType getTransactionType(){return transactionType;} public BigDecimal getAmount(){return amount;} public LocalDate getTransactionDate(){return transactionDate;} public String getSource(){return source;} public PaymentMethod getPaymentMethod(){return paymentMethod;} public String getDescription(){return description;}
    public void update(Category category,TransactionType type,BigDecimal amount,LocalDate date,String source,PaymentMethod method,String description){this.category=category;this.transactionType=type;this.amount=amount;this.transactionDate=date;this.source=source;this.paymentMethod=method;this.description=description;}
}
