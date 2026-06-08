package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.dto.AuthDtos;

import java.util.List;

public interface AuthService {

    AuthDtos.LoginResponse login(AuthDtos.LoginRequest request);

    // ── Signup ─────────────────────────────────────────────────────────────────
    // Creates a new admin user.
    // If no users exist in DB → role = ADMIN (first user is always ADMIN)
    // If users already exist → role = STORE_MANAGER (needs ADMIN approval to upgrade)
    AuthDtos.SignupResponse signup(AuthDtos.SignupRequest request);

    AuthDtos.MeResponse me(String email);

    AuthDtos.AdminUserResponse createAdminUser(AuthDtos.CreateAdminUserRequest request);

    List<AuthDtos.AdminUserResponse> getUsers();
}