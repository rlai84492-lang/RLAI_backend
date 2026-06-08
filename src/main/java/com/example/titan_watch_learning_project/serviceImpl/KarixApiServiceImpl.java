package com.example.titan_watch_learning_project.serviceImpl;
import com.example.titan_watch_learning_project.entity.BrandCarouselCard;
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

import java.util.*;

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



    private static final String SAFE_BRAND_IMAGE =
            "https://titanwatchimages123.blob.core.windows.net/watch-images/WhatsApp%20Image%202026-06-01%20at%2023.09.58.jpeg";

    private static final Map<String, String> BRAND_IMAGE_URLS = Map.ofEntries(
            Map.entry("TITAN_EDGE", SAFE_BRAND_IMAGE),
            Map.entry("TITAN_STELLAR", SAFE_BRAND_IMAGE),
            Map.entry("TITAN_AUTOMATIC", SAFE_BRAND_IMAGE),
            Map.entry("XYLYS", SAFE_BRAND_IMAGE),
            Map.entry("TITAN_DIVERS", SAFE_BRAND_IMAGE),
            Map.entry("TITAN", SAFE_BRAND_IMAGE),
            Map.entry("TITAN_SMART", SAFE_BRAND_IMAGE),
            Map.entry("TITAN_RAGA", SAFE_BRAND_IMAGE),
            Map.entry("FASTRACK", SAFE_BRAND_IMAGE)
    );
    private static final String SAMPLE_PDF_URL =
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";


    private static final Map<String, String> BRAND_CATALOGUE_URLS = Map.ofEntries(
            Map.entry("MEN_TITAN_EDGE", SAMPLE_PDF_URL),
            Map.entry("MEN_TITAN_STELLAR", SAMPLE_PDF_URL),
            Map.entry("MEN_TITAN_AUTOMATIC", SAMPLE_PDF_URL),
            Map.entry("MEN_XYLYS", SAMPLE_PDF_URL),
            Map.entry("MEN_TITAN_DIVERS", SAMPLE_PDF_URL),
            Map.entry("MEN_TITAN", SAMPLE_PDF_URL),
            Map.entry("MEN_TITAN_SMART", SAMPLE_PDF_URL),

            Map.entry("WOMEN_TITAN_EDGE", SAMPLE_PDF_URL),
            Map.entry("WOMEN_TITAN_RAGA", SAMPLE_PDF_URL),
            Map.entry("WOMEN_TITAN_SMART", SAMPLE_PDF_URL),
            Map.entry("WOMEN_XYLYS", SAMPLE_PDF_URL),
            Map.entry("WOMEN_FASTRACK", SAMPLE_PDF_URL),
            Map.entry("WOMEN_TITAN", SAMPLE_PDF_URL)
    );
    private String getShortBrandCode(String brandKey) {
        if (brandKey == null || brandKey.isBlank()) {
            return "TTN";
        }

        return switch (brandKey) {
            case "TITAN_EDGE" -> "EDG";
            case "TITAN_STELLAR" -> "STL";
            case "TITAN_AUTOMATIC" -> "ATM";
            case "XYLYS" -> "XYL";
            case "TITAN_DIVERS" -> "DIV";
            case "TITAN_SMART" -> "SMT";
            case "TITAN_RAGA" -> "RAG";
            case "FASTRACK" -> "FST";
            case "TITAN" -> "TTN";
            default -> brandKey.length() > 3 ? brandKey.substring(0, 3) : brandKey;
        };
    }


    public boolean sendBrandCarouselMessageFromDbCards(
            String phone,
            String firstName,
            String gender,
            List<BrandCarouselCard> dbCards
    ) {
        if (dbCards == null || dbCards.size() < 2) {
            log.warn("Brand carousel requires at least 2 DB cards. Found={}",
                    dbCards == null ? 0 : dbCards.size());
            return false;
        }

        try {
            List<Map<String, Object>> cards = new ArrayList<>();

            for (int i = 0; i < Math.min(dbCards.size(), 10); i++) {
                BrandCarouselCard dbCard = dbCards.get(i);

                String brandKey = dbCard.getBrandKey() == null || dbCard.getBrandKey().isBlank()
                        ? "TITAN"
                        : dbCard.getBrandKey().trim().toUpperCase();

                String title = dbCard.getTitle() == null || dbCard.getTitle().isBlank()
                        ? brandKey.replace("_", " ")
                        : dbCard.getTitle().trim();

                String description = dbCard.getDescription() == null || dbCard.getDescription().isBlank()
                        ? ""
                        : dbCard.getDescription().trim();

                String imageUrl = dbCard.getImageUrl() == null || dbCard.getImageUrl().isBlank()
                        ? ""
                        : dbCard.getImageUrl().trim();

                if (imageUrl.isBlank()) {
                    log.warn("Skipping brand carousel card id={} brandKey={} because imageUrl is blank",
                            dbCard.getId(), brandKey);
                    continue;
                }

                String bodyText = title + (description.isBlank() ? "" : "\n" + description);

                if (bodyText.length() > 120) {
                    bodyText = bodyText.substring(0, 117) + "...";
                }

                Map<String, Object> image = new LinkedHashMap<>();
                image.put("link", imageUrl);

                Map<String, Object> header = new LinkedHashMap<>();
                header.put("type", "image");
                header.put("image", image);

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("text", bodyText);

                String genderCode = "MEN".equalsIgnoreCase(gender) ? "M" : "W";
                String brandCode = getShortBrandCode(brandKey);

                String explorePayload = "EX_" + genderCode + "_" + brandCode + "_" + cards.size();
                String callbackPayload = "CB_" + genderCode + "_" + brandCode + "_" + cards.size();

                Map<String, Object> exploreReply = new LinkedHashMap<>();
                exploreReply.put("id", explorePayload);
                exploreReply.put("title", "Explore collection");

                Map<String, Object> exploreButton = new LinkedHashMap<>();
                exploreButton.put("type", "quick_reply");
                exploreButton.put("quick_reply", exploreReply);

                Map<String, Object> callbackReply = new LinkedHashMap<>();
                callbackReply.put("id", callbackPayload);
                callbackReply.put("title", "Request callback");

                Map<String, Object> callbackButton = new LinkedHashMap<>();
                callbackButton.put("type", "quick_reply");
                callbackButton.put("quick_reply", callbackReply);

                Map<String, Object> action = new LinkedHashMap<>();
                action.put("buttons", List.of(exploreButton, callbackButton));

                Map<String, Object> card = new LinkedHashMap<>();
                card.put("card_index", cards.size());
                card.put("type", "cta_url");
                card.put("header", header);
                card.put("body", body);
                card.put("action", action);

                cards.add(card);
            }

            if (cards.size() < 2) {
                log.warn("Brand carousel has less than 2 valid DB cards. Valid cards={}", cards.size());
                return false;
            }

            Map<String, Object> carouselBody = new LinkedHashMap<>();
            carouselBody.put(
                    "text",
                    "✨ *Exclusive Collections for You*\n\n"
                            + "Hi *" + firstName + "*,\n\n"
                            + "Here are our collections for you.\n\n"
                            + "👉 *Tap any brand to explore*\n"
                            + "💬 Or tell us if you'd like help choosing!"
            );

            Map<String, Object> carouselAction = new LinkedHashMap<>();
            carouselAction.put("cards", cards);

            Map<String, Object> interactive = new LinkedHashMap<>();
            interactive.put("type", "carousel");
            interactive.put("body", carouselBody);
            interactive.put("action", carouselAction);

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("type", "INTERACTIVE");
            content.put("preview_url", false);
            content.put("shorten_url", false);
            content.put("interactive", interactive);

            Map<String, Object> reference = new LinkedHashMap<>();
            reference.put("cust_ref", "titan_brand_carousel_" + System.currentTimeMillis());
            reference.put("messageTag1", "Titan Brand Carousel");
            reference.put("conversationId", "titan_brand_" + phone + "_" + System.currentTimeMillis());

            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put("to", phone);
            recipient.put("recipient_type", "individual");
            recipient.put("reference", reference);

            Map<String, Object> sender = new LinkedHashMap<>();
            sender.put("from", wabaNumber);

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

            return post(requestBody, phone, "BRAND_CAROUSEL_DB_" + gender);

        } catch (Exception e) {
            log.error("Failed to send DB brand carousel phone={} gender={}", phone, gender, e);
            return false;
        }
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
        Map<String, Object> body = buildListPayload(
                toPhone,
                bodyText,
                "View Options",
                "Choose an option",
                options
        );
        post(body, toPhone, "LIST_MESSAGE");
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
            String buttonText,
            String sectionTitle,
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
                    row.put("description", option.getOrDefault("description", "Tap to select"));
                    return row;
                })
                .toList();

        Map<String, Object> section = new LinkedHashMap<>();
        section.put("title", sectionTitle);
        section.put("rows", rows);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("button", buttonText);
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


