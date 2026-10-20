package com.learnloop.domain.user.service;

import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${learnloop.admin.bootstrap-enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${learnloop.admin.email:admin@learnloop.com}")
    private String adminEmail;

    @Value("${learnloop.admin.password:Admin@LearnLoop2026!}")
    private String adminPassword;

    @Value("${learnloop.admin.name:LearnLoop Administrator}")
    private String adminName;

    public AdminBootstrapService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapAdmin() {
        if (!bootstrapEnabled) {
            log.info("Admin bootstrap is disabled via configuration");
            return;
        }

        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        if (adminCount == 0) {
            log.info("No administrator found in database. Initializing bootstrap admin user: {}", adminEmail);

            if (userRepository.existsByEmail(adminEmail)) {
                User existing = userRepository.findByEmail(adminEmail).get();
                existing.setRole(UserRole.ADMIN);
                userRepository.save(existing);
                log.info("Promoted existing user {} to ADMIN role", adminEmail);
            } else {
                User admin = new User(
                        adminEmail.toLowerCase().trim(),
                        passwordEncoder.encode(adminPassword),
                        adminName,
                        UserRole.ADMIN
                );
                userRepository.save(admin);
                log.info("Bootstrap admin successfully created with role ADMIN");
            }
        } else {
            log.info("Administrator accounts already exist (count: {}). Bootstrap skipped.", adminCount);
        }
    }
}
