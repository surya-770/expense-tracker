package com.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.expensetracker.entity.PaymentMethod;
import com.expensetracker.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class FinanceDtos {
    private FinanceDtos() {}
    public record CategoryRequest(@NotNull TransactionType type, @jakarta.validation.constraints.NotBlank @Size(max=80) String name,
            @Size(max=40) String icon, @jakarta.validation.constraints.Pattern(regexp="^#[0-9A-Fa-f]{6}$") String color) {}
    public record CategoryResponse(Long id,String name,TransactionType type,String icon,String color,boolean shared) {}
    public record TransactionRequest(@NotNull TransactionType type,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,
            @NotNull LocalDate date,@NotNull Long categoryId,@Size(max=100) String source,PaymentMethod paymentMethod,
            @Size(max=500) String description) {}
    public record TransactionResponse(Long id,LocalDate date,String description,TransactionType type,Long categoryId,
            String category,String source,PaymentMethod paymentMethod,BigDecimal amount) {}
}
