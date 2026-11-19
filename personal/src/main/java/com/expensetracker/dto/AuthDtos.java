package com.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{10,72}$", message = "Use 10-72 characters with uppercase, lowercase, a number, and a symbol") String password,
            @NotBlank String confirmPassword,
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a three-letter code") String currency) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record ProfileUpdateRequest(@NotBlank @Size(max = 120) String fullName,
            @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a three-letter code") String currency) {}
    public record PasswordUpdateRequest(@NotBlank String currentPassword,
            @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{10,72}$", message = "Use 10-72 characters with uppercase, lowercase, a number, and a symbol") String newPassword,
            @NotBlank String confirmPassword) {}
    public record UserResponse(Long id, String fullName, String email, String currency) {}
    public record MessageResponse(String message) {}
}
