package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
@Slf4j
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * POST /campaign/trigger/t10
     * Manually fires the T-10 scheduler for testing.
     */
    @PostMapping("/trigger/t10")
    public ResponseEntity<String> triggerT10() {
        campaignService.triggerT10Campaign();
        return ResponseEntity.ok("T-10 campaign triggered");
    }

    /**
     * POST /campaign/trigger/tday
     * Manually fires the T-Day scheduler for testing.
     */
    @PostMapping("/trigger/tday")
    public ResponseEntity<String> triggerTDay() {
        campaignService.triggerTDayCampaign();
        return ResponseEntity.ok("T-Day campaign triggered");
    }

    /**
     * POST /campaign/manual?phone=91XXXXXXXXXX&name=Rahul&type=T10
     * Send a test message to any phone number.
     */
    @PostMapping("/manual")
    public ResponseEntity<String> manualTrigger(
            @RequestParam String phone,
            @RequestParam String name,
            @RequestParam(defaultValue = "T10") String type) {
        campaignService.manualTrigger(phone, name, type);
        return ResponseEntity.ok("Manual trigger sent to " + phone);
    }
}