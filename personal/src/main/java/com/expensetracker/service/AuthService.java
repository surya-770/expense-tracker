package com.expensetracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.AuthDtos;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    public AuthService(UserRepository users, PasswordEncoder passwords) {
        this.users = users; this.passwords = passwords;
    }

    @Transactional
    public AuthDtos.UserResponse register(AuthDtos.RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) throw new IllegalArgumentException("Passwords do not match");
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new IllegalArgumentException("An account with this email already exists");
        User user = users.save(new User(request.fullName().trim(), email, passwords.encode(request.password()), normalizeCurrency(request.currency())));
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse profile(String email) { return response(findUser(email)); }

    @Transactional
    public AuthDtos.UserResponse updateProfile(String email, AuthDtos.ProfileUpdateRequest request) {
        User user = findUser(email); user.setFullName(request.fullName().trim()); user.setCurrency(normalizeCurrency(request.currency()));
        return response(users.save(user));
    }

    @Transactional
    public void changePassword(String email, AuthDtos.PasswordUpdateRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) throw new IllegalArgumentException("Passwords do not match");
        User user = findUser(email);
        if (!passwords.matches(request.currentPassword(), user.getPasswordHash())) throw new IllegalArgumentException("Current password is incorrect");
        if (passwords.matches(request.newPassword(), user.getPasswordHash())) throw new IllegalArgumentException("Choose a password different from your current password");
        user.setPasswordHash(passwords.encode(request.newPassword()));
        users.save(user);
    }

    private User findUser(String email) { return users.findByEmail(email.trim().toLowerCase()).orElseThrow(() -> new IllegalStateException("Authenticated account was not found")); }
    private static String normalizeCurrency(String currency) { return currency == null || currency.isBlank() ? "USD" : currency.toUpperCase(); }
    private static AuthDtos.UserResponse response(User user) { return new AuthDtos.UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getCurrency()); }
}
