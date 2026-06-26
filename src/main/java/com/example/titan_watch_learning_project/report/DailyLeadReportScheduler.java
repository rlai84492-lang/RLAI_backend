package com.example.titan_watch_learning_project.report;//package com.example.titan_watch_learning_project.scheduler;

import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.repository.LeadRepository;
import com.example.titan_watch_learning_project.serviceImpl.EmailService;
import com.example.titan_watch_learning_project.serviceImpl.LeadExcelReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Roz fixed time pe automatically chalta hai (application.properties
 * mein "report.email.cron" se control hota hai) — us din ke saare
 * leads ka Excel banake email bhejta hai.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLeadReportScheduler {

    private final LeadRepository          leadRepository;
    private final LeadExcelReportService  excelReportService;
    private final EmailService            emailService;

    @Value("${report.email.enabled:false}")
    private boolean reportEnabled;

    @Value("${report.email.recipients:}")
    private String recipients;

    /**
     * Cron expression application.properties se aata hai:
     * report.email.cron=0 0 23 * * *   ← roz raat 11 PM
     *
     * Cron format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "${report.email.cron:0 0 23 * * *}")
    public void sendDailyLeadReport() {

        if (!reportEnabled) {
            log.info("Daily lead report is disabled (report.email.enabled=false). Skipping.");
            return;
        }

        if (recipients == null || recipients.isBlank()) {
            log.warn("Daily lead report recipients not configured. Skipping.");
            return;
        }

        try {
            log.info("Daily lead report job started...");

            // ── 1. Aaj ke saare leads nikalo (00:00 se ab tak) ──
//            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime startOfDay = LocalDate.now().minusDays(7).atStartOfDay();

            LocalDateTime endOfDay   = LocalDateTime.now();

            List<Lead> todaysLeads = leadRepository.findByCreatedAtBetween(startOfDay, endOfDay);

            if (todaysLeads.isEmpty()) {
                log.info("No leads found for today. Skipping email (or send empty report — see note below).");
                // Agar khali din mein bhi email chahiye, ye return hata do
                return;
            }

            // ── 2. Excel banao ───────────────────────────────────
            String sheetTitle = "Leads - " + LocalDate.now();
            byte[] excelBytes = excelReportService.generateExcel(todaysLeads, sheetTitle);

            // ── 3. Summary counts nikalo email body ke liye ──────
            long callbacks   = todaysLeads.stream().filter(l -> l.getLeadType() == Lead.LeadType.CALLBACK).count();
            long storeVisits = todaysLeads.stream().filter(l -> l.getLeadType() == Lead.LeadType.STORE_VISIT).count();

            String emailBody = emailService.buildDailyReportEmailBody(
                    todaysLeads.size(), (int) callbacks, (int) storeVisits
            );

            // ── 4. Email bhejo ────────────────────────────────────
            String dateStr   = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fileName  = "TitanWorld_Leads_" + dateStr + ".xlsx";
            String subject   = "Titan World — Daily Lead Report (" + dateStr + ") — " + todaysLeads.size() + " leads";

            emailService.sendEmailWithExcelAttachment(
                    recipients, subject, emailBody, excelBytes, fileName
            );

            log.info("Daily lead report sent successfully. totalLeads={} recipients={}",
                    todaysLeads.size(), recipients);

        } catch (Exception e) {
            log.error("Daily lead report job failed", e);
        }
    }
}