package com.expensetracker.entity;

import java.math.BigDecimal;
import java.time.Instant;
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

@Entity @Table(name="interest_calculations")
public class InterestCalculation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Enumerated(EnumType.STRING) @Column(name="calculation_type",nullable=false,length=10) private InterestType calculationType;
    @Column(nullable=false,precision=19,scale=2) private BigDecimal principal;
    @Column(name="annual_rate",nullable=false,precision=9,scale=4) private BigDecimal annualRate;
    @Column(nullable=false,precision=9,scale=4) private BigDecimal duration;
    @Enumerated(EnumType.STRING) @Column(name="duration_unit",nullable=false,length=10) private DurationUnit durationUnit;
    @Enumerated(EnumType.STRING) @Column(name="compounding_frequency",length=15) private CompoundingFrequency compoundingFrequency;
    @Column(name="interest_amount",nullable=false,precision=19,scale=2) private BigDecimal interestAmount;
    @Column(name="final_amount",nullable=false,precision=19,scale=2) private BigDecimal finalAmount;
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected InterestCalculation(){}
    public InterestCalculation(User user,InterestType type,BigDecimal principal,BigDecimal rate,BigDecimal duration,DurationUnit unit,CompoundingFrequency frequency,BigDecimal interest,BigDecimal finalAmount){this.user=user;this.calculationType=type;this.principal=principal;this.annualRate=rate;this.duration=duration;this.durationUnit=unit;this.compoundingFrequency=frequency;this.interestAmount=interest;this.finalAmount=finalAmount;}
    public Long getId(){return id;}public InterestType getCalculationType(){return calculationType;}public BigDecimal getPrincipal(){return principal;}public BigDecimal getAnnualRate(){return annualRate;}public BigDecimal getDuration(){return duration;}public DurationUnit getDurationUnit(){return durationUnit;}public CompoundingFrequency getCompoundingFrequency(){return compoundingFrequency;}public BigDecimal getInterestAmount(){return interestAmount;}public BigDecimal getFinalAmount(){return finalAmount;}public Instant getCreatedAt(){return createdAt;}
}
