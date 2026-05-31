package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.UserRole;
import com.example.titan_watch_learning_project.dto.AuthDtos;
import com.example.titan_watch_learning_project.entity.AdminUser;
//import com.example.titan_watch_learning_project.entity.UserRole;
import com.example.titan_watch_learning_project.repository.AdminUserRepository;
import com.example.titan_watch_learning_project.security.JwtUtil;
import com.example.titan_watch_learning_project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String password = request.getPassword() == null ? "" : request.getPassword();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        AdminUser user = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        String token = jwtUtil.generateToken(user);

        return AuthDtos.LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthDtos.MeResponse me(String email) {
        AdminUser user = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthDtos.MeResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthDtos.AdminUserResponse createAdminUser(AuthDtos.CreateAdminUserRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();

        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        if (adminUserRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("User already exists with this email");
        }

        UserRole role = UserRole.ADMIN;

        if (request.getRole() != null && !request.getRole().isBlank()) {
            role = UserRole.valueOf(request.getRole().trim().toUpperCase());
        }

        AdminUser user = new AdminUser();
        user.setFullName(request.getFullName() == null || request.getFullName().isBlank()
                ? "Admin User"
                : request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        AdminUser saved = adminUserRepository.save(user);

        return toResponse(saved);
    }

    @Override
    public List<AuthDtos.AdminUserResponse> getUsers() {
        return adminUserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuthDtos.AdminUserResponse toResponse(AdminUser user) {
        return AuthDtos.AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}