package com.example.titan_watch_learning_project.entity;//package com.example.titan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private String phone;

    @Column(name = "campaign_type")
    private String campaignType; // "T10" or "TDAY"

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.SENT;

    @Column(name = "sent_at")
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "sent_at_date")
    @Builder.Default
    private LocalDate sentAtDate = LocalDate.now();

    public enum Status { SENT, FAILED, DELIVERED }
}