package com.example.titan_watch_learning_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDto {
        private Long id;
        private String customerName;
        private String phone;
        private String currentStep;
        private String rawStep;
        private String selectedCollection;
        private String selectedStyle;
        private Boolean isActive;
        private String lastActivity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadDto {
        private Long id;
        private String customerName;
        private String phone;
        private String leadType;
        private String selectedCollection;
        private String selectedStyle;
        private String priceRange;
        private String status;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsDto {
        private Long totalReached;
        private Long activeSessions;
        private Long callbackLeads;
        private Long storeVisits;
        private Long completedFlows;
        private Long newLeads;
        private Long converted;
        private Integer conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyMessagesDto {
        private List<String> labels;
        private List<Long> inbound;
        private List<Long> outbound;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignWeekDto {
        private List<String> labels;
        private List<Long> t10;
        private List<Long> tday;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionSplitDto {
        private Long mens;
        private Long womens;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEventDto {
        private String icon;
        private String text;
        private String time;
        private String bg;
        private String color;
    }
}