//    public boolean sendDocumentMessage(String toPhone, String documentUrl, String caption, String fileName) {
//        Map<String, Object> reference = new LinkedHashMap<>();
//        reference.put("cust_ref", "titan_doc_" + System.currentTimeMillis());
//
//        Map<String, Object> recipient = new LinkedHashMap<>();
//        recipient.put("to", toPhone);
//        recipient.put("recipient_type", "individual");
//        recipient.put("reference", reference);
//
//        Map<String, Object> sender = new LinkedHashMap<>();
//        sender.put("from", wabaNumber);
//
//        Map<String, Object> media = new LinkedHashMap<>();
//        media.put("link", documentUrl);
//        media.put("filename", fileName);
//
//        Map<String, Object> attachment = new LinkedHashMap<>();
//        attachment.put("type", "document");
//        attachment.put("media", media);
//        attachment.put("caption", caption);
//
//        Map<String, Object> content = new LinkedHashMap<>();
//        content.put("type", "ATTACHMENT");
//        content.put("attachment", attachment);
//
//        Map<String, Object> message = new LinkedHashMap<>();
//        message.put("channel", "WABA");
//        message.put("recipient", recipient);
//        message.put("sender", sender);
//        message.put("content", content);
//
//        Map<String, Object> metaData = new LinkedHashMap<>();
//        metaData.put("version", "v1.0.9");
//
//        Map<String, Object> requestBody = new LinkedHashMap<>();
//        requestBody.put("message", message);
//        requestBody.put("metaData", metaData);
//
//        return post(requestBody, toPhone, "DOCUMENT_MESSAGE");
//    }
    /**
     * Step 1: PDF/Image ko Karix pe upload karo
     * Returns imageId jo Step 2 mein use hoga
     */
    public String uploadMedia(String mediaUrl, String mimeType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authentication", "Bearer " + karixApiKey);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mediaUrl", mediaUrl);
            body.put("senderId", wabaNumber);
            body.put("MimeType", mimeType);

            String json = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            log.info("Uploading media url={} mimeType={}", mediaUrl, mimeType);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://rcmapi.instaalerts.zone/services/media/upload/attachment",
                    request,
                    String.class
            );

            log.info("Karix media upload response={}", response.getBody());

            JsonNode root = objectMapper.readTree(response.getBody());

            // ✅ Correct path: response.mediaId
            String mediaId = root.path("response").path("mediaId").asText("");

            if (!mediaId.isBlank()) {
                log.info("Media uploaded successfully mediaId={}", mediaId);
                return mediaId;
            }

            log.warn("Media upload failed — no mediaId in response={}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("Media upload error url={} error={}", mediaUrl, e.getMessage());
            return null;
        }
    }

    public boolean sendDocumentMessage(
            String phone,
            String documentUrl,
            String caption,
            String filename
    ) {
        try {
            String encodedUrl = documentUrl.replace(" ", "%20");

            Map<String, Object> reference = new LinkedHashMap<>();
            reference.put("cust_ref", "titan_doc_" + System.currentTimeMillis());

            Map<String, Object> recipient = new LinkedHashMap<>();
            recipient.put("to", phone);
            recipient.put("recipient_type", "individual");
            recipient.put("reference", reference);

            Map<String, Object> sender = new LinkedHashMap<>();
            sender.put("from", wabaNumber);

            // ── CORRECT FORMAT from Karix docs ──────────────────
            Map<String, Object> attachment = new LinkedHashMap<>();
            attachment.put("type", "document");
            attachment.put("url", encodedUrl);          // ← url, not link
            attachment.put("fileName", filename.trim()); // ← fileName, not filename

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("type", "ATTACHMENT");
            content.put("attachment", attachment);
            // ────────────────────────────────────────────────────

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

            log.info("Sending PDF document phone={} url={}", phone, encodedUrl);
            return post(requestBody, phone, "DOCUMENT_ATTACHMENT");

        } catch (Exception e) {
            log.error("sendDocumentMessage error phone={}", phone, e);
            return false;
        }
    }
    public String getCatalogueUrl(String gender, String brandKey) {
        String key = gender + "_" + brandKey;
        return BRAND_CATALOGUE_URLS.getOrDefault(key, "https://www.titan.co.in");
    }}

