package com.example.titan_watch_learning_project.serviceImpl;//package com.example.titan.serviceImpl;
//
//import com.example.titan.service.KarixApiService;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.titan_watch_learning_project.service.KarixApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KarixApiServiceImpl implements KarixApiService {

    @Value("${karix.api.url}")
    private String apiUrl;

    @Value("${karix.api.key}")
    private String apiKey;

    @Value("${karix.waba.number}")
    private String wabaNumber;

    @Value("${karix.webhook.dnid}")
    private String webhookDnId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // =============================================
    // 1. Send Simple Text Message
    // =============================================
    @Override
    public String sendTextMessage(String toPhone, String text) {
        Map<String, Object> body = buildBaseMessage(toPhone);
        Map<String, Object> content = new HashMap<>();
        content.put("type", "TEXT");
        content.put("preview_url", false);
        content.put("text", text);
        getMessageMap(body).put("content", content);
        return sendToKarix(body);
    }

    // =============================================
    // 2. Send Button Message (Quick Reply — max 3 buttons on WhatsApp)
    // =============================================
    @Override
    public String sendButtonMessage(String toPhone, String bodyText, List<Map<String, String>> buttons) {
        Map<String, Object> body = buildBaseMessage(toPhone);

        List<Map<String, Object>> btnList = new ArrayList<>();
        for (int i = 0; i < Math.min(buttons.size(), 3); i++) {
            Map<String, Object> btn = new HashMap<>();
            btn.put("type", "reply");
            Map<String, String> reply = new HashMap<>();
            reply.put("id", "BTN_" + i + "_" + buttons.get(i).get("payload"));
            reply.put("title", buttons.get(i).get("title"));
            btn.put("reply", reply);
            btnList.add(btn);
        }

        Map<String, Object> interactive = new HashMap<>();
        interactive.put("type", "button");
        interactive.put("body", Map.of("text", bodyText));
        interactive.put("action", Map.of("buttons", btnList));

        Map<String, Object> content = new HashMap<>();
        content.put("type", "INTERACTIVE");
        content.put("interactive", interactive);
        getMessageMap(body).put("content", content);
        return sendToKarix(body);
    }

    // =============================================
    // 3. Send List / Carousel Message
    // =============================================
    @Override
    public String sendCarouselMessage(String toPhone, String headerText, List<Map<String, Object>> products) {
        Map<String, Object> body = buildBaseMessage(toPhone);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> p : products) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.get("id").toString());
            row.put("title", p.get("name").toString());
            Object price = p.get("price");
            row.put("description", price != null && !price.toString().isEmpty()
                    ? "₹" + price : "");
            rows.add(row);
        }

        Map<String, Object> section = new HashMap<>();
        section.put("title", "Available Watches");
        section.put("rows", rows);

        Map<String, Object> action = new HashMap<>();
        action.put("button", "Select Watch");
        action.put("sections", List.of(section));

        Map<String, Object> interactive = new HashMap<>();
        interactive.put("type", "list");
        interactive.put("header", Map.of("type", "text", "text", headerText));
        interactive.put("body", Map.of("text", "Choose a watch that matches your style"));
        interactive.put("action", action);

        Map<String, Object> content = new HashMap<>();
        content.put("type", "INTERACTIVE");
        content.put("interactive", interactive);
        getMessageMap(body).put("content", content);
        return sendToKarix(body);
    }

    // =============================================
    // Base Message Builder
    // =============================================
    private Map<String, Object> buildBaseMessage(String toPhone) {
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        message.put("channel", "WABA");
        message.put("recipient", Map.of(
                "to", toPhone,
                "recipient_type", "individual",
                "reference", Map.of("cust_ref", "titan_" + System.currentTimeMillis())
        ));
        message.put("sender", Map.of("from", wabaNumber));
        message.put("preferences", Map.of("webHookDNId", webhookDnId));
        root.put("message", message);
        root.put("metaData", Map.of("version", "v1.0.9"));
        return root;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMessageMap(Map<String, Object> root) {
        return (Map<String, Object>) root.get("message");
    }

    // =============================================
    // HTTP Call to Karix
    // =============================================
    private String sendToKarix(Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Postman jaisa exact header
            headers.set("Authentication", "Bearer " + apiKey);

            String jsonBody = objectMapper.writeValueAsString(body);

            log.info("========== KARIX REQUEST START ==========");
            log.info("Karix URL: {}", apiUrl);
            log.info("Karix Header Authentication: Bearer {}", apiKey);
            log.info("Karix Request Body: {}", jsonBody);
            log.info("========== KARIX REQUEST END ==========");

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, request, String.class);

            log.info("========== KARIX RESPONSE START ==========");
            log.info("Karix Response Status: {}", response.getStatusCode());
            log.info("Karix Response Body: {}", response.getBody());
            log.info("========== KARIX RESPONSE END ==========");

            return response.getBody();

        } catch (Exception e) {
            log.error("========== KARIX ERROR START ==========");
            log.error("Karix API error message: {}", e.getMessage());
            log.error("Karix API full error: ", e);
            log.error("========== KARIX ERROR END ==========");
            throw new RuntimeException("Failed to send message via Karix: " + e.getMessage());
        }
    }}