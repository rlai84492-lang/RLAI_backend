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

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthDtos.MeResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthDtos.AdminUserResponse createUser(
            @RequestBody AuthDtos.CreateAdminUserRequest request
    ) {
        return authService.createAdminUser(request);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthDtos.AdminUserResponse> getUsers() {
        return authService.getUsers();
    }
}