package com.example.titan_watch_learning_project.entity;//package com.example.titan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private String phone;

    private String mid;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "button_payload")
    private String buttonPayload;

    @Column(name = "step_name")
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.SENT;

    @Column(name = "sent_at")
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public enum Direction  { OUTBOUND, INBOUND }
    public enum MessageType { TEXT, BUTTON, CAROUSEL, IMAGE, TEMPLATE }
    public enum Status { SENT, DELIVERED, READ, FAILED, RECEIVED }
}