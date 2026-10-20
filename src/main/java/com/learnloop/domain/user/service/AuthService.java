package com.learnloop.domain.user.service;

import com.learnloop.common.exceptions.BadRequestException;
import com.learnloop.common.exceptions.DuplicateResourceException;
import com.learnloop.common.exceptions.UnauthorizedException;
import com.learnloop.domain.user.dto.AuthResponse;
import com.learnloop.domain.user.dto.LoginRequest;
import com.learnloop.domain.user.dto.RegisterRequest;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.entity.UserStatus;
import com.learnloop.domain.user.repository.UserRepository;
import com.learnloop.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Enforce: Never allow public self-registration as ADMIN
        if (request.getRole() == UserRole.ADMIN) {
            log.warn("Attempt to publicly register as ADMIN rejected for email: {}", request.getEmail());
            throw new BadRequestException("Self-registration as administrator is not permitted");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getEmail().toLowerCase().trim(),
                encodedPassword,
                request.getFullName().trim(),
                request.getRole() != null ? request.getRole() : UserRole.STUDENT
        );

        user = userRepository.save(user);
        log.info("Registered new user with id: {}, role: {}", user.getId(), user.getRole());

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Account has been suspended. Please contact support.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UnauthorizedException("Account is currently inactive.");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
