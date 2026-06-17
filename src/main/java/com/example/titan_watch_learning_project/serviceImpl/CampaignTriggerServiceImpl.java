package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
import com.example.titan_watch_learning_project.service.CampaignTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignTriggerServiceImpl implements CampaignTriggerService {

    private final BotSessionRepository botSessionRepository;
    private final CustomerRepository   customerRepository;

    // ─── Step name constants ──────────────────────────────────────
    // These match exactly what BotEngineServiceImpl checks
    private static final String STEP_BIRTHDAY_T10_SENT  = "BIRTHDAY_T10_CONFIRMATION_SENT";
    private static final String STEP_BIRTHDAY_TDAY_SENT = "BIRTHDAY_TDAY_TEMPLATE_SENT";
    private static final String STEP_ANNIV_T10_SENT     = "ANNIVERSARY_T10_CONFIRMATION_SENT";
    private static final String STEP_ANNIV_TDAY_SENT    = "ANNIVERSARY_TDAY_TEMPLATE_SENT";

    // ─────────────────────────────────────────────────────────────
    // BIRTHDAY T-10
    // Sets session to BIRTHDAY_T10_CONFIRMATION_SENT
    // BotEngine then handles: BIRTHDAY_MONTH_YES / BIRTHDAY_MONTH_NO
    // ─────────────────────────────────────────────────────────────
    @Override
    public void prepareBirthdayT10Session(String phone, Long customerId) {
        createOrUpdateSession(phone, customerId, STEP_BIRTHDAY_T10_SENT);
        log.info("Birthday T-10 session prepared phone={}", phone);
    }

    // ─────────────────────────────────────────────────────────────
    // BIRTHDAY T-DAY
    // Sets session to BIRTHDAY_TDAY_TEMPLATE_SENT
    // BotEngine then handles: any text → sendBirthdayTDayOpener()
    // ─────────────────────────────────────────────────────────────
    @Override
    public void prepareBirthdayTDaySession(String phone, Long customerId) {
        createOrUpdateSession(phone, customerId, STEP_BIRTHDAY_TDAY_SENT);
        log.info("Birthday T-Day session prepared phone={}", phone);
    }

    // ─────────────────────────────────────────────────────────────
    // ANNIVERSARY T-10
    // Sets session to ANNIVERSARY_T10_CONFIRMATION_SENT
    // BotEngine then handles: ANNIVERSARY_T10_MONTH_CONFIRMED / DATE_CORRECTION
    // ─────────────────────────────────────────────────────────────
    @Override
    public void prepareAnniversaryT10Session(String phone, Long customerId) {
        createOrUpdateSession(phone, customerId, STEP_ANNIV_T10_SENT);
        log.info("Anniversary T-10 session prepared phone={}", phone);
    }

    // ─────────────────────────────────────────────────────────────
    // ANNIVERSARY T-DAY
    // Sets session to ANNIVERSARY_TDAY_TEMPLATE_SENT
    // BotEngine then handles: any text → sendAnniversaryTDayOpener()
    // ─────────────────────────────────────────────────────────────
    @Override
    public void prepareAnniversaryTDaySession(String phone, Long customerId) {
        createOrUpdateSession(phone, customerId, STEP_ANNIV_TDAY_SENT);
        log.info("Anniversary T-Day session prepared phone={}", phone);
    }

    // ─────────────────────────────────────────────────────────────
    // Common helper
    // 1. Close existing active session for this phone
    // 2. Create fresh session with correct step
    // ─────────────────────────────────────────────────────────────
    private void createOrUpdateSession(String phone, Long customerId, String step) {

        // Step 1: Close any existing active session
        botSessionRepository
                .findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(phone)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    botSessionRepository.save(existing);
                    log.info("Closed existing session id={} phone={}", existing.getId(), phone);
                });

        // Step 2: Set customerId from DB if not provided
        Long resolvedCustomerId = customerId;
        if (resolvedCustomerId == null) {
            resolvedCustomerId = customerRepository
                    .findByPhone(phone)
                    .map(c -> c.getId())
                    .orElse(null);
        }

        // Step 3: Create fresh session
        BotSession session = BotSession.builder()
                .phone(phone)
                .customerId(resolvedCustomerId)
                .currentStep(step)
                .isActive(true)
                .sessionStart(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .build();

        botSessionRepository.save(session);
        log.info("New session created phone={} step={} customerId={}", phone, step, resolvedCustomerId);
    }
}