//package com.example.titan_watch_learning_project.serviceImpl;
//
//import com.example.titan_watch_learning_project.dto.DashboardResponse;
//import com.example.titan_watch_learning_project.entity.Lead;
//import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
//import com.example.titan_watch_learning_project.repository.LeadRepository;
//import com.example.titan_watch_learning_project.repository.MessageRepository;
//import com.example.titan_watch_learning_project.service.DashboardService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DashboardServiceImpl implements DashboardService {
//
//    private final DashboardDataRepository dashboardDataRepository;
//    private final MessageRepository       messageRepository;
//    private final LeadRepository          leadRepository;
//    private final JdbcTemplate            jdbcTemplate;
//
//    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    // ── All flows (fallback, no date filter) ──────────────────────
//    @Override
//    public DashboardResponse getDashboardData() {
//        List<DashboardResponse.SessionDto> sessions = dashboardDataRepository.findSessions();
//        List<DashboardResponse.LeadDto>    leads    = dashboardDataRepository.findLeads();
//        return DashboardResponse.builder()
//                .sessions(sessions)
//                .leads(leads)
//                .metrics(buildMetrics(sessions, leads, "bday_t10", null, null))
//                .hourly(buildHourlyMessages())
//                .collData(buildCollectionSplit(sessions, leads))
//                .timeline(dashboardDataRepository.findRecentActivity())
//                .build();
//    }
//
//    // ── Flow + Date-range wise dashboard ───────────────────────────
//    public DashboardResponse getDashboardDataByFlow(String flow, String range, String startDate, String endDate) {
//
//        // 1. Date range ko actual from/to date mein convert karo
//        LocalDate[] resolved = resolveDateRange(range, startDate, endDate);
//        LocalDate from = resolved[0];
//        LocalDate to   = resolved[1];
//
//        log.info("Dashboard request → flow={} range={} from={} to={}", flow, range, from, to);
//
//        // 2. Flow + date-wise sessions from DB
//        List<DashboardResponse.SessionDto> sessions =
//                dashboardDataRepository.findSessionsByFlowAndDate(flow, from, to);
//
//        // 3. Flow + date-wise leads from DB
//        List<DashboardResponse.LeadDto> leads =
//                dashboardDataRepository.findLeadsByFlowAndDate(flow, from, to);
//
//        return DashboardResponse.builder()
//                .sessions(sessions)
//                .leads(leads)
//                .metrics(buildMetrics(sessions, leads, flow, from, to))
//                .hourly(buildHourlyMessages())
//                .collData(buildCollectionSplit(sessions, leads))
//                .timeline(dashboardDataRepository.findRecentActivityByFlowAndDate(flow, from, to))
//                .build();
//    }
//
//    // ── Backward-compatible overload (no date range) ───────────────
//    public DashboardResponse getDashboardDataByFlow(String flow) {
//        return getDashboardDataByFlow(flow, "today", null, null);
//    }
//
//    @Override
//    public List<DashboardResponse.SessionDto> getSessions() {
//        return dashboardDataRepository.findSessions();
//    }
//
//    // ── Leads page — Pageable + flow + date range ──────────────────
//    public Page<Lead> getLeadsPage(String flow, String range, String startDate, String endDate, Pageable pageable) {
//        LocalDate[] resolved = resolveDateRange(range, startDate, endDate);
//        LocalDate from = resolved[0];
//        LocalDate to   = resolved[1];
//
//        if (flow != null && !flow.isBlank()) {
//            return leadRepository.findByFlowAndDateRange(flow, from.atStartOfDay(), to.plusDays(1).atStartOfDay(), pageable);
//        }
//        return leadRepository.findByDateRange(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), pageable);
//    }
//
//    // ── Date range resolver ─────────────────────────────────────────
//    // "today" | "7days" | "30days" | "custom" → [from, to]
//    private LocalDate[] resolveDateRange(String range, String startDate, String endDate) {
//        LocalDate today = LocalDate.now();
//
//        if (range == null) range = "today";
//
//        switch (range) {
//            case "7days":
//                return new LocalDate[]{ today.minusDays(6), today };
//            case "30days":
//                return new LocalDate[]{ today.minusDays(29), today };
//            case "custom":
//                try {
//                    LocalDate from = startDate != null ? LocalDate.parse(startDate, DATE_FMT) : today;
//                    LocalDate to   = endDate   != null ? LocalDate.parse(endDate,   DATE_FMT) : today;
//                    return new LocalDate[]{ from, to };
//                } catch (Exception e) {
//                    log.warn("Invalid custom date range, falling back to today: {}", e.getMessage());
//                    return new LocalDate[]{ today, today };
//                }
//            case "today":
//            default:
//                return new LocalDate[]{ today, today };
//        }
//    }
//
//    // ── Metrics — Tab-wise + Date-range-wise sahi formula ──────────
//    private DashboardResponse.MetricsDto buildMetrics(
//            List<DashboardResponse.SessionDto> sessions,
//            List<DashboardResponse.LeadDto>    leads,
//            String flow,
//            LocalDate from,
//            LocalDate to
//    ) {
//        // ── Messages Sent ─────────────────────────────────────────
//        long messagesSent = sessions.size();
//
//        // ── Delivery Rate ─────────────────────────────────────────
//        long delivered   = countDeliveredMessages(flow, from, to);
//        long sentMsgs    = countSentMessages(flow, from, to);
//        int deliveryRate = sentMsgs == 0 ? 0
//                : (int) Math.min(100, Math.round((delivered * 100.0) / sentMsgs));
//
//        // ── Active Sessions ───────────────────────────────────────
//        long activeSessions = sessions.stream()
//                .filter(s -> Boolean.TRUE.equals(s.getIsActive())).count();
//
//        // ── Open Rate ─────────────────────────────────────────────
//        long replied = sessions.stream()
//                .filter(s -> s.getCurrentStep() != null
//                        && !s.getCurrentStep().endsWith("_CONFIRMATION_SENT")
//                        && !s.getCurrentStep().endsWith("_TEMPLATE_SENT"))
//                .count();
//        int openRate = messagesSent == 0 ? 0
//                : (int) Math.min(100, Math.round((replied * 100.0) / messagesSent));
//
//        // ── Leads ─────────────────────────────────────────────────
//        long callbackLeads = leads.stream()
//                .filter(l -> "CALLBACK".equalsIgnoreCase(l.getLeadType())).count();
//        long storeVisits   = leads.stream()
//                .filter(l -> "STORE_VISIT".equalsIgnoreCase(l.getLeadType())).count();
//        long newLeads      = leads.stream()
//                .filter(l -> "NEW".equalsIgnoreCase(l.getStatus())).count();
//        long converted     = leads.stream()
//                .filter(l -> "CONVERTED".equalsIgnoreCase(l.getStatus())).count();
//
//        // ── Click Rate ────────────────────────────────────────────
//        int clickRate = replied == 0 ? 0
//                : (int) Math.min(100, Math.round(
//                ((callbackLeads + storeVisits) * 100.0) / replied));
//
//        // ── Catalogue Views ───────────────────────────────────────
//        long catalogueViews = sessions.stream()
//                .filter(s -> s.getCurrentStep() != null
//                        && s.getCurrentStep().contains("CATALOGUE")).count();
//
//        // ── Completion Rate ───────────────────────────────────────
//        long completedFlows = sessions.stream()
//                .filter(s -> s.getCurrentStep() != null
//                        && s.getCurrentStep().contains("COMPLETED")).count();
//        int completionRate = messagesSent == 0 ? 0
//                : (int) Math.min(100, Math.round((completedFlows * 100.0) / messagesSent));
//
//        // ── Conversion Rate ───────────────────────────────────────
//        int conversionRate = messagesSent == 0 ? 0
//                : (int) Math.min(100, Math.round((converted * 100.0) / messagesSent));
//
//        return DashboardResponse.MetricsDto.builder()
//                .messagesSent(messagesSent)
//                .deliveryRate(deliveryRate)
//                .openRate(openRate)
//                .clickRate(clickRate)
//                .callbackRequests(callbackLeads)
//                .storeVisitRequests(storeVisits)
//                .catalogueViews(catalogueViews)
//                .completionRate(completionRate)
//                .totalReached(messagesSent)
//                .activeSessions(activeSessions)
//                .callbackLeads(callbackLeads)
//                .storeVisits(storeVisits)
//                .completedFlows(completedFlows)
//                .newLeads(newLeads)
//                .converted(converted)
//                .conversionRate(conversionRate)
//                .build();
//    }
//
//    // ── Delivered messages — flow + date range ─────────────────────
//    private long countDeliveredMessages(String flow, LocalDate from, LocalDate to) {
//        try {
//            String pattern = flowToPattern(flow);
//            String sql =
//                    "SELECT COUNT(*) FROM messages m " +
//                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
//                            "WHERE bs.current_step LIKE ? " +
//                            "AND m.status = 'DELIVERED' " +
//                            "AND m.direction = 'OUTBOUND' " +
//                            "AND DATE(m.sent_at) BETWEEN ? AND ?";
//            Long count = jdbcTemplate.queryForObject(sql, Long.class, pattern, from, to);
//            return count != null ? count : 0L;
//        } catch (Exception e) {
//            log.warn("countDeliveredMessages failed: {}", e.getMessage());
//            return 0L;
//        }
//    }
//
//    // ── Sent messages — flow + date range ──────────────────────────
//    private long countSentMessages(String flow, LocalDate from, LocalDate to) {
//        try {
//            String pattern = flowToPattern(flow);
//            String sql =
//                    "SELECT COUNT(*) FROM messages m " +
//                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
//                            "WHERE bs.current_step LIKE ? " +
//                            "AND m.direction = 'OUTBOUND' " +
//                            "AND DATE(m.sent_at) BETWEEN ? AND ?";
//            Long count = jdbcTemplate.queryForObject(sql, Long.class, pattern, from, to);
//            return count != null ? count : 0L;
//        } catch (Exception e) {
//            log.warn("countSentMessages failed: {}", e.getMessage());
//            return 0L;
//        }
//    }
//
//    // ── Flow to SQL LIKE pattern ────────────────────────────────────
//    private String flowToPattern(String flow) {
//        if (flow == null) return "BIRTHDAY_T10_%";
//        return switch (flow) {
//            case "bday_t10"  -> "BIRTHDAY_T10_%";
//            case "bday_t0"   -> "BIRTHDAY_TDAY_%";
//            case "anniv_t10" -> "ANNIVERSARY_T10_%";
//            case "anniv_t0"  -> "ANNIVERSARY_TDAY_%";
//            default          -> "BIRTHDAY_T10_%";
//        };
//    }
//
//    // ── Hourly messages chart (always today — chart-specific) ──────
//    private DashboardResponse.HourlyMessagesDto buildHourlyMessages() {
//        Map<Integer, Map<String, Long>> dbCounts =
//                dashboardDataRepository.findMessageCountsToday();
//
//        List<String> labels   = new ArrayList<>();
//        List<Long>   inbound  = new ArrayList<>();
//        List<Long>   outbound = new ArrayList<>();
//
//        for (int hour = 8; hour <= 20; hour++) {
//            labels.add(String.format("%02d:00", hour));
//            Map<String, Long> c = dbCounts.getOrDefault(hour, new HashMap<>());
//            inbound.add(c.getOrDefault("INBOUND",  0L));
//            outbound.add(c.getOrDefault("OUTBOUND", 0L));
//        }
//
//        return DashboardResponse.HourlyMessagesDto.builder()
//                .labels(labels)
//                .inbound(inbound)
//                .outbound(outbound)
//                .build();
//    }
//
//    // ── Collection split ──────────────────────────────────────────
//    private DashboardResponse.CollectionSplitDto buildCollectionSplit(
//            List<DashboardResponse.SessionDto> sessions,
//            List<DashboardResponse.LeadDto>    leads
//    ) {
//        long mens = 0, womens = 0;
//        for (DashboardResponse.SessionDto s : sessions) {
//            if ("MENS".equalsIgnoreCase(s.getSelectedCollection()))   mens++;
//            if ("WOMENS".equalsIgnoreCase(s.getSelectedCollection())) womens++;
//        }
//        for (DashboardResponse.LeadDto l : leads) {
//            if ("MENS".equalsIgnoreCase(l.getSelectedCollection()))   mens++;
//            if ("WOMENS".equalsIgnoreCase(l.getSelectedCollection())) womens++;
//        }
//        return DashboardResponse.CollectionSplitDto.builder()
//                .mens(mens)
//                .womens(womens)
//                .build();
//    }
//
//    // ── Leads list (no filter — settings page etc.) ─────────────────
//    @Override
//    public List<DashboardResponse.LeadDto> getLeads() {
//        return leadRepository.findAllByOrderByCreatedAtDesc()
//                .stream()
//                .map(this::toLeadDto)
//                .toList();
//    }
//
//    // ── Lead entity → DTO ─────────────────────────────────────────
//    public DashboardResponse.LeadDto toLeadDto(Lead lead) {
//        return DashboardResponse.LeadDto.builder()
//                .id(lead.getId())
//                .customerName(lead.getCustomerName())
//                .phone(lead.getPhone())
//                .leadType(lead.getLeadType() == null ? null : lead.getLeadType().name())
//                .flow(lead.getFlow())
//                .selectedCollection(lead.getSelectedCollection())
//                .selectedBrand(lead.getSelectedBrand())
//                .stepName(lead.getStepName())
//                .status(lead.getStatus() == null ? null : lead.getStatus().name())
//                .notes(lead.getNotes())
//                .createdAt(lead.getCreatedAt())
//                .build();
//    }
//}



