

package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.Customer;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl {

    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final BotEngineServiceImpl botEngineService;



    private final BotSessionRepository botSessionRepository;

    private  final MessageRepository messageRepository;



    public void handleWebhookEvent(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            log.info("Webhook received: {}", payload);

            String eventType = root.path("events").path("eventType").asText("");
            log.info("Karix eventType: {}", eventType);

            if ("DELIVERY EVENTS".equalsIgnoreCase(eventType)) {
                handleDeliveryEvent(root);
                return;
            }

            if ("User initiated".equalsIgnoreCase(eventType)) {
                handleUserInitiated(root);
                return;
            }

            log.warn("Unknown event type: {}", eventType);

        } catch (Exception e) {
            log.error("Error handling webhook event", e);
        }
    }

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

        Customer customer = findOrCreate(from, profileName);

        String messageText = "";
        String buttonPayload = "";

        // Normal text message
        if ("text".equalsIgnoreCase(messageType) || "text".equalsIgnoreCase(contentType)) {
            messageText = msgNode.path("text").path("body").asText("");
            buttonPayload = normalizeTextToPayload(messageText);
        }

        // Karix button format:
        // "button": { "text": "...", "payload": "..." }
        else if ("button".equalsIgnoreCase(messageType) || "button".equalsIgnoreCase(contentType)) {
            JsonNode karixButton = msgNode.path("button");

            if (!karixButton.isMissingNode() && !karixButton.isEmpty()) {
                messageText = karixButton.path("text").asText("");
                buttonPayload = karixButton.path("payload").asText("");
            }

            // fallback: button_reply
            if (messageText.isBlank()) {
                JsonNode btnReply = msgNode.path("button_reply");
                if (!btnReply.isMissingNode() && !btnReply.isEmpty()) {
                    buttonPayload = btnReply.path("id").asText("");
                    messageText = btnReply.path("title").asText("");
                }
            }

            // fallback: interactive.button_reply
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
        }

        // Interactive messages / list / WhatsApp flow
        else if ("interactive".equalsIgnoreCase(messageType) || "interactive".equalsIgnoreCase(contentType)) {
            JsonNode interactive = msgNode.path("interactive");

            if (!interactive.isMissingNode() && !interactive.isEmpty()) {
                String interactiveType = interactive.path("type").asText("");

                JsonNode ibtn = interactive.path("button_reply");
                JsonNode ilist = interactive.path("list_reply");
                JsonNode nfmReply = interactive.path("nfm_reply");

                if (!ibtn.isMissingNode() && !ibtn.isEmpty()) {
                    buttonPayload = ibtn.path("id").asText("");
                    messageText = ibtn.path("title").asText("");
                } else if (!ilist.isMissingNode() && !ilist.isEmpty()) {
                    buttonPayload = ilist.path("id").asText("");
                    messageText = ilist.path("title").asText("");
                } else if (!nfmReply.isMissingNode() && !nfmReply.isEmpty()) {
                    messageText = nfmReply.path("body").asText("");
                    buttonPayload = nfmReply.path("response_json").asText("");
                }

                log.info("Interactive type={}", interactiveType);
            }
        }

        // Attachment messages: image, video, audio, document
        else if ("ATTACHMENT".equalsIgnoreCase(contentType)) {
            String attachmentType = msgNode.path("attachmentType").asText("attachment");
            messageText = "[" + attachmentType + "]";

            log.info("Attachment received from {} type={}", from, attachmentType);

            botEngineService.processIncomingMessage(
                    from,
                    messageText,
                    attachmentType,
                    customer.getId(),
                    customer.getName()
            );
            return;
        }

        // fallback
        else {
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

        if (buttonPayload.isBlank() && !messageText.isBlank()) {
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
//    private Customer findOrCreate(String phone, String profileName) {
//        return customerRepository.findByPhone(phone)
//                .orElseGet(() -> {
//                    Customer customer = new Customer();
//                    customer.setPhone(phone);
//                    customer.setName(profileName == null || profileName.isBlank() ? "WhatsApp User" : profileName);
//                    customer.setIsActive(true);
//                    return customerRepository.save(customer);
//                });
//    }


    // FIX:
    private Customer findOrCreate(String phone, String profileName) {  // ← parameter naam
        return customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setPhone(phone);
                    customer.setName(
                            profileName == null || profileName.isBlank()
                                    ? "WhatsApp User"
                                    : profileName
                    );
                    customer.setIsActive(true);
                    return customerRepository.save(customer);
                });
    }
    private String normalizeTextToPayload(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = text.trim()
                .toUpperCase()
                .replace("’", "")
                .replace("'", "")
                .replace(" ", "_")
                .replace("-", "_");

        if ("FIND_MY_WATCH".equals(normalized)
                || "FIND_WATCH".equals(normalized)
                || "FIND_BIRTHDAY_WATCH".equals(normalized)
                || "FIND_MY_BIRTHDAY_WATCH".equals(normalized)) {
            return "FIND_BIRTHDAY_WATCH";
        }

        if ("BIRTHDAY_OFFERS".equals(normalized)
                || "SEE_BIRTHDAY_OFFERS".equals(normalized)
                || "OFFERS".equals(normalized)) {
            return "BIRTHDAY_OFFERS";
        }

        if ("MENS_COLLECTION".equals(normalized)
                || "MENS".equals(normalized)
                || "MEN_COLLECTION".equals(normalized)
                || "MENS_COLLECTION".equals(normalized)) {
            return "MENS_COLLECTION";
        }

        if ("WOMENS_COLLECTION".equals(normalized)
                || "WOMENS".equals(normalized)
                || "WOMEN_COLLECTION".equals(normalized)
                || "WOMENS_COLLECTION".equals(normalized)) {
            return "WOMENS_COLLECTION";
        }

        if ("BELOW_₹10,000".equals(normalized)
                || "BELOW_RS10,000".equals(normalized)
                || "BELOW_10000".equals(normalized)
                || "BELOW_10K".equals(normalized)) {
            return "PRICE_BELOW_10K";
        }

        if ("₹10,000_₹25,000".equals(normalized)
                || "10,000_25,000".equals(normalized)
                || "10000_25000".equals(normalized)
                || "10K_25K".equals(normalized)) {
            return "PRICE_10K_25K";
        }

        if ("ABOVE_₹25,000".equals(normalized)
                || "ABOVE_RS25,000".equals(normalized)
                || "ABOVE_25000".equals(normalized)
                || "ABOVE_25K".equals(normalized)) {
            return "PRICE_25K_PLUS";
        }

        return normalized;
    }
    private void handleDeliveryEvent(JsonNode root) {
        String status     = root.path("notificationAttributes").path("status").asText("");
        String reason     = root.path("notificationAttributes").path("reason").asText("");
        String code       = root.path("notificationAttributes").path("code").asText("");
        String to         = root.path("recipient").path("to").asText("");
        String mid        = root.path("events").path("mid").asText("");
        String templateId = root.path("templateId").asText("");

        log.info("Delivery event: to={} status={} reason={} code={} mid={} templateId={}",
                to, status, reason, code, mid, templateId);

        // ── YE NAYA BLOCK ADD KARO — Outbound message save/update karo ──
        saveOrUpdateOutboundMessage(to, mid, status, templateId);

        // ── AUTO SESSION FROM TEMPLATE ────────────────────────────
        // Jab template delivered ya sent hua
        // → Session auto-create karo agar exist nahi karta
        if (("delivered".equalsIgnoreCase(status) || "sent".equalsIgnoreCase(status))
                && !to.isBlank()) {

            String step = switch (templateId) {
                case "tw_bday_t"  -> "BIRTHDAY_T10_CONFIRMATION_SENT";
                case "tw_bday"    -> "BIRTHDAY_TDAY_TEMPLATE_SENT";
                case "tw_anniv_t" -> "ANNIVERSARY_T10_CONFIRMATION_SENT";
                case "tw_anniv"   -> "ANNIVERSARY_TDAY_TEMPLATE_SENT";
                default           -> null;
            };

            if (step != null) {
                autoCreateSessionIfNeeded(to, step, templateId);
            }
        }
        // ─────────────────────────────────────────────────────────
    }

    // ── NAYA METHOD — messages table mein outbound entry save/update ──
