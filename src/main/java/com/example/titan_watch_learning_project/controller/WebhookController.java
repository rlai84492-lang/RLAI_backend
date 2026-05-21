package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.service.WebhookForwardService;
import com.example.titan_watch_learning_project.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;
    private final WebhookForwardService webhookForwardService;

    @Value("${webhook.forward.enabled:false}")
    private boolean forwardEnabled;

    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("Webhook hit — payload length: {}", payload.length());

        /*
         * LOCAL TESTING MODE ONLY:
         * Render receives Karix webhook and forwards it to local/ngrok backend.
         *
         * Enable only during local testing:
         * WEBHOOK_FORWARD_ENABLED=true
         * WEBHOOK_FORWARD_URL=https://your-ngrok-url.ngrok-free.app/webhook
         *
         * Production mein isko false rakho.
         */
        if (forwardEnabled) {
            boolean forwarded = webhookForwardService.forward(payload);
            log.info("Forward mode enabled. Forwarded={}", forwarded);
            return ResponseEntity.ok("OK");
        }

        /*
         * PRODUCTION MODE:
         * Render directly processes Karix webhook and sends bot replies.
         */
        webhookService.handleWebhookEvent(payload);

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Titan Webhook Service is running ✅");
    }
}