package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
import com.example.titan_watch_learning_project.repository.LeadRepository;
import com.example.titan_watch_learning_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic layer — SAARI calculations, formulas, validations yahin hote hain.
 * Database access ke liye sirf DashboardDataRepository / LeadRepository ko call karta hai.
 * Koi native SQL yahan NAHI likhi jaati.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardDataRepository dashboardDataRepository;
    private final LeadRepository          leadRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ════════════════════════════════════════════════════════════
    // ── DASHBOARD ──────────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Saare flows ka combined dashboard — date filter nahi (fallback/legacy). */
    @Override
    public DashboardResponse getDashboardData() {
        List<DashboardResponse.SessionDto> sessions = dashboardDataRepository.findSessions();
        List<DashboardResponse.LeadDto>    leads    = dashboardDataRepository.findLeads();

        return DashboardResponse.builder()
                .sessions(sessions)
                .leads(leads)
                .metrics(buildMetrics(sessions, leads, "bday_t10", null, null))
                .hourly(buildHourlyMessages())
                .collData(buildCollectionSplit(sessions, leads))
                .timeline(dashboardDataRepository.findRecentActivity())
                .build();
    }

    /** Flow + date range wise dashboard — yahi main API hai. */
    @Override
    public DashboardResponse getDashboardDataByFlow(String flow, String range, String startDate, String endDate) {
        LocalDate[] resolved = resolveDateRange(range, startDate, endDate);
        LocalDate from = resolved[0];
        LocalDate to   = resolved[1];

        log.info("Dashboard request → flow={} range={} from={} to={}", flow, range, from, to);

        List<DashboardResponse.SessionDto> sessions =
                dashboardDataRepository.findSessionsByFlowAndDate(flow, from, to);

        List<DashboardResponse.LeadDto> leads =
                dashboardDataRepository.findLeadsByFlowAndDate(flow, from, to);

        return DashboardResponse.builder()
                .sessions(sessions)
                .leads(leads)
                .metrics(buildMetrics(sessions, leads, flow, from, to))
                .hourly(buildHourlyMessages())
                .collData(buildCollectionSplit(sessions, leads))
                .timeline(dashboardDataRepository.findRecentActivityByFlowAndDate(flow, from, to))
                .build();
    }

    /** Backward-compatible overload — default range="today". */
    @Override
    public DashboardResponse getDashboardDataByFlow(String flow) {
        return getDashboardDataByFlow(flow, "today", null, null);
    }

    @Override
    public List<DashboardResponse.SessionDto> getSessions() {
        return dashboardDataRepository.findSessions();
    }

    // ════════════════════════════════════════════════════════════
    // ── LEADS ──────────────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Leads page — flow + date range wise, paginated. */
    @Override
    public Page<Lead> getLeadsPage(String flow, String range, String startDate, String endDate, Pageable pageable) {
        LocalDate[] resolved = resolveDateRange(range, startDate, endDate);
        LocalDate from = resolved[0];
        LocalDate to   = resolved[1];

        if (flow != null && !flow.isBlank()) {
            return leadRepository.findByFlowAndDateRange(
                    flow,
                    from.atStartOfDay(),
                    to.plusDays(1).atStartOfDay(),
                    pageable
            );
        }
        return leadRepository.findByDateRange(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                pageable
        );
    }

    /** Saare leads — koi filter nahi (settings/export page jaisi jagah ke liye). */
    @Override
    public List<DashboardResponse.LeadDto> getLeads() {
        return leadRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toLeadDto)
                .toList();
    }

    /** Lead entity → API response DTO. */
    @Override
    public DashboardResponse.LeadDto toLeadDto(Lead lead) {
        return DashboardResponse.LeadDto.builder()
                .id(lead.getId())
                .customerName(lead.getCustomerName())
                .phone(lead.getPhone())
                .leadType(lead.getLeadType() == null ? null : lead.getLeadType().name())
                .flow(lead.getFlow())
                .selectedCollection(lead.getSelectedCollection())
                .selectedBrand(lead.getSelectedBrand())
                .stepName(lead.getStepName())
                .status(lead.getStatus() == null ? null : lead.getStatus().name())
                .notes(lead.getNotes())
                .createdAt(lead.getCreatedAt())
                .build();
    }

    // ════════════════════════════════════════════════════════════
    // ── DATE RANGE RESOLVER ────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /**
     * Frontend se aane wala range string ("today" | "7days" | "30days" | "custom")
     * ko actual [from, to] LocalDate range mein convert karta hai.
     */
    private LocalDate[] resolveDateRange(String range, String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        if (range == null) range = "today";

        return switch (range) {
            case "7days"  -> new LocalDate[]{ today.minusDays(6),  today };
            case "30days" -> new LocalDate[]{ today.minusDays(29), today };
            case "custom" -> resolveCustomRange(startDate, endDate, today);
            default       -> new LocalDate[]{ today, today };   // "today"
        };
    }

    private LocalDate[] resolveCustomRange(String startDate, String endDate, LocalDate today) {
        try {
            LocalDate from = startDate != null ? LocalDate.parse(startDate, DATE_FMT) : today;
            LocalDate to   = endDate   != null ? LocalDate.parse(endDate,   DATE_FMT) : today;
            return new LocalDate[]{ from, to };
        } catch (Exception e) {
            log.warn("Invalid custom date range, falling back to today: {}", e.getMessage());
            return new LocalDate[]{ today, today };
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── METRICS — Saari business calculations yahan ────────────
    // ════════════════════════════════════════════════════════════

    /**
     * 8 dashboard metric tiles ke liye saare formulas.
     * Raw counts Repository se aate hain, calculation yahin hota hai.
     */
    private DashboardResponse.MetricsDto buildMetrics(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto>    leads,
            String flow,
            LocalDate from,
            LocalDate to
    ) {
        // ── Messages Sent = is flow ke total sessions ──────────────
        long messagesSent = sessions.size();

        // ── Delivery Rate = delivered / sent (messages table se) ───
        long delivered   = dashboardDataRepository.countDeliveredMessages(flow, from, to);
        long sentMsgs    = dashboardDataRepository.countSentMessages(flow, from, to);
        int deliveryRate = calculatePercentage(delivered, sentMsgs);

        // ── Active Sessions ──────────────────────────────────────
        long activeSessions = countActiveSessions(sessions);

        // ── Open Rate = jin users ne reply kiya (confirmation step paar kiya) ──
        long replied  = countRepliedUsers(sessions);
        int openRate  = calculatePercentage(replied, messagesSent);

        // ── Lead counts ──────────────────────────────────────────
        long callbackLeads = countLeadsByType(leads, "CALLBACK");
        long storeVisits   = countLeadsByType(leads, "STORE_VISIT");
        long newLeads      = countLeadsByStatus(leads, "NEW");
        long converted     = countLeadsByStatus(leads, "CONVERTED");

        // ── Click Rate = (callback+storeVisit) / replied users ──────
        int clickRate = calculatePercentage(callbackLeads + storeVisits, replied);

        // ── Catalogue Views ──────────────────────────────────────
        long catalogueViews = countSessionsContainingStep(sessions, "CATALOGUE");

        // ── Completion Rate ───────────────────────────────────────
        long completedFlows = countSessionsContainingStep(sessions, "COMPLETED");
        int completionRate  = calculatePercentage(completedFlows, messagesSent);

        // ── Conversion Rate ───────────────────────────────────────
        int conversionRate = calculatePercentage(converted, messagesSent);

        return DashboardResponse.MetricsDto.builder()
                .messagesSent(messagesSent)
                .deliveryRate(deliveryRate)
                .openRate(openRate)
                .clickRate(clickRate)
                .callbackRequests(callbackLeads)
                .storeVisitRequests(storeVisits)
                .catalogueViews(catalogueViews)
                .completionRate(completionRate)
                .totalReached(messagesSent)
                .activeSessions(activeSessions)
                .callbackLeads(callbackLeads)
                .storeVisits(storeVisits)
                .completedFlows(completedFlows)
                .newLeads(newLeads)
                .converted(converted)
                .conversionRate(conversionRate)
                .build();
    }

    // ── Metric calculation helpers (pure business logic, no SQL) ───

    /** Safe percentage calculator — divide-by-zero handle karta hai, max 100% cap karta hai. */
    private int calculatePercentage(long numerator, long denominator) {
        if (denominator == 0) return 0;
        return (int) Math.min(100, Math.round((numerator * 100.0) / denominator));
    }

    private long countActiveSessions(List<DashboardResponse.SessionDto> sessions) {
        return sessions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .count();
    }

    /**
     * "Replied" = user ne confirmation/template step paar karke aage badha
     * (matlab sirf template milna nahi, balki reply bhi kiya).
     */
    private long countRepliedUsers(List<DashboardResponse.SessionDto> sessions) {
        return sessions.stream()
                .filter(s -> s.getCurrentStep() != null
                        && !s.getCurrentStep().endsWith("_CONFIRMATION_SENT")
                        && !s.getCurrentStep().endsWith("_TEMPLATE_SENT"))
                .count();
    }

    private long countLeadsByType(List<DashboardResponse.LeadDto> leads, String leadType) {
        return leads.stream()
                .filter(l -> leadType.equalsIgnoreCase(l.getLeadType()))
                .count();
    }

    private long countLeadsByStatus(List<DashboardResponse.LeadDto> leads, String status) {
        return leads.stream()
                .filter(l -> status.equalsIgnoreCase(l.getStatus()))
                .count();
    }

    private long countSessionsContainingStep(List<DashboardResponse.SessionDto> sessions, String stepKeyword) {
        return sessions.stream()
                .filter(s -> s.getCurrentStep() != null && s.getCurrentStep().contains(stepKeyword))
                .count();
    }

    // ════════════════════════════════════════════════════════════
    // ── HOURLY MESSAGES CHART ───────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Aaj ke 8AM-8PM ke har ghante ka inbound/outbound chart data. */
    private DashboardResponse.HourlyMessagesDto buildHourlyMessages() {
        Map<Integer, Map<String, Long>> dbCounts = dashboardDataRepository.findMessageCountsToday();

        List<String> labels   = new ArrayList<>();
        List<Long>   inbound  = new ArrayList<>();
        List<Long>   outbound = new ArrayList<>();

        for (int hour = 8; hour <= 20; hour++) {
            labels.add(String.format("%02d:00", hour));
            Map<String, Long> c = dbCounts.getOrDefault(hour, new HashMap<>());
            inbound.add(c.getOrDefault("INBOUND",  0L));
            outbound.add(c.getOrDefault("OUTBOUND", 0L));
        }

        return DashboardResponse.HourlyMessagesDto.builder()
                .labels(labels)
                .inbound(inbound)
                .outbound(outbound)
                .build();
    }

    // ════════════════════════════════════════════════════════════
    // ── COLLECTION SPLIT (Men's vs Women's chart) ──────────────
    // ════════════════════════════════════════════════════════════

    private DashboardResponse.CollectionSplitDto buildCollectionSplit(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto>    leads
    ) {
        long mens   = countByCollection(sessions, leads, "MENS");
        long womens = countByCollection(sessions, leads, "WOMENS");

        return DashboardResponse.CollectionSplitDto.builder()
                .mens(mens)
                .womens(womens)
                .build();
    }

    private long countByCollection(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto>    leads,
            String collection
    ) {
        long fromSessions = sessions.stream()
                .filter(s -> collection.equalsIgnoreCase(s.getSelectedCollection()))
                .count();
        long fromLeads = leads.stream()
                .filter(l -> collection.equalsIgnoreCase(l.getSelectedCollection()))
                .count();
        return fromSessions + fromLeads;
    }
}