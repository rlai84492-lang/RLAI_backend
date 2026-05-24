
package com.example.titan_watch_learning_project.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KarixApiServiceImpl {

    private final ObjectMapper objectMapper;

    @Value("${karix.api.url}")
    private String karixApiUrl;

    @Value("${karix.api.key}")
    private String karixApiKey;

    @Value("${karix.waba.number}")
    private String wabaNumber;

    @Value("${karix.auth.header-name:Authentication}")
    private String authHeaderName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendWelcomeMessage(String toPhone) {
        String text = "Welcome to Titan! How can we help you today?";

        Map<String, Object> body = buildTextPayload(toPhone, text);

        post(body, toPhone, "WELCOME");
    }

    public void sendTextMessage(String toPhone, String text) {
        Map<String, Object> body = buildTextPayload(toPhone, text);
        post(body, toPhone, "TEXT");
    }

    private Map<String, Object> buildTextPayload(String toPhone, String text) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("preview_url", false);
        content.put("text", text);
        content.put("type", "TEXT");

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("content", content);
        message.put("recipient", recipient);
        message.put("sender", sender);

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("version", "v1.0.9");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);
        requestBody.put("metaData", metaData);

        return requestBody;
    }

    private boolean post(Map<String, Object> body, String toPhone, String stepName) {
        try {
            if (karixApiKey == null || karixApiKey.isBlank()) {
                log.error("Karix API key is blank. Please set KARIX_API_KEY env variable.");
                return false;
            }

//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set(authHeaderName, karixApiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authentication", "Bearer " + karixApiKey);

            String json = objectMapper.writeValueAsString(body);

            log.info("Karix → sending to={} step={} url={} body={}",
                    toPhone, stepName, karixApiUrl, json);

            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(karixApiUrl, request, String.class);

            String responseBody = response.getBody();

            log.info("Karix API response status={} body={}",
                    response.getStatusCode(), responseBody);

            if (responseBody == null || responseBody.isBlank()) {
                return false;
            }

            JsonNode root = objectMapper.readTree(responseBody);
            String statusCode = root.path("statusCode").asText("");

            return "200".equals(statusCode);

        } catch (Exception e) {
            log.error("Karix API error while sending to={} step={} url={}",
                    toPhone, stepName, karixApiUrl, e);
            return false;
        }
    }
    private Map<String, Object> buildBirthdayOptionsPayload(String toPhone, String text) {

        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        Map<String, Object> bodyText = new LinkedHashMap<>();
        bodyText.put("text", text);

        Map<String, Object> button1Reply = new LinkedHashMap<>();
        button1Reply.put("id", "FIND_BIRTHDAY_WATCH");
        button1Reply.put("title", "Find Birthday Watch");

        Map<String, Object> button1 = new LinkedHashMap<>();
        button1.put("type", "reply");
        button1.put("reply", button1Reply);

        Map<String, Object> button2Reply = new LinkedHashMap<>();
        button2Reply.put("id", "BIRTHDAY_OFFERS");
        button2Reply.put("title", "Birthday Offers");

        Map<String, Object> button2 = new LinkedHashMap<>();
        button2.put("type", "reply");
        button2.put("reply", button2Reply);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("buttons", List.of(button1, button2));

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "button");
        interactive.put("body", bodyText);
        interactive.put("action", action);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "INTERACTIVE");
        content.put("interactive", interactive);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("content", content);
        message.put("recipient", recipient);
        message.put("sender", sender);

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("version", "v1.0.9");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);
        requestBody.put("metaData", metaData);

        return requestBody;
    }

    public void sendBirthdayOptionsMessage(String toPhone, String customerName) {

        String text = "Dear " + customerName + ",\n"
                + "Your birthday is just 10 days away! 🎂\n\n"
                + "At Titan, we would love to celebrate your special occasion with timeless watches crafted for every moment.\n\n"
                + "Choose what you would like to explore today:";

        Map<String, Object> body = buildBirthdayOptionsPayload(toPhone, text);





        post(body, toPhone, "BIRTHDAY_OPTIONS");
    }

    public void sendButtonMessage(String toPhone, String bodyText, List<Map<String, String>> buttons) {
        Map<String, Object> body = buildInteractiveButtonPayload(toPhone, null, bodyText, buttons);
        post(body, toPhone, "BUTTON_MESSAGE");
    }

    public boolean sendImageButtonMessage(
            String toPhone,
            String imageUrl,
            String bodyMessage,
            List<Map<String, String>> buttons
    ) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        Map<String, Object> image = new LinkedHashMap<>();
        image.put("link", imageUrl);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("type", "image");
        header.put("image", image);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", bodyMessage);

        List<Map<String, Object>> whatsappButtons = new ArrayList<>();

        for (Map<String, String> btn : buttons) {
            Map<String, Object> reply = new LinkedHashMap<>();
            reply.put("id", btn.get("payload"));
            reply.put("title", btn.get("title"));

            Map<String, Object> button = new LinkedHashMap<>();
            button.put("type", "reply");
            button.put("reply", reply);

            whatsappButtons.add(button);
        }

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("buttons", whatsappButtons);

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "button");
        interactive.put("header", header);
        interactive.put("body", body);
        interactive.put("action", action);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "INTERACTIVE");
        content.put("interactive", interactive);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("recipient", recipient);
        message.put("sender", sender);
        message.put("content", content);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);

        // ✅ YEH ADD KARO - YAHI MISSING THA!
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("version", "v1.0.9");
        requestBody.put("metaData", metaData);

        return post(requestBody, toPhone, "IMAGE_BUTTON_MESSAGE");
    }


    public void sendListMessage(String toPhone, String bodyText, List<Map<String, String>> options) {
        Map<String, Object> body = buildListPayload(toPhone, bodyText, options);
        post(body, toPhone, "LIST_MESSAGE");
    }

    public void sendCollectionSelectionMessage(String toPhone) {
        String text = "Celebrate your special day with a watch that matches your personality. ✨\n\n"
                + "Choose a collection:";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Men’s Collection", "payload", "MENS_COLLECTION"),
                Map.of("title", "Women’s Collection", "payload", "WOMENS_COLLECTION")
        );

        sendButtonMessage(toPhone, text, buttons);
    }

    public void sendStyleSelectionMessage(String toPhone, String collectionName) {
        String text =
                "✨ *Style is a reflection of your soul.*\n\n"
                        + "Tell us — which aesthetic defines you?\n\n"
                        + "_Choose your vibe for " + collectionName + ":_";

        List<Map<String, String>> options = List.of(
                Map.of("title", "🤍 Minimal & Chic", "payload", "STYLE_MINIMAL_CHIC"),
                Map.of("title", "🖤 Bold & Edgy", "payload", "STYLE_BOLD_EDGY"),
                Map.of("title", "💛 Luxe & Classy", "payload", "STYLE_LUXE_CLASSY"),
                Map.of("title", "🏆 Sporty & Adventurous", "payload", "STYLE_SPORTY_ADVENTUROUS")
        );

        sendListMessage(toPhone, text, options);
    }
    public void sendDemoStyleProductMessage(String toPhone, String gender, String style) {
        String imageUrl = getDemoImageUrl(gender, style);

        String bodyText = "Here are some " + gender + " watches for your selected style: " + style + ". ⌚\n\n"
                + "This is a demo recommendation card. Later we can connect this with real Titan products, price filters, and catalogue.";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Get Callback", "payload", "REQUEST_CALLBACK"),
                Map.of("title", "See by Price", "payload", "SEE_BY_PRICE"),
                Map.of("title", "Full Catalogue", "payload", "DOWNLOAD_CATALOGUE")
        );

        sendImageButtonMessage(toPhone, imageUrl, bodyText, buttons);
    }
    private String getDemoImageUrl(String gender, String style) {
        // Demo hardcoded images. Later replace with Titan/Cloudinary/S3 image URLs.
        if ("MEN".equalsIgnoreCase(gender)) {
            if ("Minimal & Chic".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-men-minimal/800/400";
            }
            if ("Bold & Edgy".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-men-bold/800/400";
            }
            if ("Luxe & Classy".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-men-luxe/800/400";
            }
            if ("Sporty & Adventurous".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-men-sporty/800/400";
            }
        }

        if ("WOMEN".equalsIgnoreCase(gender)) {
            if ("Minimal & Chic".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-women-minimal/800/400";
            }
            if ("Bold & Edgy".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-women-bold/800/400";
            }
            if ("Luxe & Classy".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-women-luxe/800/400";
            }
            if ("Sporty & Adventurous".equalsIgnoreCase(style)) {
                return "https://picsum.photos/seed/titan-women-sporty/800/400";
            }
        }

        return "https://picsum.photos/seed/titan-watch-demo/800/400";
    }

    private Map<String, Object> buildInteractiveButtonPayload(
            String toPhone,
            String imageUrl,
            String bodyText,
            List<Map<String, String>> buttons
    ) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "button");

        if (imageUrl != null && !imageUrl.isBlank()) {
            Map<String, Object> image = new LinkedHashMap<>();
            image.put("link", imageUrl);

            Map<String, Object> header = new LinkedHashMap<>();
            header.put("type", "image");
            header.put("image", image);

            interactive.put("header", header);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", bodyText);
        interactive.put("body", body);

        List<Map<String, Object>> buttonList = buttons.stream()
                .limit(3)
                .map(btn -> {
                    Map<String, Object> reply = new LinkedHashMap<>();
                    reply.put("id", btn.get("payload"));
                    reply.put("title", btn.get("title"));

                    Map<String, Object> button = new LinkedHashMap<>();
                    button.put("type", "reply");
                    button.put("reply", reply);

                    return button;
                })
                .toList();

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("buttons", buttonList);

        interactive.put("action", action);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "INTERACTIVE");
        content.put("interactive", interactive);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("recipient", recipient);
        message.put("sender", sender);
        message.put("content", content);

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("version", "v1.0.9");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);
        requestBody.put("metaData", metaData);

        return requestBody;
    }

    private Map<String, Object> buildListPayload(
            String toPhone,
            String bodyText,
            List<Map<String, String>> options
    ) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        List<Map<String, Object>> rows = options.stream()
                .map(option -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", option.get("payload"));
                    row.put("title", option.get("title"));
                    row.put("description", "Tap to select");
                    return row;
                })
                .toList();

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("title", "Choose your style");
        section.put("rows", rows);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("button", "View Styles");
        action.put("sections", List.of(section));

        Map<String, Object> interactive = new LinkedHashMap<>();
        interactive.put("type", "list");
        interactive.put("body", Map.of("text", bodyText));
        interactive.put("action", action);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "INTERACTIVE");
        content.put("interactive", interactive);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("recipient", recipient);
        message.put("sender", sender);
        message.put("content", content);

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("version", "v1.0.9");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);
        requestBody.put("metaData", metaData);

        return requestBody;
    }





    public boolean sendImageMessage(String toPhone, String imageUrl, String caption) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        Map<String, Object> media = new LinkedHashMap<>();
        media.put("link", imageUrl);

        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("type", "image");
        attachment.put("media", media);
        attachment.put("caption", caption);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "ATTACHMENT");
        content.put("attachment", attachment);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("channel", "WABA");
        message.put("recipient", recipient);
        message.put("sender", sender);
        message.put("content", content);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);

        return post(requestBody, toPhone, "PRODUCT_IMAGE");
    }
}