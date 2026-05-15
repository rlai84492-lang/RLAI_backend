package com.example.titan_watch_learning_project.entity;//package com.example.titan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private String phone;

    @Column(name = "customer_name")
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "lead_type")
    private LeadType leadType;

    @Column(name = "preferred_time")
    private String preferredTime;

    @Column(name = "store_code")
    private String storeCode;

    @Column(name = "store_name")
    private String storeName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    private String notes;

    @Column(name = "selected_collection")
    private String selectedCollection;

    @Column(name = "selected_style")
    private String selectedStyle;

    @Column(name = "price_range")
    private String priceRange;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum LeadType { CALLBACK, STORE_VISIT, WEBSITE }
    public enum LeadStatus { NEW, ASSIGNED, CONTACTED, CONVERTED, LOST }
}