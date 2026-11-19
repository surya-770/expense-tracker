package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.expensetracker.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SavingsDtos {
    private SavingsDtos(){}
    public record GoalRequest(@NotBlank @Size(max=120) String name,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal targetAmount,LocalDate targetDate,@Size(max=500) String description){}
    public record StatusRequest(@NotNull GoalStatus status){}
    public record ContributionRequest(@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,@NotNull LocalDate date,@Size(max=500) String description){}
    public record ContributionView(Long id,BigDecimal amount,LocalDate date,String description){}
    public record GoalView(Long id,String name,BigDecimal targetAmount,BigDecimal contributed,BigDecimal remaining,BigDecimal progress,LocalDate targetDate,String description,GoalStatus status,List<ContributionView> contributions){}
}
