

package com.example.titan_watch_learning_project.controller;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.example.titan_watch_learning_project.serviceImpl.WebhookServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookServiceImpl webhookService;
    private final RestTemplate restTemplate = new RestTemplate();

    // ── Service Bus — optional, sirf tab inject hoga jab queue enabled ho ──
    @Autowired(required = false)
    private ServiceBusSenderClient serviceBusSenderClient;

    @Value("${webhook.forward.enabled:false}")
    private boolean forwardEnabled;

    @Value("${webhook.forward.url:}")
    private String forwardUrl;

    private final Map<String, Long> recentPayloads = new ConcurrentHashMap<>();

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Titan Webhook Service is running ✅");
    }

    @PostMapping({"", "/"})
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        log.info("Webhook hit — payload length: {}", payload == null ? 0 : payload.length());

        if (payload == null || payload.isBlank()) {
            log.warn("Empty webhook payload received");
            return ResponseEntity.badRequest().body("Empty payload");
        }

        // ── Duplicate guard ────────────────────────────────────────────────
        String payloadHash = sha256(payload);
        if (isDuplicatePayload(payloadHash)) {
            log.warn("Duplicate webhook ignored. hash={}", payloadHash);
            return ResponseEntity.ok("Duplicate ignored");
        }

        // ── Forward mode — sirf local/ngrok testing ke liye ───────────────
        if (forwardEnabled && forwardUrl != null && !forwardUrl.isBlank()) {
            CompletableFuture.runAsync(() -> forwardToLocal(payload));
            return ResponseEntity.ok("Forwarding accepted");
        }

        // ── Production mode ────────────────────────────────────────────────
        CompletableFuture.runAsync(() -> {
            try {
                processWebhook(payload);
            } catch (Exception e) {
                log.error("Async webhook processing failed", e);
            }
        });

        return ResponseEntity.ok("OK");
    }

    // ── Core routing — Queue ya Direct ────────────────────────────────────
    private void processWebhook(String payload) {

        // Service Bus queue enabled hai → queue mein daalo
        if (serviceBusSenderClient != null) {
            try {
                serviceBusSenderClient.sendMessage(new ServiceBusMessage(payload));
                log.info("Webhook queued to Service Bus ✅");
            } catch (Exception e) {
                log.error("Service Bus queue failed — falling back to direct processing", e);
                // Fallback — queue fail hone pe direct process karo
                webhookService.handleWebhookEvent(payload);
            }
        } else {
            // Queue disabled → direct process karo
            log.info("Service Bus disabled — processing directly");
            webhookService.handleWebhookEvent(payload);
        }
    }

    private boolean isDuplicatePayload(String payloadHash) {
        long now = System.currentTimeMillis();
        Long lastSeen = recentPayloads.get(payloadHash);

        if (lastSeen != null && now - lastSeen < 10_000) {
            return true;
        }

        recentPayloads.put(payloadHash, now);
        cleanOldPayloads(now);
        return false;
    }

    private void cleanOldPayloads(long now) {
        recentPayloads.entrySet().removeIf(entry -> now - entry.getValue() > 60_000);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
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