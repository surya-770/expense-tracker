package com.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.expensetracker.dto.AuthDtos;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;

class AuthServiceTest {
    private UserRepository users;
    private BCryptPasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void setUp() { users = org.mockito.Mockito.mock(UserRepository.class); encoder = new BCryptPasswordEncoder(4); service = new AuthService(users, encoder); }

    @Test
    void registrationNormalizesEmailAndHashesPassword() {
        when(users.existsByEmail("sam@example.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        var response = service.register(new AuthDtos.RegisterRequest("Sam Person", " SAM@example.com ", "Str0ng!Passphrase", "Str0ng!Passphrase", "inr"));
        assertEquals("sam@example.com", response.email());
        assertEquals("INR", response.currency());
        var saved = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertNotEquals("Str0ng!Passphrase", saved.getValue().getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("Str0ng!Passphrase", saved.getValue().getPasswordHash()));
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        when(users.existsByEmail("sam@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.register(new AuthDtos.RegisterRequest("Sam", "sam@example.com", "Str0ng!Passphrase", "Str0ng!Passphrase", "USD")));
    }

    @Test
    void passwordChangeRequiresCurrentPasswordAndStoresHash() {
        User user = new User("Sam", "sam@example.com", encoder.encode("Old!Passphrase1"), "USD");
        when(users.findByEmail("sam@example.com")).thenReturn(Optional.of(user));
        service.changePassword("sam@example.com", new AuthDtos.PasswordUpdateRequest("Old!Passphrase1", "New!Passphrase2", "New!Passphrase2"));
        assertNotEquals("New!Passphrase2", user.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("New!Passphrase2", user.getPasswordHash()));
    }
}
