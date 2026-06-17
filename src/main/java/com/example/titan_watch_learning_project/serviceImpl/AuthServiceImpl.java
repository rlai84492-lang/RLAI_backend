package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.UserRole;
import com.example.titan_watch_learning_project.dto.AuthDtos;
import com.example.titan_watch_learning_project.entity.AdminUser;
import com.example.titan_watch_learning_project.repository.AdminUserRepository;
import com.example.titan_watch_learning_project.security.JwtUtil;
import com.example.titan_watch_learning_project.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtil             jwtUtil;

    // ── Login ──────────────────────────────────────────────────────────────────
    // Validates email + password → returns JWT token
    @Override
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {

        // Find user by email (case-insensitive)
        AdminUser user = adminUserRepository
                .findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check account is active
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException("Account is inactive");
        }

        // Verify BCrypt password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user);

        log.info("Login successful: email={} role={}", user.getEmail(), user.getRole());

        AuthDtos.LoginResponse response = new AuthDtos.LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }

    // ── Signup ─────────────────────────────────────────────────────────────────
    // Creates a new admin user.
    // If no users exist in DB → role = ADMIN (first user is always ADMIN)
    // If users already exist → role = STORE_MANAGER (needs ADMIN approval to upgrade)
    @Override
    public AuthDtos.SignupResponse signup(AuthDtos.SignupRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // Check duplicate
        if (adminUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        // First user in DB gets ADMIN, rest get STORE_MANAGER
        long existingCount = adminUserRepository.count();
        UserRole role = existingCount == 0 ? UserRole.ADMIN : UserRole.STORE_MANAGER;

        AdminUser user = new AdminUser();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        AdminUser saved = adminUserRepository.save(user);

        log.info("New admin user created: email={} role={}", saved.getEmail(), saved.getRole());

        AuthDtos.SignupResponse response = new AuthDtos.SignupResponse();
        response.setUserId(saved.getId());
        response.setFullName(saved.getFullName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole().name());
        response.setMessage(role == UserRole.ADMIN
                ? "Account created. You are the first admin."
                : "Account created. Role: STORE_MANAGER.");
        return response;
    }

    // ── Me ─────────────────────────────────────────────────────────────────────
    // Returns profile of currently logged-in user
    @Override
    public AuthDtos.MeResponse me(String email) {

        AdminUser user = adminUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthDtos.MeResponse response = new AuthDtos.MeResponse();
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return response;
    }

    // ── Create admin user ──────────────────────────────────────────────────────
    // Only ADMIN can create another user with a specific role
    @Override
    public AuthDtos.AdminUserResponse createAdminUser(AuthDtos.CreateAdminUserRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (adminUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            role = UserRole.STORE_MANAGER;
        }

        AdminUser user = new AdminUser();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        AdminUser saved = adminUserRepository.save(user);

        log.info("Admin created user: email={} role={}", saved.getEmail(), saved.getRole());

        return toAdminUserResponse(saved);
    }

    // ── Get all users ──────────────────────────────────────────────────────────
    @Override
    public List<AuthDtos.AdminUserResponse> getUsers() {
        return adminUserRepository.findAll()
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    // ── Helper ─────────────────────────────────────────────────────────────────
    private AuthDtos.AdminUserResponse toAdminUserResponse(AdminUser user) {
        AuthDtos.AdminUserResponse r = new AuthDtos.AdminUserResponse();
        r.setUserId(user.getId());
        r.setFullName(user.getFullName());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole().name());
        r.setActive(user.getActive());
        return r;
    }
}