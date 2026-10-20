package com.learnloop.domain.user.service;

import com.learnloop.common.exceptions.BadRequestException;
import com.learnloop.common.exceptions.DuplicateResourceException;
import com.learnloop.common.exceptions.UnauthorizedException;
import com.learnloop.domain.user.dto.AuthResponse;
import com.learnloop.domain.user.dto.LoginRequest;
import com.learnloop.domain.user.dto.RegisterRequest;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.repository.UserRepository;
import com.learnloop.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest studentRegisterRequest;
    private User studentUser;

    @BeforeEach
    void setUp() {
        studentRegisterRequest = new RegisterRequest("student@example.com", "Password@123", "Alice Student", UserRole.STUDENT);
        studentUser = new User("student@example.com", "encodedPassword", "Alice Student", UserRole.STUDENT);
    }

    @Test
    @DisplayName("Successfully registers student and returns JWT AuthResponse")
    void registerStudent_Success() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(studentUser);
        when(tokenProvider.generateToken(any(), any(), any())).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(studentRegisterRequest);

        assertNotNull(response);
        assertEquals("student@example.com", response.getEmail());
        assertEquals("mock.jwt.token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Rejects public self-registration as ADMIN")
    void registerAdmin_ThrowsBadRequest() {
        RegisterRequest adminRequest = new RegisterRequest("admin@fake.com", "AdminPass123", "Fake Admin", UserRole.ADMIN);

        assertThrows(BadRequestException.class, () -> authService.register(adminRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Rejects registration when email already exists")
    void registerDuplicateEmail_ThrowsDuplicateResource() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(studentRegisterRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Successfully logs in with valid credentials")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("student@example.com", "Password@123");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("Password@123", studentUser.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(studentUser.getId(), studentUser.getEmail(), studentUser.getRole()))
                .thenReturn("valid.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getToken());
    }

    @Test
    @DisplayName("Rejects login with invalid password")
    void login_InvalidPassword_ThrowsUnauthorized() {
        LoginRequest loginRequest = new LoginRequest("student@example.com", "WrongPassword");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("WrongPassword", studentUser.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
    }
}
