package com.example.titan_watch_learning_project.entity;//package com.example.titan.entity;
//
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bot_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private String phone;

    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private String currentStep = "WELCOME";
    // Steps: WELCOME, COLLECTION, STYLE, CAROUSEL, PRICE_FILTER, CALLBACK_CONFIRM, OFFER, COMPLETED

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_collection")
    private Collection selectedCollection;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_style")
    private Style selectedStyle;

    @Column(name = "selected_price_range")
    private String selectedPriceRange;

    @Column(name = "session_start")
    @Builder.Default
    private LocalDateTime sessionStart = LocalDateTime.now();

    @Column(name = "last_activity")
    @Builder.Default
    private LocalDateTime lastActivity = LocalDateTime.now();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type")
    @Builder.Default
    private CampaignType campaignType = CampaignType.T10;

    public enum Collection { MENS, WOMENS }
    public enum Style { MINIMAL_CHIC, BOLD_EDGY, LUXE_CLASSY, SPORTY_ADVENTUROUS }
    public enum CampaignType { T10, TDAY }
}