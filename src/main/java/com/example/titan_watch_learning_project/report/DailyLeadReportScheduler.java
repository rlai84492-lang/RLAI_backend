//package com.example.titan_watch_learning_project.report;//package com.example.titan_watch_learning_project.scheduler;
//
//import com.example.titan_watch_learning_project.entity.Lead;
//import com.example.titan_watch_learning_project.repository.LeadRepository;
//import com.example.titan_watch_learning_project.serviceImpl.EmailService;
//import com.example.titan_watch_learning_project.serviceImpl.LeadExcelReportService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
///**
// * Roz fixed time pe automatically chalta hai (application.properties
// * mein "report.email.cron" se control hota hai) — us din ke saare
// * leads ka Excel banake email bhejta hai.
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DailyLeadReportScheduler {
//
//    private final LeadRepository          leadRepository;
//    private final LeadExcelReportService  excelReportService;
//    private final EmailService            emailService;
//
//    @Value("${report.email.enabled:false}")
//    private boolean reportEnabled;
//
//    @Value("${report.email.recipients:}")
//    private String recipients;
//
//    /**
//     * Cron expression application.properties se aata hai:
//     * report.email.cron=0 0 23 * * *   ← roz raat 11 PM
//     *
//     * Cron format: second minute hour day-of-month month day-of-week
//     */
//    @Scheduled(cron = "${report.email.cron:0 0 23 * * *}")
//    public void sendDailyLeadReport() {
//
//        if (!reportEnabled) {
//            log.info("Daily lead report is disabled (report.email.enabled=false). Skipping.");
//            return;
//        }
//
//        if (recipients == null || recipients.isBlank()) {
//            log.warn("Daily lead report recipients not configured. Skipping.");
//            return;
//        }
//
//        try {
//            log.info("Daily lead report job started...");
//
//            // ── 1. Aaj ke saare leads nikalo (00:00 se ab tak) ──
////            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
//            LocalDateTime startOfDay = LocalDate.now().minusDays(7).atStartOfDay();
//
//            LocalDateTime endOfDay   = LocalDateTime.now();
//
//
//            List<Lead> todaysLeads = leadRepository.findByCreatedAtBetween(startOfDay, endOfDay);
//
//            if (todaysLeads.isEmpty()) {
//                log.info("No leads found for today. Skipping email (or send empty report — see note below).");
//                // Agar khali din mein bhi email chahiye, ye return hata do
//                return;
//            }
//
//            // ── 2. Excel banao ───────────────────────────────────
//            String sheetTitle = "Leads - " + LocalDate.now();
//            byte[] excelBytes = excelReportService.generateExcel(todaysLeads, sheetTitle);
//
//            // ── 3. Summary counts nikalo email body ke liye ──────
//            long callbacks   = todaysLeads.stream().filter(l -> l.getLeadType() == Lead.LeadType.CALLBACK).count();
//            long storeVisits = todaysLeads.stream().filter(l -> l.getLeadType() == Lead.LeadType.STORE_VISIT).count();
//
//            String emailBody = emailService.buildDailyReportEmailBody(
//                    todaysLeads.size(), (int) callbacks, (int) storeVisits
//            );
//
//            // ── 4. Email bhejo ────────────────────────────────────
//            String dateStr   = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String fileName  = "TitanWorld_Leads_" + dateStr + ".xlsx";
//            String subject   = "Titan World — Daily Lead Report (" + dateStr + ") — " + todaysLeads.size() + " leads";
//
//            emailService.sendEmailWithExcelAttachment(
//                    recipients, subject, emailBody, excelBytes, fileName
//            );
//
//            log.info("Daily lead report sent successfully. totalLeads={} recipients={}",
//                    todaysLeads.size(), recipients);
//
//        } catch (Exception e) {
//            log.error("Daily lead report job failed", e);
//        }
//    }
//}


package com.example.titan_watch_learning_project.report;

//import com.example.titan_watch_learning_project.model.Lead;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLeadReportScheduler {

    private final LeadRepository leadRepository;
    private final EmailService emailService;
    private final LeadExcelReportService excelReportService;

    @Value("${report.email.recipients:}")
    private String recipients;

    @Value("${report.email.enabled:false}")
    private boolean reportEnabled;

    private static final String ALERT_RECIPIENTS =
            "niraj.singh@rightleft.ai,subramaniam.mani@rightleft.ai,samarthdevlibtech@gmail.com";

//    private static final String ALERT_RECIPIENTS = "samarthdevlibtech@gmail.com";

    @Scheduled(cron = "${report.email.cron:0 0 17 * * *}")
    public void sendDailyLeadReport() {

        if (!reportEnabled) {
            log.warn("Daily lead report is disabled. Skipping.");
            return;
        }

        if (recipients == null || recipients.isBlank()) {
            log.warn("Daily lead report recipients not configured. Skipping.");
            return;
        }

        try {
            log.info("Daily lead report job started...");

            // ✅ PRODUCTION — today's leads only
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

            // ❌ TESTING ONLY — uncomment to test with past data
//             LocalDateTime startOfDay = LocalDate.now().minusDays(7).atStartOfDay();



//            boolean testFailureMode = true; // ← Change to false for production
//
//            if (testFailureMode) {
//                throw new RuntimeException("TEST FAILURE — 1=0 condition triggered");
//            }

            LocalDateTime endOfDay = LocalDateTime.now();

//            List<Lead> todaysLeads = leadRepository.findByCreatedAtBetween(startOfDay, endOfDay);
            List<Lead> rawLeads = leadRepository.findByCreatedAtBetween(startOfDay, endOfDay);


            // Deduplicate: same phone + same leadType = ek hi lead
            Map<String, Lead> seen = new java.util.LinkedHashMap<>();
            for (Lead lead : rawLeads) {
                String key = lead.getPhone() + "_" + lead.getLeadType();
                seen.putIfAbsent(key, lead);  // pehli wali rakhta hai, baad wali skip
            }
            List<Lead> todaysLeads = new java.util.ArrayList<>(seen.values());


            if (todaysLeads.isEmpty()) {
                log.info("No leads found for today. Skipping email.");
                return;
            }

            String sheetTitle = "Daily Leads — " + LocalDate.now();
            String subject    = "Titan World — Daily Lead Report (" + LocalDate.now() + ") — "
                    + todaysLeads.size() + " leads";
//            String emailBody  = emailService.buildDailyReportEmailBody(
//                    todaysLeads.size(), 0, 0);


            long callbacks = todaysLeads.stream()
                    .filter(l -> Lead.LeadType.CALLBACK.equals(l.getLeadType()))
                    .count();
            long storeVisits = todaysLeads.stream()
                    .filter(l -> Lead.LeadType.STORE_VISIT.equals(l.getLeadType()))
                    .count();

            String emailBody = emailService.buildDailyReportEmailBody(
                    todaysLeads.size(), (int) callbacks, (int) storeVisits);


            String fileName   = "leads_" + LocalDate.now() + ".xlsx";

            byte[] excelBytes = excelReportService.generateExcel(todaysLeads, sheetTitle);

            // Send report to customers
            emailService.sendEmailWithExcelAttachment(
                    recipients, subject, emailBody, excelBytes, fileName);

            log.info("Daily lead report sent successfully. totalLeads={} recipients={}",
                    todaysLeads.size(), recipients);

            // ✅ SUCCESS ALERT — Niraj + Subbu ko
            emailService.sendEmailWithExcelAttachment(
                    ALERT_RECIPIENTS,
                    "✅ Batch SUCCESS — Daily Lead Report " + LocalDate.now(),
                    emailService.buildSuccessAlertBody(todaysLeads.size(), recipients),
                    new byte[0],
                    ""
            );
            log.info("Success alert sent to internal team.");

        } catch (Exception e) {
            log.error("Daily lead report job FAILED", e);

            // ❌ FAILURE ALERT — Niraj + Subbu ko
            try {
                emailService.sendEmailWithExcelAttachment(
                        ALERT_RECIPIENTS,
                        "❌ ALERT: Batch FAILED — Daily Lead Report " + LocalDate.now(),
                        emailService.buildFailureAlertBody(e.getMessage()),
                        new byte[0],
                        ""
                );
                log.info("Failure alert sent to internal team.");
            } catch (Exception alertEx) {
                log.error("Failed to send failure alert", alertEx);
            }
        }
    }
}