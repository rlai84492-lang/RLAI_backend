package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.dto.AuthDtos;

import java.util.List;

public interface AuthService {

    AuthDtos.LoginResponse login(AuthDtos.LoginRequest request);

    AuthDtos.MeResponse me(String email);

    AuthDtos.AdminUserResponse createAdminUser(AuthDtos.CreateAdminUserRequest request);

    List<AuthDtos.AdminUserResponse> getUsers();
}