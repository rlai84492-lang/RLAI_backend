package com.example.titan_watch_learning_project.entity;//package com.example.titan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Store {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_code", unique = true)
    private String storeCode;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(nullable = false)
    private String address;

    private String city;
    private String timings;
    private String phone;

    @Column(name = "maps_link")
    private String mapsLink;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}