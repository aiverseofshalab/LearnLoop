package com.learnloop.domain.user.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.user.dto.AuthResponse;
import com.learnloop.domain.user.dto.LoginRequest;
import com.learnloop.domain.user.dto.RegisterRequest;
import com.learnloop.domain.user.dto.UserProfileResponse;
import com.learnloop.domain.user.service.AuthService;
import com.learnloop.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, session inspection, and logout")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final String cookieName;
    private final long expirationMs;

    public AuthController(AuthService authService,
                          UserService userService,
                          @Value("${learnloop.jwt.cookie-name:learnloop_token}") String cookieName,
                          @Value("${learnloop.jwt.expiration-ms:86400000}") long expirationMs) {
        this.authService = authService;
        this.userService = userService;
        this.cookieName = cookieName;
        this.expirationMs = expirationMs;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new student or teacher account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                              HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        attachAuthCookie(response, authResponse.getToken(), (int) (expirationMs / 1000));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        attachAuthCookie(response, authResponse.getToken(), (int) (expirationMs / 1000));
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate session cookie")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        attachAuthCookie(response, "", 0);
        return ResponseEntity.ok(ApiResponse.successMessage("Logout successful"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's profile and active session")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    private void attachAuthCookie(HttpServletResponse response, String token, int maxAge) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        // In modern browsers, SameSite=Lax protects against CSRF while allowing top-level navigation
        response.addHeader("Set-Cookie", String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                cookieName, token, maxAge));
    }
}
