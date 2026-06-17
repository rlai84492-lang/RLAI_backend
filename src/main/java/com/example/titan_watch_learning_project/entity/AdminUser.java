package com.example.titan_watch_learning_project.entity;

import com.example.titan_watch_learning_project.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="full_name", nullable = false)
    private String fullName;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="password_hash", nullable = false, length = 1000)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private UserRole role = UserRole.ADMIN;

    @Column(name="active", nullable = false)
    private Boolean active = true;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }

        if (this.role == null) {
            this.role = UserRole.ADMIN;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}