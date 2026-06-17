package com.example.titan_watch_learning_project.dto;

import lombok.Data;

public class AuthDtos {

    // ── Login ────────────────────────────────────────────────────
    // What frontend sends to /api/auth/login
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    // What /api/auth/login returns
    @Data
    public static class LoginResponse {
        private String token;
        private Long   userId;
        private String fullName;
        private String email;
        private String role;
    }

    // ── Signup ────────────────────────────────────────────────────
    // What frontend sends to /api/auth/signup
    @Data
    public static class SignupRequest {
        private String fullName;
        private String email;
        private String password;
    }

    // What /api/auth/signup returns
    @Data
    public static class SignupResponse {
        private Long   userId;
        private String fullName;
        private String email;
        private String role;
        private String message;
    }

    // ── Me ────────────────────────────────────────────────────────
    // What /api/auth/me returns
    @Data
    public static class MeResponse {
        private Long   userId;
        private String fullName;
        private String email;
        private String role;
    }

    // ── Admin user management ─────────────────────────────────────
    // What /api/auth/users POST accepts
    @Data
    public static class CreateAdminUserRequest {
        private String fullName;
        private String email;
        private String password;
        private String role; // ADMIN or STORE_MANAGER
    }

    // What /api/auth/users returns per user
    @Data
    public static class AdminUserResponse {
        private Long    userId;
        private String  fullName;
        private String  email;
        private String  role;
        private Boolean active;
    }
}