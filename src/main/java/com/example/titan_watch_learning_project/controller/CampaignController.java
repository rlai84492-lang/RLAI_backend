package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.service.CampaignTriggerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignTriggerService campaignTriggerService;

    // ─────────────────────────────────────────────────────────────
    // BIRTHDAY T-10
    // Titan CRM calls this → then Karix sends the template
    // POST /api/campaign/birthday-t10
    // Body: { "phone": "919876543210", "customerId": 123 }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/birthday-t10")
    public ResponseEntity<?> prepareBirthdayT10(
            @RequestBody CampaignRequest request) {

        log.info("Campaign trigger: Birthday T-10 phone={}", request.getPhone());
        campaignTriggerService.prepareBirthdayT10Session(
                request.getPhone(),
                request.getCustomerId()
        );
        return ResponseEntity.ok(Map.of(
                "status", "session_ready",
                "flow", "birthday_t10",
                "phone", request.getPhone(),
                "message", "Session created. Now send the Karix template."
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // BIRTHDAY T-DAY (T-0)
    // POST /api/campaign/birthday-tday
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/birthday-tday")
    public ResponseEntity<?> prepareBirthdayTDay(
            @RequestBody CampaignRequest request) {

        log.info("Campaign trigger: Birthday T-Day phone={}", request.getPhone());
        campaignTriggerService.prepareBirthdayTDaySession(
                request.getPhone(),
                request.getCustomerId()
        );
        return ResponseEntity.ok(Map.of(
                "status", "session_ready",
                "flow", "birthday_tday",
                "phone", request.getPhone(),
                "message", "Session created. Now send the Karix template."
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // ANNIVERSARY T-10
    // POST /api/campaign/anniversary-t10
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/anniversary-t10")
    public ResponseEntity<?> prepareAnniversaryT10(
            @RequestBody CampaignRequest request) {

        log.info("Campaign trigger: Anniversary T-10 phone={}", request.getPhone());
        campaignTriggerService.prepareAnniversaryT10Session(
                request.getPhone(),
                request.getCustomerId()
        );
        return ResponseEntity.ok(Map.of(
                "status", "session_ready",
                "flow", "anniversary_t10",
                "phone", request.getPhone(),
                "message", "Session created. Now send the Karix template."
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // ANNIVERSARY T-DAY (T-0)
    // POST /api/campaign/anniversary-tday
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/anniversary-tday")
    public ResponseEntity<?> prepareAnniversaryTDay(
            @RequestBody CampaignRequest request) {

        log.info("Campaign trigger: Anniversary T-Day phone={}", request.getPhone());
        campaignTriggerService.prepareAnniversaryTDaySession(
                request.getPhone(),
                request.getCustomerId()
        );
        return ResponseEntity.ok(Map.of(
                "status", "session_ready",
                "flow", "anniversary_tday",
                "phone", request.getPhone(),
                "message", "Session created. Now send the Karix template."
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // Batch endpoint — Titan CRM ek saath multiple users bhej sakta hai
    // POST /api/campaign/batch
    // Body: { "flow": "birthday_t10", "users": [{"phone":"91..","customerId":1}] }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/batch")
    public ResponseEntity<?> prepareBatch(
            @RequestBody BatchRequest request) {

        int count = 0;
        for (CampaignRequest user : request.getUsers()) {
            try {
                switch (request.getFlow()) {
                    case "birthday_t10" ->
                            campaignTriggerService.prepareBirthdayT10Session(
                                    user.getPhone(), user.getCustomerId());
                    case "birthday_tday" ->
                            campaignTriggerService.prepareBirthdayTDaySession(
                                    user.getPhone(), user.getCustomerId());
                    case "anniversary_t10" ->
                            campaignTriggerService.prepareAnniversaryT10Session(
                                    user.getPhone(), user.getCustomerId());
                    case "anniversary_tday" ->
                            campaignTriggerService.prepareAnniversaryTDaySession(
                                    user.getPhone(), user.getCustomerId());
                }
                count++;
            } catch (Exception e) {
                log.error("Failed to prepare session phone={} error={}",
                        user.getPhone(), e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "batch_complete",
                "flow", request.getFlow(),
                "sessionsCreated", count,
                "total", request.getUsers().size()
        ));
    }

    // ─── Request DTOs ─────────────────────────────────────────────
    @Data
    public static class CampaignRequest {
        private String phone;
        private Long   customerId;
    }

    @Data
    public static class BatchRequest {
        private String flow;  // birthday_t10 | birthday_tday | anniversary_t10 | anniversary_tday
        private java.util.List<CampaignRequest> users;
    }
}