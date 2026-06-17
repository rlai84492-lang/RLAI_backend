package com.example.titan_watch_learning_project.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {

    private List<SessionDto> sessions;
    private List<LeadDto> leads;
    private MetricsDto metrics;
    private HourlyMessagesDto hourly;
    private Map<String, Long> styleCounts;
    private Map<String, Long> priceData;
    private CampaignWeekDto campData;
    private CollectionSplitDto collData;
    private List<ActivityEventDto> timeline;

    // ── Session ──────────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SessionDto {
        private Long    id;
        private String  customerName;
        private String  phone;
        private String  currentStep;   // raw step e.g. BIRTHDAY_T10_CONFIRMATION_SENT
        private String  rawStep;       // same — kept for backward compat
        private String  flow;          // bday_t10 | bday_t0 | anniv_t10 | anniv_t0
        private String  selectedCollection;
        private String  selectedBrand;
        private String  selectedStyle;
        private Boolean isActive;
        private String  lastActivity;
    }

    // ── Lead ─────────────────────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadDto {
        private Long id;
        private String customerName;
        private String phone;
        private String leadType;
        private String flow;
        private String selectedCollection;
        private String selectedBrand;
        private String status;
        private String notes;
        private String stepName;        // ← ADD
        private LocalDateTime createdAt;  // ← String se LocalDateTime karo
    }

    // ── Metrics ───────────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MetricsDto {
        // Campaign-level (shown in 8 tiles)
        private long   messagesSent;
        private int    deliveryRate;
        private int    openRate;
        private int    clickRate;
        private long   callbackRequests;
        private long   storeVisitRequests;
        private long   catalogueViews;
        private int    completionRate;

        // Legacy (kept for backward compat)
        private long   totalReached;
        private long   activeSessions;
        private long   callbackLeads;
        private long   storeVisits;
        private long   completedFlows;
        private long   newLeads;
        private long   converted;
        private int    conversionRate;
    }

    // ── Hourly messages ───────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class HourlyMessagesDto {
        private List<String> labels;
        private List<Long>   inbound;
        private List<Long>   outbound;
    }

    // ── Campaign week ─────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CampaignWeekDto {
        private List<String> labels;
        private List<Long>   t10;
        private List<Long>   tday;
    }

    // ── Collection split ──────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CollectionSplitDto {
        private long mens;
        private long womens;
        private long couples;
    }

    // ── Activity event ────────────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ActivityEventDto {
        private String icon;
        private String text;
        private String time;
        private String bg;
        private String color;
    }
}