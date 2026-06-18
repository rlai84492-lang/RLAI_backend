package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
import com.example.titan_watch_learning_project.repository.LeadRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
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
    private final MessageRepository messageRepository;

    private final LeadRepository leadRepository;

    @Override
    public DashboardResponse getDashboardData() {
        List<DashboardResponse.SessionDto> sessions = dashboardDataRepository.findSessions();
        List<DashboardResponse.LeadDto> leads = dashboardDataRepository.findLeads();

        DashboardResponse.MetricsDto metrics = buildMetrics(sessions, leads);
        DashboardResponse.HourlyMessagesDto hourly = buildHourlyMessages();
//        Map<String, Long> styleCounts = buildStyleCounts(sessions, leads);
//        Map<String, Long> priceData = buildPriceData(sessions, leads);
        DashboardResponse.CollectionSplitDto collData = buildCollectionSplit(sessions, leads);
        List<DashboardResponse.ActivityEventDto> timeline = dashboardDataRepository.findRecentActivity();

        return DashboardResponse.builder()
                .sessions(sessions)
                .leads(leads)
                .metrics(metrics)
                .hourly(hourly)
//                .styleCounts(styleCounts)
//                .priceData(priceData)
                .collData(collData)
                .timeline(timeline)
                .build();
    }



    public DashboardResponse getDashboardDataByFlow(String flow) {
        String likePattern = switch (flow) {
            case "bday_t10"  -> "BIRTHDAY_T10_%";
            case "bday_t0"   -> "BIRTHDAY_TDAY_%";
            case "anniv_t10" -> "ANNIVERSARY_T10_%";
            case "anniv_t0"  -> "ANNIVERSARY_TDAY_%";
            default          -> "BIRTHDAY_T10_%";
        };

        List<DashboardResponse.SessionDto> sessions =
                dashboardDataRepository.findSessionsByFlow(likePattern);
        List<DashboardResponse.LeadDto> leads =
                dashboardDataRepository.findLeadsByFlow(flow);

        return DashboardResponse.builder()
                .sessions(sessions)
                .leads(leads)
                .metrics(buildMetrics(sessions, leads))
                .hourly(buildHourlyMessages())
                .collData(buildCollectionSplit(sessions, leads))
                .timeline(dashboardDataRepository.findRecentActivity())
                .build();
    }


    @Override
    public List<DashboardResponse.SessionDto> getSessions() {
        return dashboardDataRepository.findSessions();
    }


    private DashboardResponse.MetricsDto buildMetrics(
            List<DashboardResponse.SessionDto> sessions,
            List<DashboardResponse.LeadDto> leads
    ) {
        long totalReached     = sessions.size();
        long activeSessions   = sessions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive())).count();
        long callbackLeads    = leads.stream()
                .filter(l -> "CALLBACK".equalsIgnoreCase(l.getLeadType())).count();
        long storeVisits      = leads.stream()
                .filter(l -> "STORE_VISIT".equalsIgnoreCase(l.getLeadType())).count();
        long completedFlows   = sessions.stream()
                .filter(s -> s.getCurrentStep() != null
                        && s.getCurrentStep().contains("COMPLETED")).count();
        long catalogueViews   = sessions.stream()
                .filter(s -> s.getCurrentStep() != null
                        && s.getCurrentStep().contains("CATALOGUE")).count();
        long newLeads         = leads.stream()
                .filter(l -> "NEW".equalsIgnoreCase(l.getStatus())).count();
        long converted        = leads.stream()
                .filter(l -> "CONVERTED".equalsIgnoreCase(l.getStatus())).count();

        // ← Delivery rate — sessions se calculate karo (flow-wise)
        // Active sessions = opened/read kiya
        int deliveryRate  = totalReached == 0 ? 0
                : (int) Math.round((activeSessions * 100.0) / totalReached);
        int openRate      = deliveryRate; // same as delivery for now
        int clickRate     = activeSessions == 0 ? 0
                : (int) Math.round((callbackLeads + storeVisits) * 100.0 / activeSessions);
        int completionRate = totalReached == 0 ? 0
                : (int) Math.round((completedFlows * 100.0) / totalReached);
        int conversionRate = totalReached == 0 ? 0
                : (int) Math.round((converted * 100.0) / totalReached);

        return DashboardResponse.MetricsDto.builder()
                .messagesSent(totalReached)
                .deliveryRate(Math.min(deliveryRate, 100))
                .openRate(Math.min(openRate, 100))
                .clickRate(Math.min(clickRate, 100))
                .callbackRequests(callbackLeads)
                .storeVisitRequests(storeVisits)
                .catalogueViews(catalogueViews)
                .completionRate(completionRate)
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



    public List<DashboardResponse.LeadDto> getLeads() {
        return leadRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(lead -> DashboardResponse.LeadDto.builder()
                        .id(lead.getId())
                        .customerName(lead.getCustomerName())
                        .phone(lead.getPhone())
                        .leadType(lead.getLeadType() == null ? null : lead.getLeadType().name())
                        .selectedCollection(lead.getSelectedCollection())

                        .status(lead.getStatus() == null ? null : lead.getStatus().name())
                        .createdAt(lead.getCreatedAt())
                        .build())
                .toList();
    }


    // DashboardServiceImpl.java mein — public add karo
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
                // ← YE FIX KARO
                .createdAt(lead.getCreatedAt())
                .build();
    }
}