package com.example.titan_watch_learning_project.serviceImpl;//package com.example.titan.serviceImpl;
//
//import com.example.titan.entity.Customer;
//import com.example.titan.entity.Message;
//import com.example.titan.repository.CustomerRepository;
//import com.example.titan.repository.MessageRepository;
//import com.example.titan.service.BotEngineService;
//import com.example.titan.service.WebhookService;
import com.example.titan_watch_learning_project.entity.Customer;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import com.example.titan_watch_learning_project.service.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    private final BotEngineService botEngineService;
    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    // ══════════════════════════════════════════════════════════════
    // MAIN ENTRY — Karix POSTs all events here
    //
    // Real Karix payload structure (from logs):
    // {
    //   "channel": "WABA",
    //   "events": { "eventType": "DELIVERY EVENTS" or "User initiated" },
    //   "eventContent": { "message": { "from": "...", "text": {...}, ... } },
    //   "notificationAttributes": { "status": "delivered/read/failed" }
    // }
    // ══════════════════════════════════════════════════════════════
    @Override
    public void handleWebhookEvent(String rawPayload) {
        try {
            log.info("Webhook received: {}", rawPayload.substring(0, Math.min(rawPayload.length(), 200)));
            JsonNode root = objectMapper.readTree(rawPayload);

            // ── LOCAL POSTMAN TESTING FORMAT ──────────────────────
            // { "from": "91xxx", "buttonPayload": "FIND_WATCH", "text": "..." }
            if (root.has("from") && (root.has("buttonPayload") || root.has("text"))
                    && !root.has("channel")) {
                handleLocalTest(root);
                return;
            }

            // ── REAL KARIX FORMAT ──────────────────────────────────
            // eventType is inside events.eventType — NOT at root level
            String eventType = root.path("events").path("eventType").asText("");
            log.info("Karix eventType: {}", eventType);

            if ("User initiated".equals(eventType)) {
                // User sent a message (text, button click, audio, etc.)
                handleUserInitiated(root);
            } else if ("DELIVERY EVENTS".equals(eventType)) {
                // Delivery/read status update
                handleDeliveryEvent(root);
            } else {
                log.warn("Unhandled eventType: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // User initiated — user sent something to our WABA number
    // ══════════════════════════════════════════════════════════════
    private void handleUserInitiated(JsonNode root) {
        JsonNode msgNode = root.path("eventContent").path("message");

        if (msgNode.isMissingNode() || msgNode.isEmpty()) {
            log.warn("No message in eventContent");
            return;
        }

        String from = msgNode.path("from").asText("");
        String contentType = msgNode.path("contentType").asText("");
        String messageType = msgNode.path("messageType").asText("");
        String profileName = msgNode.path("profileName").asText("");

        log.info("User initiated from={} profileName={} contentType={} messageType={}",
                from, profileName, contentType, messageType);

        if (from.isBlank()) {
            log.warn("User initiated event received but from number is blank");
            return;
        }

        Customer customer = findOrCreate(from);

        String messageText = "";
        String buttonPayload = "";

        if ("text".equalsIgnoreCase(messageType) || "text".equalsIgnoreCase(contentType)) {

            messageText = msgNode.path("text").path("body").asText("");
            buttonPayload = normalizeTextToPayload(messageText);

        } else if ("button".equalsIgnoreCase(messageType) || "button".equalsIgnoreCase(contentType)) {

            JsonNode karixButton = msgNode.path("button");

            if (!karixButton.isMissingNode() && !karixButton.isEmpty()) {
                messageText = karixButton.path("text").asText("");
                buttonPayload = karixButton.path("payload").asText("");
            }

            if (messageText.isBlank()) {
                JsonNode btnReply = msgNode.path("button_reply");

                if (!btnReply.isMissingNode() && !btnReply.isEmpty()) {
                    buttonPayload = btnReply.path("id").asText("");
                    messageText = btnReply.path("title").asText("");
                }
            }

            if (messageText.isBlank()) {
                JsonNode interactive = msgNode.path("interactive");

                if (!interactive.isMissingNode() && !interactive.isEmpty()) {
                    JsonNode ibtn = interactive.path("button_reply");

                    if (!ibtn.isMissingNode() && !ibtn.isEmpty()) {
                        buttonPayload = ibtn.path("id").asText("");
                        messageText = ibtn.path("title").asText("");
                    }
                }
            }

        } else if ("interactive".equalsIgnoreCase(messageType) || "interactive".equalsIgnoreCase(contentType)) {

            JsonNode interactive = msgNode.path("interactive");

            if (!interactive.isMissingNode() && !interactive.isEmpty()) {
                JsonNode ibtn = interactive.path("button_reply");
                JsonNode ilist = interactive.path("list_reply");

                if (!ibtn.isMissingNode() && !ibtn.isEmpty()) {
                    buttonPayload = ibtn.path("id").asText("");
                    messageText = ibtn.path("title").asText("");
                } else if (!ilist.isMissingNode() && !ilist.isEmpty()) {
                    buttonPayload = ilist.path("id").asText("");
                    messageText = ilist.path("title").asText("");
                }
            }

        } else if ("ATTACHMENT".equalsIgnoreCase(contentType)) {

            messageText = "[" + msgNode.path("attachmentType").asText("attachment") + "]";
            karixAcknowledge(from, customer);
            return;

        } else {

            messageText = msgNode.path("text").path("body").asText("");

            if (messageText.isBlank()) {
                log.info("Unknown message type from {}: contentType={} messageType={} raw={}",
                        from, contentType, messageType, msgNode);
                return;
            }

            buttonPayload = normalizeTextToPayload(messageText);
        }

        if (messageText.isBlank() && buttonPayload.isBlank()) {
            log.warn("Could not parse user message from {}. contentType={} messageType={} raw={}",
                    from, contentType, messageType, msgNode);
            return;
        }

        if (buttonPayload.isBlank()) {
            buttonPayload = normalizeTextToPayload(messageText);
        }

        log.info("Processing: phone={} text={} payload={}", from, messageText, buttonPayload);

        botEngineService.processIncomingMessage(
                from,
                messageText,
                buttonPayload,
                customer.getId(),
                customer.getName()
        );
    }
    // ══════════════════════════════════════════════════════════════
    // Delivery event — message delivered / read / failed
    // ══════════════════════════════════════════════════════════════
    private void handleDeliveryEvent(JsonNode root) {
        String mid    = root.path("events").path("mid").asText();
        String status = root.path("notificationAttributes").path("status").asText();
        log.info("Delivery: mid={} status={}", mid, status);

        if (mid.isBlank()) return;

        messageRepository.findByMid(mid).ifPresent(message -> {
            switch (status.toLowerCase()) {
                case "delivered":
                    message.setStatus(Message.Status.DELIVERED);
                    message.setDeliveredAt(LocalDateTime.now());
                    break;
                case "read":
                    message.setStatus(Message.Status.READ);
                    message.setReadAt(LocalDateTime.now());
                    break;
                case "failed":
                    message.setStatus(Message.Status.FAILED);
                    break;
            }
            messageRepository.save(message);
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Local Postman test format handler
    // ══════════════════════════════════════════════════════════════
    private void handleLocalTest(JsonNode root) {
        String phone = root.path("from").asText();
        if (phone.isBlank()) {
            log.warn("Local test: no phone");
            return;
        }
        String text    = root.path("text").asText("");
        String payload = root.has("buttonPayload")
                ? root.get("buttonPayload").asText()
                : normalizeTextToPayload(text);

        log.info("LOCAL TEST: phone={} text={} payload={}", phone, text, payload);
        Customer customer = findOrCreate(phone);
        botEngineService.processIncomingMessage(phone, text, payload,
                customer.getId(), customer.getName());
    }

    // ══════════════════════════════════════════════════════════════
    // For unknown/attachment — just reply politely
    // ══════════════════════════════════════════════════════════════
    private void karixAcknowledge(String phone, Customer customer) {
        botEngineService.processIncomingMessage(phone, "Hi", "WELCOME_BACK",
                customer.getId(), customer.getName());
    }

    // ══════════════════════════════════════════════════════════════
    // Normalize free text to payload (user typed instead of clicking)
    // ══════════════════════════════════════════════════════════════
    private String normalizeTextToPayload(String text) {
        if (text == null) return "";
        String t = text.toLowerCase().trim();

        // Greetings → start bot
        if (t.matches("hi|hello|hey|helo|start|tryon|namaste|hii|namaskar"))
            return "FIND_WATCH";

        // Watch discovery
        if (t.contains("find") || t.contains("watch") || t.contains("birthday watch"))
            return "FIND_WATCH";

        // Offers
        if (t.contains("offer") || t.contains("discount") || t.contains("birthday offer"))
            return "BIRTHDAY_OFFERS";

        // Collection
        if (t.contains("men") || t.contains("gents") || t.contains("male"))
            return "MENS";
        if (t.contains("women") || t.contains("ladies") || t.contains("female"))
            return "WOMENS";

        // Style
        if (t.contains("minimal") || t.contains("chic") || t.contains("simple"))
            return "MINIMAL_CHIC";
        if (t.contains("bold") || t.contains("edgy"))
            return "BOLD_EDGY";
        if (t.contains("luxe") || t.contains("classy") || t.contains("luxury"))
            return "LUXE_CLASSY";
        if (t.contains("sport") || t.contains("adventure"))
            return "SPORTY_ADVENTUROUS";

        // Price keywords
        if (t.contains("2000") || t.contains("5000") || t.contains("2k") || t.contains("5k"))
            return "2000-5000";
        if (t.contains("10000") || t.contains("10k") || t.contains("under 10"))
            return "5000-10000";
        if (t.contains("25000") || t.contains("25k"))
            return "10000-25000";
        if (t.contains("above 25") || t.contains("premium") || t.contains("luxury"))
            return "25000-999999";

        // Price question — user asked about price
        if (t.contains("price") || t.contains("cost") || t.contains("rate") || t.contains("kitna"))
            return "SEE_PRICE";

        // Callback
        if (t.contains("call") || t.contains("callback") || t.contains("contact"))
            return "CALLBACK";

        // Store
        if (t.contains("store") || t.contains("shop") || t.contains("visit"))
            return "STORE_VISIT";

        // Website
        if (t.contains("website") || t.contains("online") || t.contains("web"))
            return "WEBSITE";

        // Catalogue
        if (t.contains("catalogue") || t.contains("catalog") || t.contains("pdf"))
            return "CATALOGUE";

        return text.toUpperCase().trim();
    }

    // ══════════════════════════════════════════════════════════════
    // Find or create customer
    // ══════════════════════════════════════════════════════════════
    private Customer findOrCreate(String phone) {
        return customerRepository.findByPhone(phone).orElseGet(() -> {
            log.info("New customer: {}", phone);
            return customerRepository.save(Customer.builder()
                    .phone(phone)
                    .name("Guest_" + phone.substring(Math.max(0, phone.length() - 4)))
                    .birthDay(1)
                    .birthMonth(1)
                    .isActive(true)
                    .build());
        });
    }
}