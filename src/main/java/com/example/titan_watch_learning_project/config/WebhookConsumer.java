package com.example.titan_watch_learning_project.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.example.titan_watch_learning_project.serviceImpl.WebhookServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "webhook.queue.enabled", havingValue = "true")
public class WebhookConsumer {

    private final WebhookServiceImpl webhookService;

    @Value("${spring.jms.servicebus.connection-string}")
    private String connectionString;

    // ── Dev forward ───────────────────────────────────────────
    @Value("${dev.forward.enabled:false}")
    private boolean devForwardEnabled;

    @Value("${dev.forward.url:}")
    private String devForwardUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    // ─────────────────────────────────────────────────────────

    private ServiceBusProcessorClient processorClient;

    @PostConstruct
    public void startProcessor() {
        log.info("Azure Service Bus Processor starting...");

        processorClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .queueName("titan-watch-servicebus")
                .processMessage(context -> {
                    String payload = context.getMessage().getBody().toString();
                    log.info("Message received from Service Bus");

                    // Production process karo
                    webhookService.handleWebhookEvent(payload);

                    // Dev forward — agar enabled hai
                    forwardToDev(payload);
                })
                .processError(context -> {
                    log.error("Service Bus error: {}", context.getException().getMessage());
                })
                .buildProcessorClient();

        processorClient.start();
        log.info("Azure Service Bus Processor started ✅");
    }

    private void forwardToDev(String payload) {
        if (!devForwardEnabled || devForwardUrl == null || devForwardUrl.isBlank()) return;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> req = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(devForwardUrl, req, String.class);
            log.info("Forwarded to dev ✅");
        } catch (Exception e) {
            log.warn("Dev forward failed: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stopProcessor() {
        if (processorClient != null) {
            processorClient.stop();
            processorClient.close();
            log.info("Azure Service Bus Processor stopped");
        }
    }
}