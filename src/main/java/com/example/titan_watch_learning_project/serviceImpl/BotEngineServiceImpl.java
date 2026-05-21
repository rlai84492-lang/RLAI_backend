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

        log.info("Step for {}: {}", phone, session.getCurrentStep());

        if (session.getCurrentStep() == null || session.getCurrentStep().isBlank()) {
            session.setCurrentStep("WELCOME");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
        }

        handleWelcome(phone, session);
    }

    @Override
    public void sendWelcomeMessage(String phone, String customerName, BotSession session) {

    }

    @Override
    public void sendBirthdayDayMessage(String phone, String customerName, BotSession session) {

    }

    private void handleWelcome(String phone, BotSession session) {
        // First test with simple text. Once working, replace with sendImageButtonMessage.
        karixApiService.sendTextMessage(phone, "Welcome to Titan! Thank you for contacting us.");

        // Later, after text is confirmed working:
        // karixApiService.sendImageButtonMessage(phone);

        session.setCurrentStep("WELCOME_SENT");
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
        message.setMessageType(Message.MessageType.TEXT); // ya BUTTON
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