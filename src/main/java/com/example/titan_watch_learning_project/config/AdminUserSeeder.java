package com.example.titan_watch_learning_project.config;

import com.example.titan_watch_learning_project.UserRole;
import com.example.titan_watch_learning_project.entity.AdminUser;
//import com.example.titan_watch_learning_project.entity.UserRole;
import com.example.titan_watch_learning_project.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String defaultEmail;

    @Value("${admin.default.password}")
    private String defaultPassword;

    @Value("${admin.default.name}")
    private String defaultName;

    @Override
    public void run(String... args) {
        String email = defaultEmail.trim().toLowerCase();

        if (adminUserRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        AdminUser admin = new AdminUser();
        admin.setFullName(defaultName);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);

        adminUserRepository.save(admin);

        log.info("Default ADMIN user created: {}", email);
    }
}