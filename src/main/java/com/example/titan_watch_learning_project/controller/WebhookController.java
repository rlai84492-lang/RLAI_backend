package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * POST /webhook
     * Karix POSTs every WhatsApp event here.
     * Always return 200 — a non-2xx causes Karix to retry.
     */
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("Webhook hit — payload length: {}", payload.length());
        webhookService.handleWebhookEvent(payload);
        return ResponseEntity.ok("OK");
    }

    /**
     * GET /webhook/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Titan Webhook Service is running ✅");
    }
}