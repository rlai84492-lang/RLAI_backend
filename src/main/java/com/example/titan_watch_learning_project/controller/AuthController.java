package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.dto.AuthDtos;
import com.example.titan_watch_learning_project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Public: login ─────────────────────────────────────────────────────────
    // POST /api/auth/login
    // Body: { "email": "...", "password": "..." }
    // Returns JWT token
    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    // ── Public: signup ────────────────────────────────────────────────────────
    // POST /api/auth/signup
    // Body: { "fullName": "...", "email": "...", "password": "..." }
    // First user → ADMIN, subsequent users → STORE_MANAGER
    @PostMapping("/signup")
    public AuthDtos.SignupResponse signup(@RequestBody AuthDtos.SignupRequest request) {
        return authService.signup(request);
    }

    // ── Protected: current user info ──────────────────────────────────────────
    // GET /api/auth/me
    // Requires: Authorization: Bearer <token>
    @GetMapping("/me")
    public AuthDtos.MeResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }

    // ── Admin only: create another user ───────────────────────────────────────
    // POST /api/auth/users
    // Requires: ADMIN role
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthDtos.AdminUserResponse createUser(
            @RequestBody AuthDtos.CreateAdminUserRequest request
    ) {
        return authService.createAdminUser(request);
    }

    // ── Admin only: list all users ────────────────────────────────────────────
    // GET /api/auth/users
    // Requires: ADMIN role
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthDtos.AdminUserResponse> getUsers() {
        return authService.getUsers();
    }
}