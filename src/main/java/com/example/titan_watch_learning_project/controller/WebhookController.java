//package com.example.titan_watch_learning_project.controller;
//
//import com.example.titan_watch_learning_project.service.WebhookForwardService;
//import com.example.titan_watch_learning_project.service.WebhookService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/webhook")
//@RequiredArgsConstructor
//@Slf4j
//public class WebhookController {
//
//    private final WebhookService webhookService;
//    private final WebhookForwardService webhookForwardService;
//
//    @Value("${webhook.forward.enabled:false}")
//    private boolean forwardEnabled;
//
//    @PostMapping
//    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
//        log.info("Webhook hit — payload length: {}", payload.length());
//
//        /*
//         * LOCAL TESTING MODE ONLY:
//         * Render receives Karix webhook and forwards it to local/ngrok backend.
//         *
//         * Enable only during local testing:
//         * WEBHOOK_FORWARD_ENABLED=true
//         * WEBHOOK_FORWARD_URL=https://your-ngrok-url.ngrok-free.app/webhook
//         *
//         * Production mein isko false rakho.
//         */
//        if (forwardEnabled) {
//            boolean forwarded = webhookForwardService.forward(payload);
//            log.info("Forward mode enabled. Forwarded={}", forwarded);
//            return ResponseEntity.ok("OK");
//        }
//
//        /*
//         * PRODUCTION MODE:
//         * Render directly processes Karix webhook and sends bot replies.
//         */
//        webhookService.handleWebhookEvent(payload);
//
//        return ResponseEntity.ok("OK");
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<String> health() {
//        return ResponseEntity.ok("Titan Webhook Service is running ✅");
//    }
//}

package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.serviceImpl.WebhookServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookServiceImpl webhookService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${webhook.forward.enabled:false}")
    private boolean forwardEnabled;

    @Value("${webhook.forward.url:}")
    private String forwardUrl;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Titan Webhook Service is running ✅");
    }

    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("Webhook hit — payload length: {}", payload == null ? 0 : payload.length());

        if (payload == null || payload.isBlank()) {
            log.warn("Empty webhook payload received");
            return ResponseEntity.badRequest().body("Empty payload");
        }

        // Render mode: only forward to ngrok/local and do not process on Render
        if (forwardEnabled && forwardUrl != null && !forwardUrl.isBlank()) {
            forwardToLocal(payload);
            return ResponseEntity.ok("Forwarded to local/ngrok");
        }

        // Local mode: process, save DB, create session, send WhatsApp reply
        webhookService.handleWebhookEvent(payload);

        return ResponseEntity.ok("OK");
    }

    private void forwardToLocal(String payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(forwardUrl, request, String.class);

            log.info("Forwarded webhook to local/ngrok. url={} status={} body={}",
                    forwardUrl, response.getStatusCode(), response.getBody());

        } catch (Exception e) {
            log.error("Failed to forward webhook to local/ngrok. url={}", forwardUrl, e);
        }
    }
}