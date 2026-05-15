package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.Customer;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import com.example.titan_watch_learning_project.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookServiceImpl implements WebhookService {

    private final BotEngineService botEngineService;
    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    // =============================================
    // MAIN ENTRY — Karix POSTs all events here
    // =============================================
    @Override
    public void handleWebhookEvent(String rawPayload) {
        try {
            log.info("Webhook received: {}", rawPayload);
            JsonNode root = objectMapper.readTree(rawPayload);

            String eventType = root.path("type").asText();
            log.info("Event type: {}", eventType);

            // Local Postman testing payload
            if (root.has("buttonPayload") || root.has("text")) {
                String phone = extractPhone(root);
                String messageText = root.has("text") ? root.get("text").asText() : "";
                String buttonPayload = root.has("buttonPayload")
                        ? root.get("buttonPayload").asText()
                        : extractUserInput(root);



                log.info("LOCAL TEST webhook phone: {}", phone);
                log.info("LOCAL TEST webhook text: {}", messageText);
                log.info("LOCAL TEST webhook buttonPayload: {}", buttonPayload);


                if (phone == null || phone.isBlank()) {
                    log.warn("Phone missing in local test webhook");
                    return;
                }

                Customer customer = findOrCreateCustomer(phone);

                botEngineService.processIncomingMessage(
                        phone,
                        messageText,
                        buttonPayload,
                        customer.getId(),
                        customer.getName()
                );
                return;
            }

            // Real Karix event handling
            switch (eventType) {
                case "message":
                    handleIncomingMessage(root);
                    break;
                case "status":
                    handleStatusUpdate(root);
                    break;
                case "button_reply":
                    handleButtonReply(root);
                    break;
                case "interactive":
                    handleInteractiveReply(root);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
        }
    }

    // =============================================
    // User sent a free-text message
    // =============================================
    private void handleIncomingMessage(JsonNode root) {
        String phone       = root.path("from").asText();
        String messageText = root.path("text").path("body").asText();
        log.info("Incoming text from {}: {}", phone, messageText);

        Customer customer = findOrCreateCustomer(phone);
        botEngineService.processIncomingMessage(
                phone, messageText, null,
                customer.getId(), customer.getName());
    }

    // =============================================
    // User clicked a quick-reply button
    // =============================================
    private void handleButtonReply(JsonNode root) {
        String phone       = root.path("from").asText();
        String buttonId    = root.path("button_reply").path("id").asText();
        String buttonTitle = root.path("button_reply").path("title").asText();
        log.info("Button clicked by {}: {} ({})", phone, buttonTitle, buttonId);

        Customer customer = findOrCreateCustomer(phone);
        botEngineService.processIncomingMessage(
                phone, buttonTitle, buttonId,
                customer.getId(), customer.getName());
    }

    // =============================================
    // User selected from a list (interactive) message
    // =============================================
    private void handleInteractiveReply(JsonNode root) {
        String phone = root.path("from").asText();

        JsonNode buttonReply = root.path("interactive").path("button_reply");
        JsonNode listReply   = root.path("interactive").path("list_reply");

        String id, title;
        if (!buttonReply.isMissingNode()) {
            id    = buttonReply.path("id").asText();
            title = buttonReply.path("title").asText();
        } else {
            id    = listReply.path("id").asText();
            title = listReply.path("title").asText();
        }

        log.info("Interactive reply from {}: {} ({})", phone, title, id);
        Customer customer = findOrCreateCustomer(phone);
        botEngineService.processIncomingMessage(
                phone, title, id,
                customer.getId(), customer.getName());
    }

    // =============================================
    // Delivery / read / failed status callback
    // =============================================
    private void handleStatusUpdate(JsonNode root) {
        String mid    = root.path("id").asText();
        String status = root.path("status").asText();
        log.info("Status update for mid {}: {}", mid, status);

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
                default:
                    log.warn("Unknown delivery status: {}", status);
            }
            messageRepository.save(message);
        });
    }

    // =============================================
    // Find existing customer or create a new guest record
    // =============================================
    private Customer findOrCreateCustomer(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    log.info("New customer from phone: {}", phone);
                    Customer c = Customer.builder()
                            .phone(phone)
                            .name("Guest_" + phone.substring(
                                    Math.max(0, phone.length() - 4)))
                            .birthDay(1)
                            .birthMonth(1)
                            .isActive(true)
                            .build();
                    return customerRepository.save(c);
                });
    }

    private String extractPhone(JsonNode root) {
        if (root.has("from")) {
            return root.get("from").asText();
        }

        if (root.has("sender")) {
            return root.get("sender").asText();
        }

        if (root.has("phone")) {
            return root.get("phone").asText();
        }

        return null;
    }

    private String extractUserInput(JsonNode root) {
        if (root.has("buttonPayload")) {
            return root.get("buttonPayload").asText();
        }

        if (root.has("text")) {
            String text = root.get("text").asText();

            if ("Find My Watch".equalsIgnoreCase(text)) {
                return "FIND_WATCH";
            }

            if ("Birthday Offers".equalsIgnoreCase(text)) {
                return "BIRTHDAY_OFFERS";
            }

            return text;
        }

        return null;
    }

}