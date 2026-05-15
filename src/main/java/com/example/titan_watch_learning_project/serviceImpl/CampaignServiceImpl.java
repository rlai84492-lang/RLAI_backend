package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.entity.Campaign;
import com.example.titan_watch_learning_project.entity.CampaignLog;
import com.example.titan_watch_learning_project.entity.Customer;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.CampaignLogRepository;
import com.example.titan_watch_learning_project.repository.CampaignRepository;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import com.example.titan_watch_learning_project.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CustomerRepository customerRepository;
    private final CampaignLogRepository campaignLogRepo;
    private final CampaignRepository campaignRepo;
    private final BotEngineService botEngineService;
    private final BotSessionRepository sessionRepo;

    // =============================================
    // T-10 — runs every day at 9:00 AM
    // =============================================
    @Override
    @Scheduled(cron = "0 0 9 * * *")
    public void triggerT10Campaign() {
        log.info("Running T-10 birthday campaign...");

        LocalDate targetDate = LocalDate.now().plusDays(10);
        int day   = targetDate.getDayOfMonth();
        int month = targetDate.getMonthValue();

        List<Customer> customers =
                customerRepository.findByBirthDayAndBirthMonth(day, month);
        log.info("Found {} customers for T-10 on {}/{}", customers.size(), day, month);

        Campaign campaign =
                campaignRepo.findByCampaignType(Campaign.CampaignType.T10).orElse(null);

        for (Customer customer : customers) {
            try {
                // Skip if already sent today
                boolean alreadySent = campaignLogRepo
                        .existsByCustomerIdAndCampaignTypeAndSentAtDate(
                                customer.getId(), "T10", LocalDate.now());
                if (alreadySent) {
                    log.info("T-10 already sent to {}", customer.getPhone());
                    continue;
                }

                // Deactivate any old active session for this number
                sessionRepo.findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(customer.getPhone())
                        .ifPresent(oldSession -> {
                            oldSession.setIsActive(false);
                            sessionRepo.save(oldSession);
                        });
                // Fresh session for this campaign
                BotSession session = BotSession.builder()
                        .phone(customer.getPhone())
                        .customerId(customer.getId())
                        .currentStep("WELCOME")
                        .campaignType(BotSession.CampaignType.T10)
                        .isActive(true)
                        .build();
                sessionRepo.save(session);

                botEngineService.sendWelcomeMessage(
                        customer.getPhone(), customer.getName(), session);

                // Log the outbound campaign message
                if (campaign != null) {
                    CampaignLog entry = CampaignLog.builder()
                            .campaignId(campaign.getId())
                            .customerId(customer.getId())
                            .phone(customer.getPhone())
                            .campaignType("T10")
                            .status(CampaignLog.Status.SENT)
                            .build();
                    campaignLogRepo.save(entry);
                }

                log.info("T-10 sent to: {}", customer.getPhone());
                Thread.sleep(500); // Rate-limiting buffer between sends

            } catch (Exception e) {
                log.error("Failed T-10 for {}: {}", customer.getPhone(), e.getMessage());
            }
        }

        log.info("T-10 campaign completed for {}/{}", day, month);
    }

    // =============================================
    // T-DAY — runs every day at 8:00 AM
    // =============================================
    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void triggerTDayCampaign() {
        log.info("Running T-Day birthday campaign...");

        LocalDate today = LocalDate.now();
        int day   = today.getDayOfMonth();
        int month = today.getMonthValue();

        List<Customer> customers =
                customerRepository.findByBirthDayAndBirthMonth(day, month);
        log.info("Found {} birthday customers today {}/{}", customers.size(), day, month);

        for (Customer customer : customers) {
            try {
                // Reuse active T-10 session if present, otherwise create fresh one
                BotSession session = sessionRepo
                        .findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(customer.getPhone())
                        .orElseGet(() -> {
                            BotSession s = BotSession.builder()
                                    .phone(customer.getPhone())
                                    .customerId(customer.getId())
                                    .currentStep("OFFER")
                                    .campaignType(BotSession.CampaignType.TDAY)
                                    .isActive(true)
                                    .build();
                            return sessionRepo.save(s);
                        });

                session.setCurrentStep("OFFER");
                sessionRepo.save(session);

                botEngineService.sendBirthdayDayMessage(
                        customer.getPhone(), customer.getName(), session);

                // Log
                CampaignLog entry = CampaignLog.builder()
                        .customerId(customer.getId())
                        .phone(customer.getPhone())
                        .campaignType("TDAY")
                        .status(CampaignLog.Status.SENT)
                        .build();
                campaignLogRepo.save(entry);

                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Failed T-Day for {}: {}", customer.getPhone(), e.getMessage());
            }
        }

        log.info("T-Day campaign completed for {}/{}", day, month);
    }

    // =============================================
    // MANUAL TRIGGER — for UAT / testing only
    // =============================================
    @Override
    public void manualTrigger(String phone, String customerName, String campaignType) {
        log.info("Manual trigger for {} - {}", phone, campaignType);

        // Old active sessions deactivate karo
        List<BotSession> activeSessions = sessionRepo.findByPhoneAndIsActiveTrue(phone);
        for (BotSession oldSession : activeSessions) {
            oldSession.setIsActive(false);
        }
        sessionRepo.saveAll(activeSessions);

        BotSession session = BotSession.builder()
                .phone(phone)
                .currentStep("WELCOME")
                .campaignType("TDAY".equalsIgnoreCase(campaignType)
                        ? BotSession.CampaignType.TDAY
                        : BotSession.CampaignType.T10)
                .isActive(true)
                .build();

        sessionRepo.save(session);

        if ("T10".equalsIgnoreCase(campaignType)) {
            botEngineService.sendWelcomeMessage(phone, customerName, session);
        } else {
            botEngineService.sendBirthdayDayMessage(phone, customerName, session);
        }
    }
}