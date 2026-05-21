package com.example.titan_watch_learning_project.serviceImpl;//package com.example.titan.serviceImpl;
//
//import com.example.titan.service.KarixApiService;
import com.example.titan_watch_learning_project.service.KarixApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

//    @Value("${karix.webhook.dnid}")
//    private String webhookDnId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ══════════════════════════════════════════════════════
    // 1. Plain text message
    // ══════════════════════════════════════════════════════
    @Override
    public String sendTextMessage(String toPhone, String text) {
        Map<String, Object> body = buildBase(toPhone);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "TEXT");
        content.put("preview_url", false);
        content.put("text", text);
        msg(body).put("content", content);
        return post(body);
    }

    // ══════════════════════════════════════════════════════
    // 2. Quick-reply button message (max 3 buttons, no image)
    // ══════════════════════════════════════════════════════
    @Override
    public String sendButtonMessage(String toPhone, String bodyText, List<Map<String, String>> buttons) {
        Map<String, Object> body = buildBase(toPhone);
        msg(body).put("content", buildInteractiveButton(null, bodyText, buttons));
        return post(body);
    }

    // ══════════════════════════════════════════════════════
    // 3. Image + button message (Tanishq style)
    // ══════════════════════════════════════════════════════
    @Override
    public String sendImageButtonMessage(String toPhone, String imageUrl, String bodyText, List<Map<String, String>> buttons) {
        Map<String, Object> body = buildBase(toPhone);
        msg(body).put("content", buildInteractiveButton(imageUrl, bodyText, buttons));
        return post(body);
    }

    // ══════════════════════════════════════════════════════
    // 4. Carousel / list message — for style selection, product carousel
    // ══════════════════════════════════════════════════════
    @Override
    public String sendCarouselCards(String toPhone, String bodyText, List<Map<String, Object>> cards) {
        Map<String, Object> body = buildBase(toPhone);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> card : cards) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",          String.valueOf(card.get("id")));
            row.put("title",       String.valueOf(card.get("name")));
            String price = card.get("price") != null && !card.get("price").toString().isEmpty()
                    ? "₹" + card.get("price") : "";
            row.put("description", price);
            rows.add(row);
        }

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("title", "Select an option");
        section.put("rows",  rows);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("button",   "View options");
        action.put("sections", List.of(section));

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type",   "list");
        interactive.put("body",   Map.of("text", bodyText));
        interactive.put("action", action);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type",        "INTERACTIVE");
        content.put("interactive", interactive);
        msg(body).put("content", content);
        return post(body);
    }

    // ══════════════════════════════════════════════════════
    // 5. List message (same as carousel but generic)
    // ══════════════════════════════════════════════════════
    @Override
    public String sendListMessage(String toPhone, String bodyText, List<Map<String, Object>> options) {
        return sendCarouselCards(toPhone, bodyText, options);
    }

    // ══════════════════════════════════════════════════════
    // PRIVATE — interactive button builder
    // ══════════════════════════════════════════════════════
    private Map<String, Object> buildInteractiveButton(String imageUrl, String bodyText, List<Map<String, String>> buttons) {
        List<Map<String, Object>> btnList = new ArrayList<>();
        int limit = Math.min(buttons.size(), 3);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> btn = new LinkedHashMap<>();
            btn.put("type", "reply");
            btn.put("reply", Map.of(
                    "id",    buttons.get(i).get("payload"),   // simple payload — no BTN_0_ prefix
                    "title", buttons.get(i).get("title")
            ));
            btnList.add(btn);
        }

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "button");

        if (imageUrl != null && !imageUrl.isBlank()) {
            interactive.put("header", Map.of(
                    "type",  "image",
                    "image", Map.of("link", imageUrl)
            ));
        }

        interactive.put("body",   Map.of("text", bodyText));
        interactive.put("action", Map.of("buttons", btnList));

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type",        "INTERACTIVE");
        content.put("interactive", interactive);
        return content;
    }

    // ══════════════════════════════════════════════════════
    // PRIVATE — base message wrapper
    // ══════════════════════════════════════════════════════
    private Map<String, Object> buildBase(String toPhone) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("recipient", Map.of(
                "to",             toPhone,
                "recipient_type", "individual",
                "reference",      Map.of("cust_ref", "titan_" + System.currentTimeMillis())
        ));
        message.put("sender",      Map.of("from", wabaNumber));
//        message.put("preferences", Map.of("webHookDNId", webhookDnId));
        root.put("message",  message);
        root.put("metaData", Map.of("version", "v1.0.9"));
        return root;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> msg(Map<String, Object> root) {
        return (Map<String, Object>) root.get("message");
    }

    // ══════════════════════════════════════════════════════
    // PRIVATE — HTTP POST
    // ══════════════════════════════════════════════════════
    private String post(Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String json = objectMapper.writeValueAsString(body);
            log.info("Karix → sending: {}", json.substring(0, Math.min(json.length(), 300)));

            ResponseEntity<String> res = restTemplate.postForEntity(
                    apiUrl, new HttpEntity<>(json, headers), String.class);
            log.info("Karix ← response: {}", res.getBody());
            return res.getBody();
        } catch (Exception e) {
            log.error("Karix API error: {}", e.getMessage(), e);
            return null;
        }
    }
}