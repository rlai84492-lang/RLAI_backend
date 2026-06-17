package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.service.WebhookForwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookForwardServiceImpl implements WebhookForwardService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${webhook.forward.enabled:false}")
    private boolean forwardEnabled;

    @Value("${webhook.forward.url:}")
    private String forwardUrl;

    @Override
    public boolean forward(String payload) {
        if (!forwardEnabled || forwardUrl == null || forwardUrl.isBlank()) {
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Forwarded-From", "render-webhook");

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    forwardUrl,
                    request,
                    String.class
            );

            log.info("Webhook forwarded to {} with status {}",
                    forwardUrl,
                    response.getStatusCode());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Failed to forward webhook to {}: {}", forwardUrl, e.getMessage());
            return false;
        }
    }
}