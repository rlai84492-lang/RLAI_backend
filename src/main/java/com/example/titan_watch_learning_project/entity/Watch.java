package com.example.titan_watch_learning_project.entity;//package com.example.titan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "watches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Collection collection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Style style;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "product_url")
    private String productUrl;

    private String sku;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public enum Collection { MENS, WOMENS }
    public enum Style { MINIMAL_CHIC, BOLD_EDGY, LUXE_CLASSY, SPORTY_ADVENTUROUS }
}