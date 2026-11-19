package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.expensetracker.entity.CompoundingFrequency;
import com.expensetracker.entity.DurationUnit;
import com.expensetracker.entity.InterestType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public final class InterestDtos {
    private InterestDtos(){}
    public record Request(@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal principal,
            @NotNull @DecimalMin("0") @DecimalMax("1000") @Digits(integer=4,fraction=4) BigDecimal annualRate,
            @NotNull @DecimalMin("0.01") @Digits(integer=4,fraction=2) BigDecimal duration,
            @NotNull DurationUnit durationUnit,CompoundingFrequency compoundingFrequency){}
    public record SaveRequest(@NotNull InterestType type,@NotNull @jakarta.validation.Valid Request calculation){}
    public record GrowthPoint(BigDecimal elapsedYears,BigDecimal amount){}
    public record Result(Long id,InterestType type,BigDecimal principal,BigDecimal annualRate,BigDecimal duration,
            DurationUnit durationUnit,CompoundingFrequency compoundingFrequency,BigDecimal interestAmount,
            BigDecimal finalAmount,List<GrowthPoint> growth,Instant createdAt){}
}