// ── NAYA METHOD — outbound message save ya status update ──────────
    private void saveOrUpdateOutboundMessage(String phone, String mid, String karixStatus, String templateId) {
        try {
            if (phone.isBlank() || mid.isBlank()) {
                log.warn("Skipping message save — phone or mid blank. phone={} mid={}", phone, mid);
                return;
            }

            // Karix status string → entity enum mein map karo
            Message.Status mappedStatus = mapKarixStatus(karixStatus);
            if (mappedStatus == null) {
                log.warn("Unknown Karix status '{}' — skipping save for mid={}", karixStatus, mid);
                return;
            }

            Optional<Message> existing = messageRepository.findByMid(mid);

            if (existing.isPresent()) {
                // ── Already row hai (pehle SENT save hua tha) → status update karo ──
                Message msg = existing.get();
                msg.setStatus(mappedStatus);

                if (mappedStatus == Message.Status.DELIVERED) {
                    msg.setDeliveredAt(LocalDateTime.now());
                } else if (mappedStatus == Message.Status.READ) {
                    msg.setReadAt(LocalDateTime.now());
                }

                messageRepository.save(msg);
                log.info("Updated outbound message mid={} status={}", mid, mappedStatus);

            } else {
                // ── Pehli baar (SENT event) → naya row banao ──
                Message msg = Message.builder()
                        .phone(phone)
                        .mid(mid)
                        .direction(Message.Direction.OUTBOUND)
                        .status(mappedStatus)
                        .messageType(Message.MessageType.TEMPLATE)
                        .messageContent(templateId)
                        .stepName(templateId)
                        .sentAt(LocalDateTime.now())
                        .build();

                if (mappedStatus == Message.Status.DELIVERED) {
                    msg.setDeliveredAt(LocalDateTime.now());
                }

                messageRepository.save(msg);
                log.info("Created outbound message mid={} status={} templateId={}",
                        mid, mappedStatus, templateId);
            }
        } catch (Exception e) {
            log.error("Failed to save/update outbound message phone={} mid={}", phone, mid, e);
        }
    }


    // ── Karix ka lowercase status string → Message.Status enum ────────
    private Message.Status mapKarixStatus(String karixStatus) {
        if (karixStatus == null) return null;
        return switch (karixStatus.toLowerCase()) {
            case "sent", "submitted to channel" -> Message.Status.SENT;
            case "delivered" -> Message.Status.DELIVERED;
            case "read"      -> Message.Status.READ;
            case "failed"    -> Message.Status.FAILED;
            case "queued"    -> Message.Status.SENT; // Queued ko bhi SENT treat karo
            default          -> null;
        };
    }



    private void autoCreateSessionIfNeeded(String phone, String step, String templateId) {
        try {
            // Already active session hai? → skip
            boolean hasSession = botSessionRepository
                    .findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(phone)
                    .isPresent();

            if (hasSession) {
                log.info("Session already exists phone={} templateId={} — skipping auto-create",
                        phone, templateId);
                return;
            }

            // Customer DB se lo
            Long customerId = customerRepository.findByPhone(phone)
                    .map(c -> c.getId())
                    .orElse(null);

            // Session create karo
            com.example.titan_watch_learning_project.entity.BotSession session =
                    com.example.titan_watch_learning_project.entity.BotSession.builder()
                            .phone(phone)
                            .customerId(customerId)
                            .currentStep(step)
                            .isActive(true)
                            .sessionStart(java.time.LocalDateTime.now())
                            .lastActivity(java.time.LocalDateTime.now())
                            .build();

            botSessionRepository.save(session);

            log.info("Auto-created session phone={} templateId={} step={}",
                    phone, templateId, step);

        } catch (Exception e) {
            log.error("Failed to auto-create session phone={} templateId={}",
                    phone, templateId, e);
        }
    }

}