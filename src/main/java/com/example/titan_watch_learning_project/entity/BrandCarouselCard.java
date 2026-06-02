package com.example.titan_watch_learning_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "brand_carousel_cards")
@Getter
@Setter
public class BrandCarouselCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gender;

    private String brandKey;

    private String title;

    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    private Boolean active = true;

    private Integer displayOrder = 0;

}