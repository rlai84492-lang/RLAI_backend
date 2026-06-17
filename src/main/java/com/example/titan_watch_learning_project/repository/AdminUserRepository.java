package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}