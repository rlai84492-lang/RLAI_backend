package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
import com.example.titan_watch_learning_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardDataRepository dashboardDataRepository;

    @Override
    public DashboardResponse getDashboardData() {
        List<DashboardResponse.SessionDto> sessions = dashboardDataRepository.findSessions();
        List<DashboardResponse.LeadDto> leads = dashboardDataRepository.findLeads();

        DashboardResponse.MetricsDto metrics = buildMetrics(sessions, leads);
        DashboardResponse.HourlyMessagesDto hourly = buildHourlyMessages();
        Map<String, Long> styleCounts = buildStyleCounts(sessions, leads);
        Map<String, Long> priceData = buildPriceData(sessions, leads);
        DashboardResponse.CampaignWeekDto campData = buildCampaignWeek();
        DashboardResponse.CollectionSplitDto collData = buildCollectionSplit(sessions, leads);
        List<DashboardResponse.ActivityEventDto> timeline = dashboardDataRepository.findRecentActivity();

        return DashboardResponse.builder()
                .sessions(sessions)
                .leads(leads)
                .metrics(metrics)
                .hourly(hourly)
                .styleCounts(styleCounts)
                .priceData(priceData)
                .campData(campData)
                .collData(collData)
                .timeline(timeline)
                .build();
    }

    @Override
    public List<DashboardResponse.SessionDto> getSessions() {
        return dashboardDataRepository.findSessions();
    }

    @Override
    public List<DashboardResponse.LeadDto> getLeads() {
        return dashboardDataRepository.findLeads();
    }

    private DashboardResponse.MetricsDto buildMetrics(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto> leads
    ) {
        long totalReached = sessions.size();

        long activeSessions = sessions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .count();

        long callbackLeads = leads.stream()
                .filter(l -> "CALLBACK".equalsIgnoreCase(l.getLeadType()))
                .count();

        long storeVisits = leads.stream()
                .filter(l -> "STORE_VISIT".equalsIgnoreCase(l.getLeadType()))
                .count();

        long completedFlows = sessions.stream()
                .filter(s -> "COMPLETED".equalsIgnoreCase(s.getCurrentStep()))
                .count();

        long newLeads = leads.stream()
                .filter(l -> "NEW".equalsIgnoreCase(l.getStatus()))
                .count();

        long converted = leads.stream()
                .filter(l -> "CONVERTED".equalsIgnoreCase(l.getStatus()))
                .count();

        int conversionRate = totalReached == 0
                ? 0
                : (int) Math.round((converted * 100.0) / totalReached);

        return DashboardResponse.MetricsDto.builder()
                .totalReached(totalReached)
                .activeSessions(activeSessions)
                .callbackLeads(callbackLeads)
                .storeVisits(storeVisits)
                .completedFlows(completedFlows)
                .newLeads(newLeads)
                .converted(converted)
                .conversionRate(conversionRate)
                .build();
    }

    private DashboardResponse.HourlyMessagesDto buildHourlyMessages() {
        Map<Integer, Map<String, Long>> dbCounts = dashboardDataRepository.findMessageCountsToday();

        List<String> labels = new ArrayList<>();
        List<Long> inbound = new ArrayList<>();
        List<Long> outbound = new ArrayList<>();

        for (int hour = 8; hour <= 20; hour++) {
            labels.add(String.format("%02d:00", hour));

            Map<String, Long> countByDirection = dbCounts.getOrDefault(hour, new HashMap<>());

            inbound.add(countByDirection.getOrDefault("INBOUND", 0L));
            outbound.add(countByDirection.getOrDefault("OUTBOUND", 0L));
        }

        return DashboardResponse.HourlyMessagesDto.builder()
                .labels(labels)
                .inbound(inbound)
                .outbound(outbound)
                .build();
    }

    private Map<String, Long> buildStyleCounts(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto> leads
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("MINIMAL_CHIC", 0L);
        counts.put("BOLD_EDGY", 0L);
        counts.put("LUXE_CLASSY", 0L);
        counts.put("SPORTY_ADVENTUROUS", 0L);

        for (DashboardResponse.SessionDto session : sessions) {
            addStyleCount(counts, session.getSelectedStyle());
        }

        for (DashboardResponse.LeadDto lead : leads) {
            addStyleCount(counts, lead.getSelectedStyle());
        }

        return counts;
    }

    private void addStyleCount(Map<String, Long> counts, String style) {
        if (style == null || style.isBlank()) return;

        String s = style.toUpperCase();

        if (s.contains("MINIMAL")) {
            counts.put("MINIMAL_CHIC", counts.get("MINIMAL_CHIC") + 1);
        } else if (s.contains("BOLD")) {
            counts.put("BOLD_EDGY", counts.get("BOLD_EDGY") + 1);
        } else if (s.contains("LUXE")) {
            counts.put("LUXE_CLASSY", counts.get("LUXE_CLASSY") + 1);
        } else if (s.contains("SPORTY")) {
            counts.put("SPORTY_ADVENTUROUS", counts.get("SPORTY_ADVENTUROUS") + 1);
        }
    }

    private Map<String, Long> buildPriceData(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto> leads
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("₹2k–5k", 0L);
        counts.put("₹5k–10k", 0L);
        counts.put("₹10k–25k", 0L);
        counts.put(">₹25k", 0L);

        for (DashboardResponse.SessionDto session : sessions) {
            addPriceFromRawStep(counts, session.getRawStep());
        }

        for (DashboardResponse.LeadDto lead : leads) {
            addPriceFromText(counts, lead.getPriceRange());
        }

        return counts;
    }

    private void addPriceFromRawStep(Map<String, Long> counts, String rawStep) {
        if (rawStep == null || rawStep.isBlank()) return;

        String s = rawStep.toUpperCase();

        if (s.contains("PRICE_2K_5K")) {
            counts.put("₹2k–5k", counts.get("₹2k–5k") + 1);
        } else if (s.contains("PRICE_5K_10K")) {
            counts.put("₹5k–10k", counts.get("₹5k–10k") + 1);
        } else if (s.contains("PRICE_10K_25K")) {
            counts.put("₹10k–25k", counts.get("₹10k–25k") + 1);
        } else if (s.contains("PRICE_25K_PLUS")) {
            counts.put(">₹25k", counts.get(">₹25k") + 1);
        }
    }

    private void addPriceFromText(Map<String, Long> counts, String priceRange) {
        if (priceRange == null || priceRange.isBlank()) return;

        String s = priceRange.toUpperCase();

        if (s.contains("2000") || s.contains("2K")) {
            counts.put("₹2k–5k", counts.get("₹2k–5k") + 1);
        } else if (s.contains("5000") || s.contains("5K")) {
            counts.put("₹5k–10k", counts.get("₹5k–10k") + 1);
        } else if (s.contains("10000") || s.contains("10K")) {
            counts.put("₹10k–25k", counts.get("₹10k–25k") + 1);
        } else if (s.contains("25000") || s.contains("25K")) {
            counts.put(">₹25k", counts.get(">₹25k") + 1);
        }
    }

    private DashboardResponse.CampaignWeekDto buildCampaignWeek() {
        Map<String, Long> rawCounts = dashboardDataRepository.findCampaignCountsLast7Days();

        List<String> labels = new ArrayList<>();
        List<Long> t10 = new ArrayList<>();
        List<Long> tday = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            String label = date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            labels.add(label);

            String dateKey = date.toString();

            t10.add(rawCounts.getOrDefault(dateKey + "|T10", 0L)
                    + rawCounts.getOrDefault(dateKey + "|T-10", 0L));

            tday.add(rawCounts.getOrDefault(dateKey + "|TDAY", 0L)
                    + rawCounts.getOrDefault(dateKey + "|T-DAY", 0L));
        }

        return DashboardResponse.CampaignWeekDto.builder()
                .labels(labels)
                .t10(t10)
                .tday(tday)
                .build();
    }

    private DashboardResponse.CollectionSplitDto buildCollectionSplit(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto> leads
    ) {
        long mens = 0;
        long womens = 0;

        for (DashboardResponse.SessionDto session : sessions) {
            String c = session.getSelectedCollection();
            if ("MENS".equalsIgnoreCase(c)) mens++;
            if ("WOMENS".equalsIgnoreCase(c)) womens++;
        }

        for (DashboardResponse.LeadDto lead : leads) {
            String c = lead.getSelectedCollection();
            if ("MENS".equalsIgnoreCase(c)) mens++;
            if ("WOMENS".equalsIgnoreCase(c)) womens++;
        }

        return DashboardResponse.CollectionSplitDto.builder()
                .mens(mens)
                .womens(womens)
                .build();
    }
}