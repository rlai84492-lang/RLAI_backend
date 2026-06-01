
package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.WatchProduct;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.titan_watch_learning_project.entity.WatchProduct;

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


    private static final String STORE_LOCATOR_URL = "https://www.titan.co.in/store-locator";

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


    public boolean sendBrandCarouselMessage(
            String phone,
            String firstName,
            String gender,
            List<Map<String, String>> brands
    ) {
        if (brands == null || brands.size() < 2) {
            log.warn("Brand carousel requires at least 2 brands. Found={}", brands == null ? 0 : brands.size());
            return false;
        }

        try {
            List<Map<String, Object>> cards = new ArrayList<>();

            for (int i = 0; i < Math.min(brands.size(), 10); i++) {
                Map<String, String> brand = brands.get(i);

                String brandKey = brand.get("key");
                String brandName = brand.get("name");

                String imageUrl = BRAND_IMAGE_URLS.getOrDefault(
                        brandKey,
                        BRAND_IMAGE_URLS.getOrDefault("TITAN", "")
                );

                Map<String, Object> image = new LinkedHashMap<>();
                image.put("link", imageUrl);

                Map<String, Object> header = new LinkedHashMap<>();
                header.put("type", "image");
                header.put("image", image);

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("text", brandName);

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
                log.warn("Brand carousel has less than 2 valid cards.");
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

            return post(requestBody, phone, "BRAND_CAROUSEL_" + gender);

        } catch (Exception e) {
            log.error("Failed to send brand carousel phone={} gender={}", phone, gender, e);
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


//    public void sendListMessage(String toPhone, String bodyText, List<Map<String, String>> options) {
//        Map<String, Object> body = buildListPayload(toPhone, bodyText, options);
//        post(body, toPhone, "LIST_MESSAGE");
//    }

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


    public void sendListMessage(
            String toPhone,
            String bodyText,
            String buttonText,
            String sectionTitle,
            List<Map<String, String>> options
    ) {
        Map<String, Object> body = buildListPayload(
                toPhone,
                bodyText,
                buttonText,
                sectionTitle,
                options
        );
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

//    private Map<String, Object> buildListPayload(
//            String toPhone,
//            String bodyText,
//            List<Map<String, String>> options
//    ) {
//        Map<String, Object> reference = new LinkedHashMap<>();
//        reference.put("cust_ref", "titan_" + System.currentTimeMillis());
//
//        Map<String, Object> recipient = new LinkedHashMap<>();
//        recipient.put("to", toPhone);
//        recipient.put("recipient_type", "individual");
//        recipient.put("reference", reference);
//
//        Map<String, Object> sender = new LinkedHashMap<>();
//        sender.put("from", wabaNumber);
//
//        List<Map<String, Object>> rows = options.stream()
//                .map(option -> {
//                    Map<String, Object> row = new LinkedHashMap<>();
//                    row.put("id", option.get("payload"));
//                    row.put("title", option.get("title"));
//                    row.put("description", "Tap to select");
//                    return row;
//                })
//                .toList();
//
//        Map<String, Object> section = new LinkedHashMap<>();
//        section.put("title", "Choose your style");
//        section.put("rows", rows);
//
//        Map<String, Object> action = new LinkedHashMap<>();
//        action.put("button", "View Styles");
//        action.put("sections", List.of(section));
//
//        Map<String, Object> interactive = new LinkedHashMap<>();
//        interactive.put("type", "list");
//        interactive.put("body", Map.of("text", bodyText));
//        interactive.put("action", action);
//
//        Map<String, Object> content = new LinkedHashMap<>();
//        content.put("type", "INTERACTIVE");
//        content.put("interactive", interactive);
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
//        return requestBody;
//    }




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




    public boolean sendDocumentMessage(String toPhone, String documentUrl, String caption, String fileName) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("cust_ref", "titan_doc_" + System.currentTimeMillis());

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("to", toPhone);
        recipient.put("recipient_type", "individual");
        recipient.put("reference", reference);

        Map<String, Object> sender = new LinkedHashMap<>();
        sender.put("from", wabaNumber);

        Map<String, Object> media = new LinkedHashMap<>();
        media.put("link", documentUrl);
        media.put("filename", fileName);

        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("type", "document");
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

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("version", "v1.0.9");

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("message", message);
        requestBody.put("metaData", metaData);

        return post(requestBody, toPhone, "DOCUMENT_MESSAGE");
    }
    public String getCatalogueUrl(String gender, String brandKey) {
        String key = gender + "_" + brandKey;
        return BRAND_CATALOGUE_URLS.getOrDefault(key, "https://www.titan.co.in");
    }


    public boolean sendCarouselMessage(String phone, List<WatchProduct> products) {
        if (products == null || products.size() < 2) {
            log.warn("Carousel requires at least 2 products. Found={}", products == null ? 0 : products.size());
            return false;
        }

        try {
            List<Map<String, Object>> cards = new ArrayList<>();

            int maxCards = Math.min(products.size(), 6);

            for (int i = 0; i < maxCards; i++) {
                WatchProduct product = products.get(i);

                String brand = product.getBrand() == null || product.getBrand().isBlank()
                        ? "TITAN"
                        : product.getBrand().trim().toUpperCase();

                String imageUrl = product.getImageUrl() == null || product.getImageUrl().isBlank()
                        ? ""
                        : product.getImageUrl().trim();

                if (imageUrl.isBlank()) {
                    log.warn("Skipping product id={} because imageUrl is blank for carousel", product.getId());
                    continue;
                }

                String priceText = product.getPrice() == null
                        ? ""
                        : "₹" + String.format("%.0f", product.getPrice());

                String styleText = product.getStyle() == null || product.getStyle().isBlank()
                        ? ""
                        : product.getStyle().trim();

                String bodyText =
                        brand + "\n"
                                + (priceText.isBlank() ? "" : priceText + "\n")
                                + (styleText.isBlank() ? "" : styleText);

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

//                Map<String, Object> interestedQuickReply = new LinkedHashMap<>();
//                interestedQuickReply.put("id", "REQUEST_CALLBACK_" + cards.size());
//                interestedQuickReply.put("title", "Interested?");
//
//                Map<String, Object> interestedButton = new LinkedHashMap<>();
//                interestedButton.put("type", "quick_reply");
//                interestedButton.put("quick_reply", interestedQuickReply);
//
//                Map<String, Object> storeQuickReply = new LinkedHashMap<>();
//                storeQuickReply.put("id", "BOOK_STORE_VISIT_" + cards.size());
//                storeQuickReply.put("title", "Nearby Store");
//
//                Map<String, Object> storeButton = new LinkedHashMap<>();
//                storeButton.put("type", "quick_reply");
//                storeButton.put("quick_reply", storeQuickReply);
//
//                Map<String, Object> action = new LinkedHashMap<>();
//                action.put("buttons", List.of(interestedButton, storeButton));


                String productUrl = product.getProductUrl() == null || product.getProductUrl().isBlank()
                        ? "https://www.titan.co.in"
                        : product.getProductUrl().trim();

                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("display_text", "View Product");
                parameters.put("url", productUrl);

                Map<String, Object> action = new LinkedHashMap<>();
                action.put("name", "cta_url");
                action.put("parameters", parameters);

                Map<String, Object> card = new LinkedHashMap<>();
                card.put("card_index", cards.size());
                card.put("type", "cta_url");
                card.put("header", header);
                card.put("body", body);
                card.put("action", action);

                cards.add(card);
            }

            if (cards.size() < 2) {
                log.warn("Carousel requires at least 2 valid cards with images. Valid cards={}", cards.size());
                return false;
            }

            Map<String, Object> carouselBody = new LinkedHashMap<>();
            carouselBody.put(
                    "text",
                    "✨ Curated Titan picks for you.\nSwipe to explore and choose your favourite."
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
            reference.put("cust_ref", "titan_carousel_" + System.currentTimeMillis());
            reference.put("messageTag1", "Titan Carousel");
            reference.put("conversationId", "titan_" + phone + "_" + System.currentTimeMillis());

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

            return post(requestBody, phone, "CAROUSEL_MESSAGE");

        } catch (Exception e) {
            log.error("Failed to send carousel message phone={}", phone, e);
            return false;
        }
    }}