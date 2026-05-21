package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotEngineServiceImpl implements BotEngineService {

    private final MessageRepository messageRepository;
    private final BotSessionRepository botSessionRepository;
    private final KarixApiServiceImpl karixApiService;

    @Override
    public void processIncomingMessage(
            String phone,
            String text,
            String payload,
            Long customerId,
            String customerName
    ) {
        log.info("processMessage phone={} payload={} text={}", phone, payload, text);

        saveIncomingMessage(phone, text, payload, customerId);

        BotSession session = getOrCreateSession(phone, customerId);

        if ("FIND_BIRTHDAY_WATCH".equalsIgnoreCase(payload)) {
            karixApiService.sendTextMessage(
                    phone,
                    "Great choice! Let’s help you find the perfect birthday watch. Please choose a collection: Men’s or Women’s."
            );

            session.setCurrentStep("COLLECTION");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        if ("BIRTHDAY_OFFERS".equalsIgnoreCase(payload)) {
            karixApiService.sendTextMessage(
                    phone,
                    "A special birthday deserves special rewards! 🎉 Our Titan team will help you explore your exclusive birthday offers."
            );

            session.setCurrentStep("BIRTHDAY_OFFERS");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        log.info("Step for {}: {}", phone, session.getCurrentStep());

        handleWelcome(phone, customerName, session);
    }
    @Override
    public void sendWelcomeMessage(String phone, String customerName, BotSession session) {

    }

    @Override
    public void sendBirthdayDayMessage(String phone, String customerName, BotSession session) {

    }

//    private void handleWelcome(String phone, BotSession session) {
//        // First test with simple text. Once working, replace with sendImageButtonMessage.
//        karixApiService.sendTextMessage(phone, "Welcome to Titan! ⌚ This is a test notification sent from Samarth to verify message delivery, formatting, and device connectivity. Everything is working smoothly and the integration looks great.");
//
//        // Later, after text is confirmed working:
//        // karixApiService.sendImageButtonMessage(phone);
//
//        session.setCurrentStep("WELCOME_SENT");
//        session.setLastActivity(LocalDateTime.now());
//        botSessionRepository.save(session);
//    }


    private void handleWelcome(String phone, String customerName, BotSession session) {

        // 1st message: current working test message
        karixApiService.sendTextMessage(
                phone,
                "Welcome to Titan! ⌚ This is a test notification sent from Samarth to verify message delivery, formatting, and device connectivity. Everything is working smoothly and the integration looks great."
        );

        // Dynamic name
        String name = customerName != null && !customerName.isBlank()
                ? customerName
                : "Customer";

        // 2nd message: Birthday option buttons
        karixApiService.sendBirthdayOptionsMessage(phone, name);

        session.setCurrentStep("BIRTHDAY_OPTIONS");
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);
    }

    private void saveIncomingMessage(String phone, String text, String payload, Long customerId) {
        Message message = new Message();
        message.setPhone(phone);
        message.setCustomerId(customerId);
        message.setDirection(Message.Direction.INBOUND);

        message.setMessageContent(text);
        message.setButtonPayload(payload);
        if (payload != null && !payload.isBlank() && !payload.equalsIgnoreCase(text)) {
            message.setMessageType(Message.MessageType.BUTTON);
        } else {
            message.setMessageType(Message.MessageType.TEXT);
        }
        message.setStatus(Message.Status.RECEIVED);
        message.setStepName("INCOMING");
        message.setSentAt(LocalDateTime.now());

        messageRepository.save(message);
    }

    private BotSession getOrCreateSession(String phone, Long customerId) {
        return botSessionRepository
                .findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(phone)
                .orElseGet(() -> {
                    BotSession session = new BotSession();
                    session.setPhone(phone);
                    session.setCustomerId(customerId);
                    session.setCurrentStep("WELCOME");
                    session.setIsActive(true);
                    session.setSessionStart(LocalDateTime.now());
                    session.setLastActivity(LocalDateTime.now());
                    return botSessionRepository.save(session);
                });
    }
}