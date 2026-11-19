package com.expensetracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.expensetracker.dto.AuthDtos;
import com.expensetracker.service.AuthService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final AuthService authService;
    public ProfileController(AuthService authService) { this.authService = authService; }
    @GetMapping public AuthDtos.UserResponse profile(Authentication authentication) { return authService.profile(authentication.getName()); }
    @PutMapping public AuthDtos.UserResponse update(@Valid @RequestBody AuthDtos.ProfileUpdateRequest request, Authentication authentication) {
        return authService.updateProfile(authentication.getName(), request);
    }
    @PutMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody AuthDtos.PasswordUpdateRequest request, Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
    }
}
