//package com.example.titan_watch_learning_project.serviceImpl;
//import com.example.titan_watch_learning_project.entity.BotSession;
//import com.example.titan_watch_learning_project.entity.BrandCarouselCard;
//import com.example.titan_watch_learning_project.repository.BotSessionRepository;
//import com.example.titan_watch_learning_project.repository.BrandCarouselCardRepository;
//import com.example.titan_watch_learning_project.repository.CustomerRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import com.example.titan_watch_learning_project.entity.Message;
//import com.example.titan_watch_learning_project.entity.WatchProduct;
//import com.example.titan_watch_learning_project.repository.MessageRepository;
//import com.example.titan_watch_learning_project.service.BotEngineService;
//import com.example.titan_watch_learning_project.service.ProductCatalogService;
//
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class BotEngineServiceImpl implements BotEngineService {
//
//    private final MessageRepository messageRepository;
//    private final BotSessionRepository botSessionRepository;
//    private final KarixApiServiceImpl karixApiService;
//private  final ProductCatalogService productCatalogService;
//    private final CustomerRepository customerRepository;
//    private final TaskScheduler taskScheduler;
//
//    private final BrandCarouselCardRepository brandCarouselCardRepository;
//
//
//
//    private void sendDobConfirmationButtons(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "🎂 *Birthday Month Confirmation*\n\n"
//                        + "Hi *" + firstName + "*,\n\n"
//                        + "Our records show that this is your *birthday month*.\n\n"
//                        + "Can you confirm this for us?",
//                List.of(
//                        Map.of("title", "Yes, correct", "payload", "BIRTHDAY_MONTH_YES"),
//                        Map.of("title", "No, not right", "payload", "BIRTHDAY_MONTH_NO")
//                )
//        );
//    }
//    private void sendTDayAccountBenefitIntro(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "🎉 *Birthday Benefit Activated*\n\n"
//                        + "Hi *" + firstName + "*, our records show that your annual account benefit has been updated.\n\n"
//                        + "A *10% discount code* is now active on your profile and can be redeemed at the nearest *Titan World* store.\n\n"
//                        + "This benefit is valid for the next *21 days*.\n\n"
//                        + "Tap below:",
//                List.of(
//                        Map.of("title", "View Account Benefit", "payload", "VIEW_ACCOUNT_BENEFIT"),
//                        Map.of("title", "Locate Nearest Store", "payload", "LOCATE_NEAREST_STORE")
//                )
//        );
//    }
//
//
//    private void sendAnniversaryTDayAccountBenefitIntro(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "💍 *Anniversary Benefit Activated*\n\n"
//                        + "Hi *" + firstName + "*, our records show that your annual account benefit has been updated.\n\n"
//                        + "A *10% anniversary discount code* is now active on your profile and can be redeemed at the nearest *Titan World* store.\n\n"
//                        + "This benefit is valid for the next *21 days*.\n\n"
//                        + "Tap below:",
//                List.of(
//                        Map.of("title", "View Account Benefit", "payload", "ANNIV_VIEW_BENEFIT"),
//                        Map.of("title", "Locate Nearest Store", "payload", "ANNIV_LOCATE_STORE")
//                )
//        );
//    }
//
//    private void sendAnniversaryTDayWish(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "🎉 *HAPPY ANNIVERSARY, " + firstName + "!*\n\n"
//                        + "May this day be filled with exceptional moments — and many more years of beautiful time together.\n\n"
//                        + "Enjoy up to *10% discount* on your purchase at the nearest *Titan World* store for the next *21 days*.\n\n"
//                        + "Once again, a very happy anniversary from all of us at *Titan World*!",
//                List.of(
//                        Map.of("title", "Locate Store", "payload", "ANNIV_LOCATE_STORE")
//                )
//        );
//    }
//
//    private void sendAnniversaryTDayStoreHelp(String phone) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "📍 *Store Visit Assistance*\n\n"
//                        + "Hope you found your nearest store.\n\n"
//                        + "If you need help, our expert can call you and book an appointment.",
//                List.of(
//                        Map.of("title", "Request Callback", "payload", "ANNIV_REQUEST_CALLBACK")
//                )
//        );
//    }
//
//    private void sendAnniversaryTDayCallbackConfirmation(String phone) {
//        karixApiService.sendTextMessage(
//                phone,
//                "✅ *Done. Our team will be in touch.*\n\n"
//                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
//                        + "In the meantime, feel free to see the curated collection."
//        );
//    }
//
//    private void sendAnniversaryTDayFollowUp(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "⏳ *Your anniversary offer is still live, " + firstName + ".*\n\n"
//                        + "A few hours left today — and we'd hate for you to miss it.\n\n"
//                        + "Visit your nearest store or book an appointment with an expert.",
//                List.of(
//                        Map.of("title", "Visit Store Today", "payload", "ANNIV_VISIT_STORE_TODAY"),
//                        Map.of("title", "Request Callback", "payload", "ANNIV_REQUEST_CALLBACK")
//                )
//        );
//    }
//
//    private void sendAnniversaryTDayExit(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "💍 *Hope you had a wonderful anniversary, " + firstName + ".*\n\n"
//                        + "The *Titan World* family is always here when you're ready.\n\n"
//                        + "Visit us anytime — no anniversary needed.",
//                List.of(
//                        Map.of("title", "Visit us anytime", "payload", "ANNIV_VISIT_US_ANYTIME")
//                )
//        );
//    }
//
//
//    private boolean isAnniversaryTDayFlowStep(String step) {
//        if (step == null || step.isBlank()) {
//            return false;
//        }
//
//        return "ANNIVERSARY_T_DAY_TEMPLATE_SENT".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_WISH_SENT".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_STEP_6B_SENT".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_CALLBACK_CONFIRMED".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_STEP_7_SENT".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_STEP_7_STORE_SENT".equalsIgnoreCase(step)
//                || "ANNIVERSARY_T_DAY_STEP_7_CALLBACK_CONFIRMED".equalsIgnoreCase(step);
//    }
//
//
//    private void scheduleAnniversaryTDayStep6b(String phone, Long customerId, String firstName) {
//        taskScheduler.schedule(() -> {
//            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);
//
//            if (latest == null) {
//                return;
//            }
//
//            if (!"ANNIVERSARY_T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(latest.getCurrentStep())) {
//                return;
//            }
//
//            sendAnniversaryTDayStoreHelp(phone);
//
//            latest.setCurrentStep("ANNIVERSARY_T_DAY_STEP_6B_SENT");
//            latest.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(latest);
//
//        }, Instant.now().plusSeconds(ANNIVERSARY_TDAY_STEP_6B_DELAY_SECONDS));
//    }
//
//    private void scheduleAnniversaryTDayStep7(String phone, Long customerId, String firstName) {
//        taskScheduler.schedule(() -> {
//            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);
//
//            if (latest == null) {
//                return;
//            }
//
//            if (!"ANNIVERSARY_T_DAY_CALLBACK_CONFIRMED".equalsIgnoreCase(latest.getCurrentStep())) {
//                return;
//            }
//
//            sendAnniversaryTDayFollowUp(phone, firstName);
//
//            latest.setCurrentStep("ANNIVERSARY_T_DAY_STEP_7_SENT");
//            latest.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(latest);
//
//        }, Instant.now().plusSeconds(ANNIVERSARY_TDAY_STEP_7_DELAY_SECONDS));
//    }
//
//    private void scheduleAnniversaryTDayStep8(String phone, Long customerId, String firstName) {
//        taskScheduler.schedule(() -> {
//            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);
//
//            if (latest == null) {
//                return;
//            }
//
//            if (!"ANNIVERSARY_T_DAY_STEP_7_CALLBACK_CONFIRMED".equalsIgnoreCase(latest.getCurrentStep())) {
//                return;
//            }
//
//            sendAnniversaryTDayExit(phone, firstName);
//
//            latest.setCurrentStep("ANNIVERSARY_T_DAY_FLOW_ENDED");
//            latest.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(latest);
//
//        }, Instant.now().plusSeconds(ANNIVERSARY_TDAY_STEP_8_DELAY_SECONDS));
//    }
//
//
//    private void sendCallbackConfirmationWithExplore(String phone) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "✅ *Done. Our team will be in touch.*\n\n"
//                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
//                        + "In the meantime, feel free to see the curated collection.",
//                List.of(
//                        Map.of("title", "Explore Again", "payload", "EXPLORE_AGAIN")
//                )
//        );
//    }
//
//    private void sendAnniversaryConfirmationButtons(String phone, String firstName, String anniversaryMonth) {
//        String monthText = (anniversaryMonth == null || anniversaryMonth.isBlank())
//                ? "this"
//                : anniversaryMonth;
//
//        karixApiService.sendButtonMessage(
//                phone,
//                "✨ 💍 *Anniversary Month Confirmation*\n\n"
//                        + "Hi *" + firstName + "*,\n\n"
//                        + "Our records show that *" + monthText + "* is your anniversary month. 🎉\n\n"
//                        + "👉 *Can you confirm this for us?*" + "* is your anniversary month.\n\n"
//                        + "Can you confirm this for us?",
//                List.of(
//                        Map.of("title", "✅Yes, correct", "payload", "ANNIVERSARY_MONTH_YES"),
//                        Map.of("title", "❌No, not right", "payload", "ANNIVERSARY_MONTH_NO")
//                )
//        );
//    }
////    private void sendAnniversaryBridgeMessage(String phone, String firstName) {
////        karixApiService.sendButtonMessage(
////                phone,
////                "✨ 💍 *Thank you for confirming, " + firstName + "!*\n\n"
////                        + "Your *anniversary month* is noted. 💖\n\n"
////                        + "👉 *Would you still like to explore our curated collections?*",
////                List.of(
////                        Map.of("title", "✅Yes, show me", "payload", "YES_SHOW_ME"),
////                        Map.of("title", "❌No, maybe later", "payload", "NO_MAYBE_LATER")
////                )
////        );
////    }
//
//
//
//    private void sendAnniversaryBridgeMessage(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "✨ 💍 *Thank you for confirming, " + firstName + "!*\n\n"
//                        + "Your *anniversary month* is noted. 💖\n\n"
//                        + "👉 *Would you still like to explore our curated collections?*",
//                List.of(
//                        Map.of("title", "Yes, show me", "payload", "ANNIV_YES_SHOW_ME"),
//                        Map.of("title", "Maybe later", "payload", "ANNIV_MAYBE_LATER")
//                )
//        );
//    }
//
//
//    private void sendAnniversaryOpener(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "💍 *Great to hear from you, " + firstName + "!*\n\n"
//                        + "We have got something special waiting for you from the *Titan World* collection - perfect for your *anniversary*.\n\n"
//                        + "👉 *What would you like to do?*",
//                List.of(
//                        Map.of("title", "Find perfect gift", "payload", "FIND_ANNIVERSARY_GIFT"),
//                        Map.of("title", "Anniv offers", "payload", "SEE_ANNIVERSARY_OFFERS")
//                )
//        );
//    }
//
//
//    private void sendAnniversaryGenderSelection(String phone) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "💍 *Let's find the perfect watch for the occasion.*\n\n"
//                        + "👉Browsing for:",
//                List.of(
//                        Map.of("title", "Men's collection", "payload", "MENS_COLLECTION"),
//                        Map.of("title", "Women's collection", "payload", "WOMENS_COLLECTION"),
//                        Map.of("title", "Couple watches", "payload", "COUPLE_WATCHES")
//                )
//        );
//    }
//
//    private void sendCoupleWatchesCatalogue(String phone, String firstName) {
//        karixApiService.sendTextMessage(
//                phone,
//                "💍 *Here are our Couple Watches collections, " + firstName + ".*\n\n"
//                        + "Choose whose catalogue you'd like to explore - or both!\n\n"
//                        + "📖 " + SAMPLE_ANNIVERSARY_PDF_URL
//        );
//    }
//
//
//    private void sendAnniversaryCatalogueFollowUp(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "👀 *Did anything catch your eye, " + firstName + "?*\n\n"
//                        + "We can help you get it - visit a nearby *Titan* store or have one of our experts call you.",
//                List.of(
//                        Map.of("title", "Visit nearest store", "payload", "VISIT_NEAREST_STORE"),
//                        Map.of("title", "Request callback", "payload", "REQUEST_CALLBACK")
//                )
//        );
//    }
//
//    private void sendAnniversaryStoreHelpMessage(String phone) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "📍 *Store Visit Assistance*\n\n"
//                        + "Hope you found your nearest store.\n\n"
//                        + "If you need help, our expert can call you and book an appointment.",
//                List.of(
//                        Map.of("title", "Book appointment", "payload", "BOOK_AN_APPOINTMENT")
//                )
//        );
//    }
//
//    private void sendAnniversaryCallbackConfirmation(String phone) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "✅ *Done. Our team will be in touch.*\n\n"
//                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
//                        + "In the meantime, feel free to see the curated collection.",
//                List.of(
//                        Map.of("title", "Explore again", "payload", "EXPLORE_AGAIN")
//                )
//        );
//    }
//
//
//    private void sendAnniversaryOffers(String phone, String firstName) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "🎁 *Here's your anniversary offer, " + firstName + "*\n\n"
//                        + "Up to *10% off* on selected Titan brands - yours to use for *21 days before and after your anniversary*.\n\n"
//                        + "Your nearest *Titan World* store is ready for you.",
//                List.of(
//                        Map.of("title", "Book store visit", "payload", "ANNIV_BOOK_STORE"),
//                        Map.of("title", "Request callback", "payload", "ANNIV_T10_CALLBACK")
//                )
//        );
//    }
//
//
//
//    private String formatMonthName(String month) {
//        if (month == null || month.isBlank()) {
//            return "this";
//        }
//
//        String clean = month.trim().replace("_", " ").toLowerCase(Locale.ENGLISH);
//        return clean.substring(0, 1).toUpperCase(Locale.ENGLISH) + clean.substring(1);
//    }
//
//    private String getAnniversaryMonthForMessage(Long customerId) {
//        if (customerId == null) {
//            return "this";
//        }
//
//        return customerRepository.findById(customerId)
//                .map(customer -> {
//                    if (customer.getAnniversaryMonth() != null && !customer.getAnniversaryMonth().isBlank()) {
//                        return formatMonthName(customer.getAnniversaryMonth());
//                    }
//
//                    if (customer.getAnniversaryDate() != null) {
//                        return formatMonthName(customer.getAnniversaryDate().getMonth().name());
//                    }
//
//                    return "this";
//                })
//                .orElse("this");
//    }
//
//
//    private void scheduleAnniversaryStep4a(String phone, Long customerId, String firstName) {
//        taskScheduler.schedule(() -> {
//            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);
//
//            if (latest == null) {
//                return;
//            }
//
//            if (!"ANNIVERSARY_CATALOGUE_SENT".equalsIgnoreCase(latest.getCurrentStep())
//                    && !"ANNIVERSARY_COUPLE_CATALOGUE_SENT".equalsIgnoreCase(latest.getCurrentStep())) {
//                return;
//            }
//
//            sendAnniversaryCatalogueFollowUp(phone, firstName);
//
//            latest.setCurrentStep("ANNIVERSARY_STEP_4A_SENT");
//            latest.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(latest);
//
//        }, Instant.now().plusSeconds(ANNIVERSARY_STEP_4A_DELAY_SECONDS));
//    }
//
//
//    public void processIncomingMessage(
//            String phone,
//            String text,
//            String payload,
//            Long customerId,
//            String customerName
//    ) {
//        log.info("processMessage phone={} payload={} text={}", phone, payload, text);
//
//        // ✅ User ka har incoming message DB me store hoga.
//        saveIncomingMessage(phone, text, payload, customerId);
//
//        BotSession session = getOrCreateSession(phone, customerId);
//        String firstName = getFirstName(customerName);
//
//        String cleanPayload = normalizePayload(payload);
//        String cleanText = normalizePayload(text);
//
//        if (cleanPayload.isBlank()) {
//            cleanPayload = cleanText;
//        }
//
//        log.info("Current session step for {}: {} cleanPayload={}",
//                phone, session.getCurrentStep(), cleanPayload);
//
//
//        String routePayload = cleanPayload
//                .replaceAll("_+", "_")
//                .replaceAll("^_|_$", "");
//
//        log.info("Routing payload phone={} cleanPayload={} routePayload={}",
//                phone, cleanPayload, routePayload);
//
//
//
//        // ----------------------------------------------------
//// GLOBAL TESTING OVERRIDES
//// Must run before ANNIVERSARY_DATE_PENDING / DOB_CORRECTION_PENDING
//// Existing cleanPayload logic untouched.
//// ----------------------------------------------------
//
//        if ("ANNIVERSARY_DAY_T".equals(routePayload)
//                || "ANNIVERSARY_T_DAY".equals(routePayload)
//                || "ANNIVERSARY_DAY".equals(routePayload)
//                || "HAPPY_ANNIVERSARY".equals(routePayload)
//                || "ANNIVERSARY_DAY_FLOW".equals(routePayload)) {
//
//            log.info("Starting ANNIVERSARY T-DAY flow phone={} routePayload={}", phone, routePayload);
//
//            sendAnniversaryTDayAccountBenefitIntro(phone, firstName);
//
//            session.setCurrentStep("ANNIVERSARY_T_DAY_TEMPLATE_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("CONFIRM_DETAILS".equals(routePayload)
//                || "CONFIRM_DETAIL".equals(routePayload)
//                || "CONFIRM".equals(routePayload)) {
//
//            log.info("Starting BIRTHDAY T-10 confirm details flow phone={} routePayload={}", phone, routePayload);
//
//            session.setCurrentStep("BIRTHDAY_CONFIRM_DETAILS");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            sendDobConfirmationButtons(phone, firstName);
//            return;
//        }
//
//        if ("ANNIVERSARY".equals(routePayload)
//                || "ANNIVARY".equals(routePayload)
//                || "ANNIVERSARY_FLOW".equals(routePayload)) {
//
//            log.info("Starting ANNIVERSARY T-10 flow phone={} routePayload={}", phone, routePayload);
//
//            session.setCurrentStep("ANNIVERSARY_CONFIRMATION_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            sendAnniversaryConfirmationButtons(
//                    phone,
//                    firstName,
//                    getAnniversaryMonthForMessage(customerId)
//            );
//            return;
//        }
//
//
//
//        // ----------------------------------------------------
//// ANNIVERSARY DATE CORRECTION PENDING
//// ----------------------------------------------------
//        if ("ANNIVERSARY_DATE_PENDING".equalsIgnoreCase(session.getCurrentStep())) {
//            if (isValidDob(text)) {
//                LocalDate anniversaryDate = LocalDate.parse(text.trim(), DOB_FORMATTER);
//
//                customerRepository.findById(customerId).ifPresent(customer -> {
//                    customer.setAnniversaryDate(anniversaryDate);
//                    customer.setAnniversaryMonth(anniversaryDate.getMonth().name());
//                    customerRepository.save(customer);
//                });
//
//                session.setCurrentStep("ANNIVERSARY_BRIDGE_SENT");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                sendAnniversaryBridgeMessage(phone, firstName);
//                return;
//            }
//
//            karixApiService.sendTextMessage(
//                    phone,
//                    "Please reply with your anniversary date in this format:\nDD/MM/YYYY"
//            );
//            return;
//        }
//
//        // ----------------------------------------------------
//        // DOB CORRECTION PENDING
//        // Isko session create hone ke baad aur welcome se pehle rakhna hai.
//        // ----------------------------------------------------
//        if ("DOB_CORRECTION_PENDING".equalsIgnoreCase(session.getCurrentStep())) {
//            if (isValidDob(text)) {
//                LocalDate dob = LocalDate.parse(text.trim(), DOB_FORMATTER);
//
//                customerRepository.findById(customerId).ifPresent(customer -> {
//                    customer.setDateOfBirth(dob);
//                    customer.setBirthdayMonth(dob.getMonth().name());
//                    customerRepository.save(customer);
//                });
//
//                session.setCurrentStep("DOB_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                sendBridgeMessage(phone, firstName);
//                return;
//            }
//
//            karixApiService.sendTextMessage(
//                    phone,
//                    "Please reply with your date of birth in this format:\nDD/MM/YYYY"
//            );
//            return;
//        }
//// ----------------------------------------------------
//// FALLBACK TEMPLATE TESTING FLOW
//// User manually types/clicks: Confirm Details
//// Future approved fallback template CTA payload: CONFIRM_DETAILS
//// ----------------------------------------------------
//        if ("CONFIRM_DETAILS".equals(cleanPayload)
//                || "CONFIRM_DETAIL".equals(cleanPayload)
//                || "CONFIRM".equals(cleanPayload)) {
//
//            session.setCurrentStep("BIRTHDAY_CONFIRM_DETAILS");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            sendDobConfirmationButtons(phone,firstName);
//            return;
//        }
//
//        // ----------------------------------------------------
//// ANNIVERSARY YES / NO HANDLING
//// ----------------------------------------------------
//        if ("ANNIVERSARY_CONFIRMATION_SENT".equalsIgnoreCase(session.getCurrentStep())) {
//
////            if ("YES_THAT_S_CORRECT".equals(cleanPayload)
////                    || "YES_THATS_CORRECT".equals(cleanPayload)
////                    || "YES_CORRECT".equals(cleanPayload)
////                    || "ANNIVERSARY_MONTH_YES".equals(cleanPayload)) {
////
////                session.setCurrentStep("ANNIVERSARY_MONTH_CONFIRMED");
////                session.setLastActivity(LocalDateTime.now());
////                botSessionRepository.save(session);
////
////                sendAnniversaryBridgeMessage(phone, firstName);
////                return;
////            }
//
//
//
//            if ("YES_THAT_S_CORRECT".equals(cleanPayload)
//                    || "YES_THATS_CORRECT".equals(cleanPayload)
//                    || "YES_CORRECT".equals(cleanPayload)
//                    || "ANNIVERSARY_MONTH_YES".equals(cleanPayload)) {
//
//                session.setCurrentStep("ANNIVERSARY_BRIDGE_SENT");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                sendAnniversaryBridgeMessage(phone, firstName);
//                return;
//            }
//
//
//            if ("NO_THAT_S_NOT_RIGHT".equals(cleanPayload)
//                    || "NO_THATS_NOT_RIGHT".equals(cleanPayload)
//                    || "NO_NOT_RIGHT".equals(cleanPayload)
//                    || "ANNIVERSARY_MONTH_NO".equals(cleanPayload)) {
//
//                session.setCurrentStep("ANNIVERSARY_DATE_PENDING");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                karixApiService.sendTextMessage(
//                        phone,
//                        "No worries, " + firstName + "!\n"
//                                + "Could you help us update your anniversary date?\n"
//                                + "Please reply with your anniversary date in this format:\n"
//                                + "DD/MM/YYYY"
//                );
//                return;
//            }
//        }
//
//// ----------------------------------------------------
//// ANNIVERSARY TESTING START
//// User manually types: Anniversary
//// Future approved template CTA payloads:
//// Yes -> ANNIVERSARY_MONTH_YES
//// No  -> ANNIVERSARY_MONTH_NO
//// ----------------------------------------------------
//        if ("ANNIVERSARY".equals(cleanPayload)
//                || "ANNIVARY".equals(cleanPayload)
//                || "ANNIVERSARY_FLOW".equals(cleanPayload)) {
//
//            session.setCurrentStep("ANNIVERSARY_CONFIRMATION_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            sendAnniversaryConfirmationButtons(phone, firstName, getAnniversaryMonthForMessage(customerId));
//            return;
//        }
//
//
//        // ----------------------------------------------------
//        // T10 TESTING START
//        // User manually types: Yes, that's correct / No, that's not right
//        // Future approved template payloads:
//        // Yes -> BIRTHDAY_MONTH_YES
//        // No  -> BIRTHDAY_MONTH_NO
//        // ----------------------------------------------------
//        if ("YES_THAT_S_CORRECT".equals(cleanPayload)
//                || "YES_THATS_CORRECT".equals(cleanPayload)
//                || "BIRTHDAY_MONTH_YES".equals(cleanPayload)) {
//
//            session.setCurrentStep("BIRTHDAY_MONTH_CONFIRMED");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            sendChatbotOpener(phone, firstName);
//            return;
//        }
//
//        if ("NO_THAT_S_NOT_RIGHT".equals(cleanPayload)
//                || "NO_THATS_NOT_RIGHT".equals(cleanPayload)
//                || "BIRTHDAY_MONTH_NO".equals(cleanPayload)) {
//
//            session.setCurrentStep("DOB_CORRECTION_PENDING");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            karixApiService.sendTextMessage(
//                    phone,
//                    "No worries, " + firstName + "!\n"
//                            + "Could you help us update your birthday?\n"
//                            + "Please reply with your date of birth in this format:\n"
//                            + "DD/MM/YYYY"
//            );
//            return;
//        }
//
//
//        // ----------------------------------------------------
//// ANNIVERSARY STEP 0b bridge buttons
//// Separate payloads so birthday flow never mixes.
//// ----------------------------------------------------
//        if ("ANNIV_YES_SHOW_ME".equals(cleanPayload)) {
//
//            sendAnniversaryOpener(phone, firstName);
//
//            session.setCurrentStep("ANNIVERSARY_OPENER");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("ANNIV_MAYBE_LATER".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(
//                    phone,
//                    "💍 No worries, " + firstName + "!\n"
//                            + "We're here whenever you're ready."
//            );
//
//            session.setCurrentStep("ANNIVERSARY_GRACEFUL_EXIT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//        // ----------------------------------------------------
//        // STEP 0b bridge message buttons
//        // ----------------------------------------------------
//        if ("YES_SHOW_ME".equals(cleanPayload)
//                || "SHOW_ME".equals(cleanPayload)) {
//
//            if ("ANNIVERSARY_BRIDGE_SENT".equalsIgnoreCase(session.getCurrentStep())
//                    || "ANNIVERSARY_MONTH_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())
//                    || "ANNIVERSARY_DATE_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())) {
//
//                sendAnniversaryOpener(phone, firstName);
//
//                session.setCurrentStep("ANNIVERSARY_OPENER");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//            sendChatbotOpener(phone, firstName);
//
//            session.setCurrentStep("CHATBOT_OPENER");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("NO_MAYBE_LATER".equals(cleanPayload)
//                || "MAYBE_LATER".equals(cleanPayload)) {
//
//            if ("ANNIVERSARY_BRIDGE_SENT".equalsIgnoreCase(session.getCurrentStep())
//                    || "ANNIVERSARY_MONTH_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())
//                    || "ANNIVERSARY_DATE_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())) {
//
//                karixApiService.sendTextMessage(
//                        phone,
//                        "💍 No worries, " + firstName + "!\n"
//                                + "We're here whenever you're ready."
//                );
//
//                session.setCurrentStep("ANNIVERSARY_GRACEFUL_EXIT");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//            karixApiService.sendTextMessage(
//                    phone,
//                    "No worries, " + firstName + "! 🎂 We're here whenever you're ready."
//            );
//
//            session.setCurrentStep("GRACEFUL_EXIT_T10");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//
//
//
//
//        // ----------------------------------------------------
//// ANNIVERSARY T-DAY TESTING START
//// User manually types: Anniversary Day - T
//// Future approved template payloads:
//// View Account Benefit -> ANNIV_VIEW_BENEFIT
//// Locate Nearest Store -> ANNIV_LOCATE_STORE
//// ----------------------------------------------------
//        if ("ANNIVERSARY_DAY_T".equals(cleanPayload)
//                || "ANNIVERSARY_T_DAY".equals(cleanPayload)
//                || "ANNIVERSARY_DAY".equals(cleanPayload)
//                || "HAPPY_ANNIVERSARY".equals(cleanPayload)
//                || "ANNIVERSARY_DAY_FLOW".equals(cleanPayload)) {
//
//            sendAnniversaryTDayAccountBenefitIntro(phone, firstName);
//
//            session.setCurrentStep("ANNIVERSARY_T_DAY_TEMPLATE_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("ANNIV_VIEW_BENEFIT".equals(cleanPayload)
//                || "ANNIVERSARY_VIEW_BENEFIT".equals(cleanPayload)
//                || "ANNIV_VIEW_ACCOUNT_BENEFIT".equals(cleanPayload)
//                || (
//                "ANNIVERSARY_T_DAY_TEMPLATE_SENT".equalsIgnoreCase(session.getCurrentStep())
//                        && ("VIEW_ACCOUNT_BENEFIT".equals(cleanPayload)
//                        || "VIEW_BENEFIT".equals(cleanPayload))
//        )) {
//
//            sendAnniversaryTDayWish(phone, firstName);
//
//            session.setCurrentStep("ANNIVERSARY_T_DAY_WISH_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("ANNIV_LOCATE_STORE".equals(cleanPayload)
//                || "ANNIV_LOCATE_NEAREST_STORE".equals(cleanPayload)
//                || "ANNIVERSARY_LOCATE_STORE".equals(cleanPayload)
//                || (
//                isAnniversaryTDayFlowStep(session.getCurrentStep())
//                        && ("LOCATE_NEAREST_STORE".equals(cleanPayload)
//                        || "LOCATE_STORE".equals(cleanPayload))
//        )) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            session.setCurrentStep("ANNIVERSARY_T_DAY_STORE_LOCATOR_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            scheduleAnniversaryTDayStep6b(phone, customerId, firstName);
//            return;
//        }
//
//        if ("ANNIV_VISIT_STORE_TODAY".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            sendAnniversaryTDayStoreHelp(phone);
//
//            session.setCurrentStep("ANNIVERSARY_T_DAY_STEP_7_STORE_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("ANNIV_VISIT_US_ANYTIME".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            session.setCurrentStep("ANNIVERSARY_T_DAY_FLOW_ENDED");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//
//        // ----------------------------------------------------
//        // T-DAY TESTING START
//        // User manually types: View Account Benefit / Locate Nearest Store
//        // Future approved template payloads:
//        // View Account Benefit -> VIEW_ACCOUNT_BENEFIT
//        // Locate Nearest Store -> LOCATE_NEAREST_STORE
//        // ----------------------------------------------------
//
//        if ("HAPPY_BIRTHDAY".equals(cleanPayload)
//                || "HAPPY_BDAY".equals(cleanPayload)
//                || "BIRTHDAY_DAY".equals(cleanPayload)) {
//
//            sendTDayAccountBenefitIntro(phone, firstName);
//
//            session.setCurrentStep("T_DAY_TEMPLATE_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//        if ("VIEW_ACCOUNT_BENEFIT".equals(cleanPayload)
//                || "VIEW_BENEFIT".equals(cleanPayload)) {
//
//            sendBirthdayWish(phone, firstName);
//
//            session.setCurrentStep("BIRTHDAY_WISH_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("LOCATE_NEAREST_STORE".equals(cleanPayload)) {
//
//            sendStoreHelpBirthdayMessage(phone);
//
//            session.setCurrentStep("T_DAY_STORE_LOCATOR_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//        // ----------------------------------------------------
//// ANNIVERSARY STEP 1
//// ----------------------------------------------------
//        if ("FIND_ANNIVERSARY_GIFT".equals(cleanPayload)
//                || "FIND_PERFECT_GIFT".equals(cleanPayload)
//                || "FIND_GIFT".equals(cleanPayload)) {
//
//            sendAnniversaryGenderSelection(phone);
//
//            session.setCurrentStep("ANNIVERSARY_GENDER_SELECTION");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("SEE_ANNIVERSARY_OFFERS".equals(cleanPayload)
//                || "ANNIVERSARY_OFFERS".equals(cleanPayload)) {
//
//            sendAnniversaryOffers(phone, firstName);
//
//            session.setCurrentStep("ANNIVERSARY_OFFERS_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//        // ----------------------------------------------------
//        // STEP 1: Find my perfect watch
//        // ----------------------------------------------------
//        if ("FIND_MY_PERFECT_WATCH".equals(cleanPayload)
//                || "FIND_PERFECT_WATCH".equals(cleanPayload)
//                || "FIND_BIRTHDAY_WATCH".equals(cleanPayload)
//                || "FIND_WATCH".equals(cleanPayload)
//                || "FIND_MY_WATCH".equals(cleanPayload)
//                || "FIND_MY_BIRTHDAY_WATCH".equals(cleanPayload)) {
//
//            sendGenderSelection(phone);
//
//            session.setCurrentStep("GENDER_SELECTION");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // STEP 1: See birthday offers
//        // ----------------------------------------------------
//
//        if ("SEE_BIRTHDAY_OFFERS".equals(cleanPayload)
//                || "BIRTHDAY_OFFERS".equals(cleanPayload)
//                || "EXCLUSIVE_BIRTHDAY_OFFERS".equals(cleanPayload)) {
//
//            sendBirthdayOfferPath(phone, firstName);
//
//            session.setCurrentStep("BIRTHDAY_OFFER_PATH");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // STEP 2: Men's collection -> brand carousel
//        // ----------------------------------------------------
//        if ("MEN_S_COLLECTION".equals(cleanPayload)
//                || "MENS_COLLECTION".equals(cleanPayload)
//                || "MEN_COLLECTION".equals(cleanPayload)
//                || "MENS".equals(cleanPayload)) {
//
//            boolean sent = sendBrandCarousel(phone, firstName, "MEN");
//
//            if (!sent) {
//                karixApiService.sendTextMessage(
//                        phone,
//                        "Sorry, collections could not be loaded right now. Please try again."
//                );
//                return;
//            }
//
//            if ("ANNIVERSARY_GENDER_SELECTION".equalsIgnoreCase(session.getCurrentStep())) {
//                session.setCurrentStep("ANNIVERSARY_MEN_BRAND_CAROUSEL");
//            } else {
//                session.setCurrentStep("MEN_BRAND_CAROUSEL");
//            }
//
//            session.setSelectedCollection(BotSession.Collection.MENS);
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // STEP 2: Women's collection -> brand carousel
//        // ----------------------------------------------------
//        if ("WOMEN_S_COLLECTION".equals(cleanPayload)
//                || "WOMENS_COLLECTION".equals(cleanPayload)
//                || "WOMEN_COLLECTION".equals(cleanPayload)
//                || "WOMENS".equals(cleanPayload)) {
//
//            boolean sent = sendBrandCarousel(phone, firstName, "WOMEN");
//
//            if (!sent) {
//                karixApiService.sendTextMessage(
//                        phone,
//                        "Sorry, collections could not be loaded right now. Please try again."
//                );
//                return;
//            }
//            if ("ANNIVERSARY_GENDER_SELECTION".equalsIgnoreCase(session.getCurrentStep())) {
//                session.setCurrentStep("ANNIVERSARY_WOMEN_BRAND_CAROUSEL");
//            } else {
//                session.setCurrentStep("WOMEN_BRAND_CAROUSEL");
//            }
//
//            session.setSelectedCollection(BotSession.Collection.WOMENS);
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//// ANNIVERSARY COUPLE WATCHES
//// ----------------------------------------------------
//        if ("COUPLE_WATCHES".equals(cleanPayload)
//                || "COUPLE_WATCHES_💍".equals(cleanPayload)
//                || "COUPLE".equals(cleanPayload)) {
//
//            sendCoupleWatchesCatalogue(phone, firstName);
//
//            session.setCurrentStep("ANNIVERSARY_COUPLE_CATALOGUE_SENT");
//            session.setSelectedCollection(BotSession.Collection.COUPLES);
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            scheduleAnniversaryStep4a(phone, customerId, firstName);
//            return;
//        }
//
//
//        // ----------------------------------------------------
//        // STEP 3: Brand carousel Explore
//        // Payload: EXPLORE_MEN_TITAN_EDGE / EXPLORE_WOMEN_TITAN_RAGA
//        // ----------------------------------------------------
//        if (cleanPayload.startsWith("EX_M_")
//                || cleanPayload.startsWith("EX_W_")) {
//
//            log.info("EXPLORE COLLECTION CLICKED phone={} payload={}", phone, cleanPayload);
//
//            String previousStep = session.getCurrentStep();
//
//            String gender = cleanPayload.startsWith("EX_M_") ? "MEN" : "WOMEN";
//            String brandCode = extractShortBrandCode(cleanPayload);
//            String brandKey = shortBrandCodeToBrandKey(brandCode);
//
//            log.info("EXPLORE resolved gender={} brandCode={} brandKey={}",
//                    gender, brandCode, brandKey);
//
//            boolean anniversaryFlow =
//                    "ANNIVERSARY_MEN_BRAND_CAROUSEL".equalsIgnoreCase(previousStep)
//                            || "ANNIVERSARY_WOMEN_BRAND_CAROUSEL".equalsIgnoreCase(previousStep)
//                            || "ANNIVERSARY_GENDER_SELECTION".equalsIgnoreCase(previousStep)
//                            || "ANNIVERSARY_OPENER".equalsIgnoreCase(previousStep);
//
//            sendCatalogue(phone, firstName, gender, brandKey, anniversaryFlow);
//            if ("ANNIVERSARY_MEN_BRAND_CAROUSEL".equalsIgnoreCase(previousStep)
//                    || "ANNIVERSARY_WOMEN_BRAND_CAROUSEL".equalsIgnoreCase(previousStep)) {
//
//                session.setCurrentStep("ANNIVERSARY_CATALOGUE_SENT");
//                session.setSelectedCollection(
//                        "MEN".equals(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS
//                );
//                session.setSelectedBrand(brandKey);
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                scheduleAnniversaryStep4a(phone, customerId, firstName);
//                return;
//            }
//
//            session.setCurrentStep("CATALOGUE_SENT");
//            session.setSelectedCollection(
//                    "MEN".equals(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS
//            );
//            session.setSelectedBrand(brandKey);
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            scheduleStep4a(phone, customerId, firstName);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // STEP 3: Brand carousel callback
//        // Payload: CALLBACK_MEN_TITAN_EDGE / CALLBACK_WOMEN_TITAN_RAGA
//        // ----------------------------------------------------
//     if (cleanPayload.startsWith("CB_M_")
//             || cleanPayload.startsWith("CB_W_")) {
//
//         String previousStep = session.getCurrentStep();
//
//        String gender = cleanPayload.startsWith("CB_M_") ? "MEN" : "WOMEN";
//        String brandCode = extractShortBrandCode(cleanPayload);
//        String brandKey = shortBrandCodeToBrandKey(brandCode);
//
//        session.setCurrentStep("CALLBACK_REQUESTED");
//         session.setSelectedCollection(
//                 "MEN".equals(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS
//         );
//
//         session.setSelectedBrand(brandKey);
//        session.setLastActivity(LocalDateTime.now());
//        botSessionRepository.save(session);
//
//        karixApiService.sendButtonMessage(
//                phone,
//                "Done. Our team will be in touch.\n\n"
//                        + "One of our Titan experts will call you to help you with the one that caught your eye.\n\n"
//                        + "In the meantime, feel free to see the curated collection.",
//                List.of(
//                        Map.of("title", "Explore again", "payload", "EX_" + ("MEN".equals(gender) ? "M" : "W") + "_" + brandCode + "_0")
//                )
//        );
//        return;
//    }
//
//
//        // ----------------------------------------------------
//// ANNIVERSARY OFFER PATH - Store visit
//// ----------------------------------------------------
//        if ("ANNIV_BOOK_STORE".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            sendAnniversaryStoreHelpMessage(phone);
//
//            session.setCurrentStep("ANNIVERSARY_STORE_VISIT_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//
//        // ----------------------------------------------------
//        // STEP 4a / STEP 5 / Store visit
//        // ----------------------------------------------------
//        if ("VISIT_NEAREST_STORE".equals(cleanPayload)
//                || "VISIT_STORE".equals(cleanPayload)
//                || "BOOK_STORE_VISIT".equals(cleanPayload)
//                || cleanPayload.startsWith("BOOK_STORE_VISIT_")
//                || "NEARBY_STORE".equals(cleanPayload)
//                || "PICK_UP_STORE".equals(cleanPayload)
//                || "STORE_VISIT".equals(cleanPayload)
//                || "FIND_STORE_NEAR_ME".equals(cleanPayload)) {
//
//            String previousStep = session.getCurrentStep();
//
//            if ("ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)) {
//
//                sendAnniversaryStoreHelpMessage(phone);
//
//                session.setCurrentStep("ANNIVERSARY_STORE_VISIT_SENT");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
////            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
////
////            sendStoreHelpMessage(phone);
////
////            session.setCurrentStep("STORE_VISIT_SENT");
////            session.setLastActivity(LocalDateTime.now());
////            botSessionRepository.save(session);
////            return;
//
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            taskScheduler.schedule(() -> {
//                sendStoreHelpMessage(phone);
//            }, Instant.now().plusSeconds(2));
//
//            session.setCurrentStep("STORE_VISIT_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // Birthday wish locate store button
//        // ----------------------------------------------------
//        if ("LOCATE_STORE".equals(cleanPayload)
//                || "LOCATE_NEAREST_STORE_FROM_WISH".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            sendStoreHelpBirthdayMessage(phone);
//
//            session.setCurrentStep("T_DAY_STORE_LOCATOR_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            return;
//        }
//
//        // ----------------------------------------------------
//        // STEP 7 Visit Store Today
//        // ----------------------------------------------------
//        if ("VISIT_STORE_TODAY".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            sendStoreHelpBirthdayMessage(phone);
//
//            session.setCurrentStep("STEP_7_STORE_SENT");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // Callback / Book appointment
//        // ----------------------------------------------------
////        if ("REQUEST_CALLBACK".equals(cleanPayload)
////                || cleanPayload.startsWith("REQUEST_CALLBACK_")
////                || "REQUEST_A_CALLBACK".equals(cleanPayload)
////                || "BOOK_AN_APPOINTMENT".equals(cleanPayload)
////                || "INTERESTED".equals(cleanPayload)
////                || "SPEAK_WITH_EXPERT".equals(cleanPayload)) {
////
////
////            String previousStep = session.getCurrentStep();
////
////            if ("ANNIV_REQUEST_CALLBACK".equals(cleanPayload)
////                    || (
////                    "REQUEST_CALLBACK".equals(cleanPayload)
////                            && isAnniversaryTDayFlowStep(previousStep)
////            )) {
////
////                sendAnniversaryTDayCallbackConfirmation(phone);
////
////                if ("ANNIVERSARY_T_DAY_STEP_7_SENT".equalsIgnoreCase(previousStep)
////                        || "ANNIVERSARY_T_DAY_STEP_7_STORE_SENT".equalsIgnoreCase(previousStep)) {
////
////                    session.setCurrentStep("ANNIVERSARY_T_DAY_STEP_7_CALLBACK_CONFIRMED");
////                    session.setLastActivity(LocalDateTime.now());
////                    botSessionRepository.save(session);
////
////                    scheduleAnniversaryTDayStep8(phone, customerId, firstName);
////                    return;
////                }
////
////                session.setCurrentStep("ANNIVERSARY_T_DAY_CALLBACK_CONFIRMED");
////                session.setLastActivity(LocalDateTime.now());
////                botSessionRepository.save(session);
////
////                scheduleAnniversaryTDayStep7(phone, customerId, firstName);
////                return;
////            }
////
////
////
////            if ("ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)
////                    || "ANNIVERSARY_STORE_VISIT_SENT".equalsIgnoreCase(previousStep)
////                    || "ANNIVERSARY_CATALOGUE_SENT".equalsIgnoreCase(previousStep)
////                    || "ANNIVERSARY_COUPLE_CATALOGUE_SENT".equalsIgnoreCase(previousStep)) {
////
////                sendAnniversaryCallbackConfirmation(phone);
////
////                session.setCurrentStep("ANNIVERSARY_CALLBACK_CONFIRMED");
////                session.setLastActivity(LocalDateTime.now());
////                botSessionRepository.save(session);
////                return;
////            }
////
////
////
////
////            sendCallbackConfirmation(phone);
////
////            // Step 7 ke baad user callback click kare,
////            // then Step 8 scheduler trigger hoga.
////            if ("STEP_7_SENT".equalsIgnoreCase(previousStep)
////                    || "STEP_7_STORE_SENT".equalsIgnoreCase(previousStep)) {
////
////                session.setCurrentStep("STEP_7_CALLBACK_CONFIRMED");
////                session.setLastActivity(LocalDateTime.now());
////                botSessionRepository.save(session);
////
////                scheduleStep8(phone, customerId, firstName);
////                return;
////            }
////
////            // T-Day Step 6b ke baad callback click kare,
////            // then Step 7 scheduler trigger hoga.
////            session.setCurrentStep("CALLBACK_CONFIRMED");
////            session.setLastActivity(LocalDateTime.now());
////            botSessionRepository.save(session);
////
////            if ("STEP_6B_SENT".equalsIgnoreCase(previousStep)
////                    || "T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(previousStep)
////                    || "BIRTHDAY_WISH_SENT".equalsIgnoreCase(previousStep)) {
////
////                scheduleStep7(phone, customerId, firstName);
////            }
////
////            return;
////        }
//
//
//
//
//
//
//        if ("REQUEST_CALLBACK".equals(cleanPayload)
//                || cleanPayload.startsWith("REQUEST_CALLBACK_")
//                || "REQUEST_A_CALLBACK".equals(cleanPayload)
//                || "BOOK_AN_APPOINTMENT".equals(cleanPayload)
//                || "INTERESTED".equals(cleanPayload)
//                || "SPEAK_WITH_EXPERT".equals(cleanPayload)
//                || "ANNIV_REQUEST_CALLBACK".equals(cleanPayload)) {
//
//            String previousStep = session.getCurrentStep();
//
//
//
//            // ----------------------------------------------------
//// ANNIVERSARY OFFER PATH - Callback
//// ----------------------------------------------------
//            if ("ANNIV_T10_CALLBACK".equals(cleanPayload)) {
//
//                sendAnniversaryCallbackConfirmation(phone);
//
//                session.setCurrentStep("ANNIVERSARY_CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//
//            // ----------------------------------------------------
//// Anniversary T-10 Step 4a Request Callback
//// Request callback pe direct Done nahi bhejna.
//// Pehle Book Appointment option dikhana hai.
//// ----------------------------------------------------
//            if ("REQUEST_CALLBACK".equals(cleanPayload)
//                    && "ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)) {
//
//                sendAnniversaryStoreHelpMessage(phone);
//
//                session.setCurrentStep("ANNIVERSARY_STORE_VISIT_SENT");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//
//            if ("ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)
//                    || "ANNIVERSARY_STORE_VISIT_SENT".equalsIgnoreCase(previousStep)
//                    || "ANNIVERSARY_CATALOGUE_SENT".equalsIgnoreCase(previousStep)
//                    || "ANNIVERSARY_COUPLE_CATALOGUE_SENT".equalsIgnoreCase(previousStep)) {
//
//                sendAnniversaryCallbackConfirmation(phone);
//
//                session.setCurrentStep("ANNIVERSARY_CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//            if ("ANNIV_REQUEST_CALLBACK".equals(cleanPayload)
//                    || (
//                    "REQUEST_CALLBACK".equals(cleanPayload)
//                            && isAnniversaryTDayFlowStep(previousStep)
//            )) {
//
//                sendAnniversaryTDayCallbackConfirmation(phone);
//
//                if ("ANNIVERSARY_T_DAY_STEP_7_SENT".equalsIgnoreCase(previousStep)
//                        || "ANNIVERSARY_T_DAY_STEP_7_STORE_SENT".equalsIgnoreCase(previousStep)) {
//
//                    session.setCurrentStep("ANNIVERSARY_T_DAY_STEP_7_CALLBACK_CONFIRMED");
//                    session.setLastActivity(LocalDateTime.now());
//                    botSessionRepository.save(session);
//
//                    scheduleAnniversaryTDayStep8(phone, customerId, firstName);
//                    return;
//                }
//
//                session.setCurrentStep("ANNIVERSARY_T_DAY_CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                scheduleAnniversaryTDayStep7(phone, customerId, firstName);
//                return;
//            }
//
////            sendCallbackConfirmation(phone);
//
//
//            // ----------------------------------------------------
//// Birthday T-Day Request Callback
//// Step 6c: send confirmation first
//// Step 7: schedule after 4-6 hours
//// Testing: currently STEP_7_DELAY_SECONDS = 10 seconds
//// ----------------------------------------------------
//            if ("REQUEST_CALLBACK".equals(cleanPayload)
//                    && (
//                    "STEP_6B_SENT".equalsIgnoreCase(previousStep)
//                            || "T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(previousStep)
//                            || "BIRTHDAY_WISH_SENT".equalsIgnoreCase(previousStep)
//            )) {
//
//                sendCallbackConfirmation(phone);
//
//                session.setCurrentStep("CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                scheduleStep7(phone, customerId, firstName);
//                return;
//            }
//
//// ----------------------------------------------------
//// Birthday T-Day Step 7 Request Callback
//// Step 7b: send confirmation first
//// Step 8: schedule after 4 hours
//// Testing: currently STEP_8_DELAY_SECONDS = 10 seconds
//// ----------------------------------------------------
//            if ("REQUEST_CALLBACK".equals(cleanPayload)
//                    && (
//                    "STEP_7_SENT".equalsIgnoreCase(previousStep)
//                            || "STEP_7_STORE_SENT".equalsIgnoreCase(previousStep)
//            )) {
//
//                sendCallbackConfirmation(phone);
//
//                session.setCurrentStep("STEP_7_CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                scheduleStep8(phone, customerId, firstName);
//                return;
//            }
//
//
//            if ("BOOK_AN_APPOINTMENT".equals(cleanPayload)
//                    || "STORE_VISIT_SENT".equalsIgnoreCase(previousStep)
//                    || "CATALOGUE_FOLLOW_UP_SENT".equalsIgnoreCase(previousStep)) {
//
//                sendCallbackConfirmationWithExplore(phone);
//
//                session.setCurrentStep("CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//            if ("STEP_7_SENT".equalsIgnoreCase(previousStep)
//                    || "STEP_7_STORE_SENT".equalsIgnoreCase(previousStep)) {
//
//                session.setCurrentStep("STEP_7_CALLBACK_CONFIRMED");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//
//                scheduleStep8(phone, customerId, firstName);
//                return;
//            }
//
//            session.setCurrentStep("CALLBACK_CONFIRMED");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//
//            if ("STEP_6B_SENT".equalsIgnoreCase(previousStep)
//                    || "T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(previousStep)
//                    || "BIRTHDAY_WISH_SENT".equalsIgnoreCase(previousStep)) {
//
//                scheduleStep7(phone, customerId, firstName);
//            }
//
//            return;
//        }
//
//
//        // ----------------------------------------------------
//        // Explore again -> Step 2
//        // ----------------------------------------------------
//        if ("EXPLORE_AGAIN".equals(cleanPayload)
//                || "BROWSE_AGAIN".equals(cleanPayload)
//                || "EXPLORE_OTHER_STYLES".equals(cleanPayload)) {
//
//            if ("ANNIVERSARY_CALLBACK_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())) {
//                sendAnniversaryGenderSelection(phone);
//
//                session.setCurrentStep("ANNIVERSARY_GENDER_SELECTION");
//                session.setLastActivity(LocalDateTime.now());
//                botSessionRepository.save(session);
//                return;
//            }
//
//            sendGenderSelection(phone);
//
//            session.setCurrentStep("GENDER_SELECTION");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // STEP 8 Visit us anytime
//        // ----------------------------------------------------
//        if ("VISIT_US_ANYTIME".equals(cleanPayload)) {
//
//            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
//
//            session.setCurrentStep("FLOW_ENDED");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        // ----------------------------------------------------
//        // OLD EXISTING FUNCTIONALITY FALLBACK
//        // Agar koi old payload aata hai toh break nahi hoga.
//        // ----------------------------------------------------
//
//        if (cleanPayload.isBlank()
//                || "HI".equals(cleanPayload)
//                || "HELLO".equals(cleanPayload)
//                || "START".equals(cleanPayload)) {
//
//            handleWelcome(phone, customerName, session);
//            return;
//        }
//
//        if ("STYLE_MINIMAL_CHIC".equals(cleanPayload)) {
//            handleStyleSelected(phone, session, "STYLE_MINIMAL_CHIC", "Minimal & Chic");
//            return;
//        }
//
//        if ("STYLE_BOLD_EDGY".equals(cleanPayload)) {
//            handleStyleSelected(phone, session, "STYLE_BOLD_EDGY", "Bold & Edgy");
//            return;
//        }
//
//        if ("STYLE_LUXE_CLASSY".equals(cleanPayload)) {
//            handleStyleSelected(phone, session, "STYLE_LUXE_CLASSY", "Luxe & Classy");
//            return;
//        }
//
//        if ("STYLE_SPORTY_ADVENTUROUS".equals(cleanPayload)) {
//            handleStyleSelected(phone, session, "STYLE_SPORTY_ADVENTUROUS", "Sporty & Adventurous");
//            return;
//        }
//
//        if ("SEE_BY_PRICE".equals(cleanPayload)) {
//            sendPriceSelection(phone);
//
//            String collectionType = getCollectionFromSession(session);
//            String style = getStyleFromSession(session);
//
//            session.setCurrentStep("PRICE_SELECTION_" + collectionType + "_" + style);
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        if ("PRICE_BELOW_10K".equals(cleanPayload)
//                || "PRICE_2K_5K".equals(cleanPayload)
//                || "PRICE_5K_10K".equals(cleanPayload)
//                || "PRICE_10K_25K".equals(cleanPayload)
//                || "PRICE_25K_PLUS".equals(cleanPayload)) {
//
//            handlePriceSelected(phone, session, cleanPayload);
//



package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.entity.BrandCarouselCard;
import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.repository.*;
import com.example.titan_watch_learning_project.service.BotEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotEngineServiceImpl implements BotEngineService {

    private final MessageRepository messageRepository;
    private final BotSessionRepository botSessionRepository;
    private final KarixApiServiceImpl karixApiService;
    private final CustomerRepository customerRepository;
    private final TaskScheduler taskScheduler;
    private final BrandCarouselCardRepository brandCarouselCardRepository;

    private final LeadRepository leadRepository;

    // ---------------------------------------------------------------------
    // URLs / Delays
    // ---------------------------------------------------------------------
    private static final String STORE_LOCATOR_URL = "https://www.titan.co.in/store-locator";

    private static final String SAMPLE_ANNIVERSARY_PDF_URL =
            "https://titanwatchimages123.blob.core.windows.net/watch-images/watchs_products_images/Moodshoot%20Pdf%20Anniversary.pdf";

    // Testing values. Production me inko real delay se replace kar dena.
    private static final long BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_DELAY_SECONDS = 5;
    private static final long ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_DELAY_SECONDS = 4;
    private static final long ANNIVERSARY_T10_DATE_BRIDGE_DELAY_SECONDS = 4;


    private static final long BIRTHDAY_TDAY_STORE_HELP_DELAY_SECONDS = 4;
    private static final long BIRTHDAY_TDAY_FINAL_REMINDER_DELAY_SECONDS = 4;
    private static final long BIRTHDAY_TDAY_EXIT_DELAY_SECONDS = 4;


    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);

    // ---------------------------------------------------------------------
    // Step names - meaningful names only for new saves.
    // ---------------------------------------------------------------------

    private static final String STEP_WELCOME = "WELCOME";
    private static final String STEP_WELCOME_SENT = "WELCOME_SENT";

    // Birthday T-10
    private static final String STEP_BIRTHDAY_T10_CONFIRMATION_SENT = "BIRTHDAY_T10_CONFIRMATION_SENT";
    private static final String STEP_BIRTHDAY_T10_DOB_PENDING = "BIRTHDAY_T10_DOB_CORRECTION_PENDING";
    private static final String STEP_BIRTHDAY_T10_BRIDGE_SENT = "BIRTHDAY_T10_BRIDGE_SENT";
    private static final String STEP_BIRTHDAY_T10_OPENER_SENT = "BIRTHDAY_T10_OPENER_SENT";
    private static final String STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT = "BIRTHDAY_T10_GENDER_SELECTION_SENT";
    private static final String STEP_BIRTHDAY_T10_MEN_BRAND_CAROUSEL_SENT = "BIRTHDAY_T10_MEN_BRAND_CAROUSEL_SENT";
    private static final String STEP_BIRTHDAY_T10_WOMEN_BRAND_CAROUSEL_SENT = "BIRTHDAY_T10_WOMEN_BRAND_CAROUSEL_SENT";
    private static final String STEP_BIRTHDAY_T10_CATALOGUE_SENT = "BIRTHDAY_T10_CATALOGUE_SENT";
    private static final String STEP_BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_SENT = "BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_SENT";
    private static final String STEP_BIRTHDAY_T10_OFFER_SENT = "BIRTHDAY_T10_OFFER_SENT";
    private static final String STEP_BIRTHDAY_T10_STORE_VISIT_SENT = "BIRTHDAY_T10_STORE_VISIT_SENT";
    private static final String STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED = "BIRTHDAY_T10_CALLBACK_CONFIRMED";
    private static final String STEP_BIRTHDAY_T10_GRACEFUL_EXIT = "BIRTHDAY_T10_GRACEFUL_EXIT";

    // Birthday T-Day / T-0
    private static final String STEP_BIRTHDAY_TDAY_TEMPLATE_SENT = "BIRTHDAY_TDAY_TEMPLATE_SENT";
    private static final String STEP_BIRTHDAY_TDAY_WISH_SENT = "BIRTHDAY_TDAY_WISH_SENT";
    private static final String STEP_BIRTHDAY_TDAY_STORE_LOCATOR_SENT = "BIRTHDAY_TDAY_STORE_LOCATOR_SENT";
    private static final String STEP_BIRTHDAY_TDAY_STORE_HELP_SENT = "BIRTHDAY_TDAY_STORE_HELP_SENT";
    private static final String STEP_BIRTHDAY_TDAY_CALLBACK_CONFIRMED = "BIRTHDAY_TDAY_CALLBACK_CONFIRMED";
    private static final String STEP_BIRTHDAY_TDAY_FINAL_REMINDER_SENT = "BIRTHDAY_TDAY_FINAL_REMINDER_SENT";
    private static final String STEP_BIRTHDAY_TDAY_FINAL_REMINDER_STORE_SENT = "BIRTHDAY_TDAY_FINAL_REMINDER_STORE_SENT";
    private static final String STEP_BIRTHDAY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED = "BIRTHDAY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED";
    private static final String STEP_BIRTHDAY_TDAY_EXIT_MESSAGE_SENT = "BIRTHDAY_TDAY_EXIT_MESSAGE_SENT";
    private static final String STEP_BIRTHDAY_TDAY_FLOW_COMPLETED = "BIRTHDAY_TDAY_FLOW_COMPLETED";
    // Anniversary T-10
    private static final String STEP_ANNIVERSARY_T10_CONFIRMATION_SENT = "ANNIVERSARY_T10_CONFIRMATION_SENT";
    private static final String STEP_ANNIVERSARY_T10_DATE_PENDING = "ANNIVERSARY_T10_DATE_CORRECTION_PENDING";
    private static final String STEP_ANNIVERSARY_T10_BRIDGE_SENT = "ANNIVERSARY_T10_BRIDGE_SENT";
    private static final String STEP_ANNIVERSARY_T10_OPENER_SENT = "ANNIVERSARY_T10_OPENER_SENT";
    private static final String STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT = "ANNIVERSARY_T10_GENDER_SELECTION_SENT";
    private static final String STEP_ANNIVERSARY_T10_MEN_BRAND_CAROUSEL_SENT = "ANNIVERSARY_T10_MEN_BRAND_CAROUSEL_SENT";
    private static final String STEP_ANNIVERSARY_T10_WOMEN_BRAND_CAROUSEL_SENT = "ANNIVERSARY_T10_WOMEN_BRAND_CAROUSEL_SENT";
    private static final String STEP_ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT = "ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT";
    private static final String STEP_ANNIVERSARY_T10_CATALOGUE_SENT = "ANNIVERSARY_T10_CATALOGUE_SENT";
    private static final String STEP_ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_SENT = "ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_SENT";
    private static final String STEP_ANNIVERSARY_T10_OFFER_SENT = "ANNIVERSARY_T10_OFFER_SENT";
    private static final String STEP_ANNIVERSARY_T10_STORE_VISIT_SENT = "ANNIVERSARY_T10_STORE_VISIT_SENT";
    private static final String STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED = "ANNIVERSARY_T10_CALLBACK_CONFIRMED";
    private static final String STEP_ANNIVERSARY_T10_GRACEFUL_EXIT = "ANNIVERSARY_T10_GRACEFUL_EXIT";

    // Anniversary T-Day / T-0
    private static final String STEP_ANNIVERSARY_TDAY_TEMPLATE_SENT = "ANNIVERSARY_TDAY_TEMPLATE_SENT";
    private static final String STEP_ANNIVERSARY_TDAY_WISH_SENT = "ANNIVERSARY_TDAY_WISH_SENT";
    private static final String STEP_ANNIVERSARY_TDAY_STORE_LOCATOR_SENT = "ANNIVERSARY_TDAY_STORE_LOCATOR_SENT";
    private static final String STEP_ANNIVERSARY_TDAY_STORE_HELP_SENT = "ANNIVERSARY_TDAY_STORE_HELP_SENT";
    private static final String STEP_ANNIVERSARY_TDAY_CALLBACK_CONFIRMED = "ANNIVERSARY_TDAY_CALLBACK_CONFIRMED";
    private static final String STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_SENT = "ANNIVERSARY_TDAY_FINAL_REMINDER_SENT";
    private static final String STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_STORE_SENT = "ANNIVERSARY_TDAY_FINAL_REMINDER_STORE_SENT";
    private static final String STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED = "ANNIVERSARY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED";
    private static final String STEP_ANNIVERSARY_TDAY_FLOW_ENDED = "ANNIVERSARY_TDAY_FLOW_ENDED";


    private static final long ANNIVERSARY_TDAY_STORE_HELP_DELAY_SECONDS = 4;
    private static final long ANNIVERSARY_TDAY_FINAL_REMINDER_DELAY_SECONDS = 4;
    private static final long ANNIVERSARY_TDAY_EXIT_DELAY_SECONDS = 4;

    private static final String STEP_ANNIVERSARY_TDAY_EXIT_MESSAGE_SENT =
            "ANNIVERSARY_TDAY_EXIT_MESSAGE_SENT";

    private static final String STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED =
            "ANNIVERSARY_TDAY_FLOW_COMPLETED";

    // old compatibility ke liye rehne do, but new save me use mat karo

    private static final String BIRTHDAY_CATALOGUE_PDF_URL =
            "https://titanwatchimages123.blob.core.windows.net/watch-images/watchs_products_images/Moodshoot%20Pdf%20Birthday.pdf";

    private static final String ANNIVERSARY_CATALOGUE_PDF_URL =
            "https://titanwatchimages123.blob.core.windows.net/watch-images/watchs_products_images/Moodshoot%20Pdf%20Anniversary.pdf";


    private static final String STEP_BIRTHDAY_T10_BRIDGE_PENDING = "BIRTHDAY_T10_BRIDGE_PENDING";
    private static final long BIRTHDAY_T10_DOB_BRIDGE_DELAY_SECONDS = 5;


    private static final String STEP_ANNIVERSARY_T10_BRIDGE_PENDING =
            "ANNIVERSARY_T10_BRIDGE_PENDING";

    // ---------------------------------------------------------------------
    // Main entry point
    // ---------------------------------------------------------------------


    private void saveLead(
            String phone,
            Long customerId,
            String customerName,
            BotSession session,
            Lead.LeadType leadType,
            String notes
    ) {
        try {
            if (phone == null || phone.isBlank()) {
                log.warn("Lead not saved because phone is blank. leadType={} notes={}", leadType, notes);
                return;
            }

            String selectedCollection = null;
            String selectedStyle = null;
            String priceRange = null;

            if (session != null) {
                selectedCollection = session.getSelectedCollection() == null
                        ? null
                        : session.getSelectedCollection().name();

                selectedStyle = session.getSelectedBrand();
                priceRange = session.getSelectedPriceRange();
            }

            Lead lead = Lead.builder()
                    .customerId(customerId)
                    .phone(phone)
                    .customerName(customerName)
                    .leadType(leadType)
                    .status(Lead.LeadStatus.NEW)
                    .selectedCollection(selectedCollection)
                    .selectedStyle(selectedStyle)
                    .priceRange(priceRange)
                    .notes(notes)
                    .build();

            if (customerId != null) {
                customerRepository.findById(customerId).ifPresent(customer -> {
                    lead.setStoreCode(customer.getStoreCode());

                    if (lead.getCustomerName() == null || lead.getCustomerName().isBlank()) {
                        lead.setCustomerName(customer.getName());
                    }
                });
            }

            leadRepository.save(lead);

            log.info("Lead saved phone={} type={} collection={} style={} notes={}",
                    phone, leadType, selectedCollection, selectedStyle, notes);

        } catch (Exception e) {
            log.error("Lead save failed but bot flow will continue. phone={} type={} notes={}",
                    phone, leadType, notes, e);
        }
    }


    private boolean handleBirthdayTDayExitOrCompletedState(
            String phone,
            BotSession session,
            String cleanPayload
    ) {
        String currentStep = safeStep(session);

        // Exit message sent hai:
        // Sirf "Visit us anytime" allowed hai.
        // Baaki upper old buttons ignore.
        if (isBirthdayTDayExitMessageStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "BIRTHDAY_TDAY_VISIT_ANYTIME",
                    "VISIT_US_ANYTIME")) {

                karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
                saveStep(session, STEP_BIRTHDAY_TDAY_FLOW_COMPLETED);
                return true;
            }

            log.info("Ignored old Birthday T-Day button after exit message phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        // Flow completed hai:
        // Ab koi bhi old upper button kuch trigger nahi karega.
        if (isBirthdayTDayCompletedStep(currentStep)) {
            log.info("Ignored old Birthday T-Day button after flow completed phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        return false;
    }

    private boolean isAnniversaryTDayExitMessageStep(String step) {
        return isStep(step,
                STEP_ANNIVERSARY_TDAY_EXIT_MESSAGE_SENT,
                "ANNIVERSARY_TDAY_EXIT_MESSAGE_SENT"
        );
    }

    private boolean isAnniversaryTDayCompletedStep(String step) {
        return isStep(step,
                STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED,
                "ANNIVERSARY_TDAY_FLOW_COMPLETED",
                STEP_ANNIVERSARY_TDAY_FLOW_ENDED,
                "ANNIVERSARY_TDAY_FLOW_ENDED"
        );
    }

    private boolean handleAnniversaryTDayExitOrCompletedState(
            String phone,
            BotSession session,
            String cleanPayload
    ) {
        String currentStep = safeStep(session);

        // Exit message sent hai:
        // Sirf Visit us anytime allowed hai.
        // Baaki upper old buttons ignore.
        if (isAnniversaryTDayExitMessageStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_TDAY_VISIT_ANYTIME",
                    "ANNIV_VISIT_US_ANYTIME",
                    "VISIT_US_ANYTIME")) {

                karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
                saveStep(session, STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED);
                return true;
            }

            log.info("Ignored old Anniversary T-Day button after exit message phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        // Flow completed hai:
        // Ab koi old upper button kuch trigger nahi karega.
        if (isAnniversaryTDayCompletedStep(currentStep)) {
            log.info("Ignored old Anniversary T-Day button after flow completed phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        return false;
    }
    @Override
    public void processIncomingMessage(
            String phone,
            String text,
            String payload,
            Long customerId,
            String customerName
    ) {
        log.info("processMessage phone={} payload={} text={}", phone, payload, text);

        BotSession session = getOrCreateSession(phone, customerId);
        String currentStep = safeStep(session);
        saveIncomingMessage(phone, text, payload, customerId, currentStep);

        String firstName = getFirstName(customerName);
        String cleanPayload = getCleanPayload(text, payload);

        log.info("Routing phone={} currentStep={} cleanPayload={}", phone, currentStep, cleanPayload);

        // Manual/template starters must run before date-pending blocks.
        // This prevents stuck sessions such as DATE_PENDING blocking Confirm Details / Anniversary Day - T.
        if (startBirthdayT10Flow(phone, session, firstName, cleanPayload)) return;
        if (startBirthdayTDayFlow(phone, session, firstName, cleanPayload)) return;
        if (startAnniversaryT10Flow(phone, session, firstName, customerId, cleanPayload)) return;
        if (startAnniversaryTDayFlow(phone, session, firstName, cleanPayload)) return;

        // Pending date corrections.
// Pending date corrections.
        if (handleBirthdayT10DobCorrection(phone, session, firstName, customerId, text)) return;
        if (handleAnniversaryT10DateCorrection(phone, session, firstName, customerId, text)) return;

// ----------------------------------------------------
// Birthday T-Day exit/completed guard
// Must run BEFORE Birthday T-10 router.
// Exit ke baad old upper buttons ignore karne hain.
// ----------------------------------------------------

        if (handleBirthdayTDayExitOrCompletedState(phone, session, cleanPayload)) return;

// ----------------------------------------------------
// Anniversary T-Day exit/completed guard
// Must run before all routers.
// Exit ke baad upper old buttons ignore karne hain.
// ----------------------------------------------------
        if (handleAnniversaryTDayExitOrCompletedState(phone, session, cleanPayload)) return;

// Four clearly separated flow routers.
        if (handleBirthdayT10Flow(phone, session, firstName, cleanPayload)) return;
        if (handleBirthdayTDayFlow(phone, session, firstName, cleanPayload, customerId)) return;
        if (handleAnniversaryT10Flow(phone, session, firstName, cleanPayload, customerId)) return;
        if (handleAnniversaryTDayFlow(phone, session, firstName, cleanPayload, customerId)) return;









        if (handleStaleKnownPayloadFallback(phone, session, firstName, cleanPayload, customerId)) return;

        if (isAnyActiveFlowStep(safeStep(session))) {
            log.info("Ignored unknown payload inside active flow phone={} currentStep={} payload={}",
                    phone, safeStep(session), cleanPayload);
            return;
        }

        log.info("No matching payload found. Starting welcome flow phone={} cleanPayload={}", phone, cleanPayload);
        handleWelcome(phone, customerName, session);
    }




    private boolean isBirthdayT10AnyStep(String step) {
        return isStep(step,
                STEP_BIRTHDAY_T10_CONFIRMATION_SENT,
                STEP_BIRTHDAY_T10_DOB_PENDING,
                STEP_BIRTHDAY_T10_BRIDGE_SENT,
                STEP_BIRTHDAY_T10_OPENER_SENT,
                STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT,
                STEP_BIRTHDAY_T10_MEN_BRAND_CAROUSEL_SENT,
                STEP_BIRTHDAY_T10_WOMEN_BRAND_CAROUSEL_SENT,
                STEP_BIRTHDAY_T10_CATALOGUE_SENT,
                STEP_BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_SENT,
                STEP_BIRTHDAY_T10_OFFER_SENT,
                STEP_BIRTHDAY_T10_STORE_VISIT_SENT,
                STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED,
                STEP_BIRTHDAY_T10_GRACEFUL_EXIT,

                // legacy compatibility
                "BIRTHDAY_CONFIRM_DETAILS",
                "DOB_CORRECTION_PENDING",
                "DOB_CONFIRMED",
                "BIRTHDAY_MONTH_CONFIRMED",
                "CHATBOT_OPENER",
                "GENDER_SELECTION",
                "MEN_BRAND_CAROUSEL",
                "WOMEN_BRAND_CAROUSEL",
                "CATALOGUE_SENT",
                "BIRTHDAY_OFFER_PATH",
                "STORE_VISIT_SENT",
                "CALLBACK_CONFIRMED",
                "CALLBACK_REQUESTED"
        );
    }

    private boolean isAnniversaryT10AnyStep(String step) {
        return isStep(step,
                STEP_ANNIVERSARY_T10_CONFIRMATION_SENT,
                STEP_ANNIVERSARY_T10_DATE_PENDING,
                STEP_ANNIVERSARY_T10_BRIDGE_SENT,
                STEP_ANNIVERSARY_T10_OPENER_SENT,
                STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT,
                STEP_ANNIVERSARY_T10_MEN_BRAND_CAROUSEL_SENT,
                STEP_ANNIVERSARY_T10_WOMEN_BRAND_CAROUSEL_SENT,
                STEP_ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT,
                STEP_ANNIVERSARY_T10_CATALOGUE_SENT,
                STEP_ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_SENT,
                STEP_ANNIVERSARY_T10_OFFER_SENT,
                STEP_ANNIVERSARY_T10_STORE_VISIT_SENT,
                STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED,
                STEP_ANNIVERSARY_T10_GRACEFUL_EXIT,
                STEP_ANNIVERSARY_T10_BRIDGE_PENDING,

                // legacy compatibility
                "ANNIVERSARY_CONFIRMATION_SENT",
                "ANNIVERSARY_DATE_PENDING",
                "ANNIVERSARY_BRIDGE_SENT",
                "ANNIVERSARY_MONTH_CONFIRMED",
                "ANNIVERSARY_DATE_CONFIRMED",
                "ANNIVERSARY_OPENER",
                "ANNIVERSARY_GENDER_SELECTION",
                "ANNIVERSARY_MEN_BRAND_CAROUSEL",
                "ANNIVERSARY_WOMEN_BRAND_CAROUSEL",
                "ANNIVERSARY_COUPLE_CATALOGUE_SENT",
                "ANNIVERSARY_CATALOGUE_SENT",
                "ANNIVERSARY_STEP_4A_SENT",
                "ANNIVERSARY_OFFERS_SENT",
                "ANNIVERSARY_STORE_VISIT_SENT",
                "ANNIVERSARY_CALLBACK_CONFIRMED"
        );
    }

    private boolean isAnyActiveFlowStep(String step) {
        return isBirthdayT10AnyStep(step)
                || isBirthdayTDayActiveStep(step)
                || isBirthdayTDayExitMessageStep(step)
                || isBirthdayTDayCompletedStep(step)
                || isAnniversaryT10AnyStep(step)
                || isAnniversaryTDayActiveStep(step)
                || isAnniversaryTDayExitMessageStep(step)
                || isAnniversaryTDayCompletedStep(step);
    }

    private boolean blockWelcomeFallbackInsideActiveFlow(
            String phone,
            BotSession session,
            String cleanPayload
    ) {
        String currentStep = safeStep(session);

        if (!isAnyActiveFlowStep(currentStep)) {
            return false;
        }

        // Manual starters ko block mat karo.
        if (isPayload(cleanPayload,
                "CONFIRM_DETAILS",
                "CONFIRM_DETAIL",
                "CONFIRM",
                "HAPPY_BIRTHDAY",
                "HAPPY_BDAY",
                "BIRTHDAY_DAY",
                "ANNIVERSARY",
                "ANNIVARY",
                "ANNIVERSARY_FLOW",
                "ANNIVERSARY_DAY_T",
                "ANNIVERSARY_T_DAY",
                "ANNIVERSARY_DAY",
                "HAPPY_ANNIVERSARY",
                "ANNIVERSARY_DAY_FLOW")) {
            return false;
        }

        // Normal greeting ko bhi optional welcome allow kar sakte ho.
        if (isPayload(cleanPayload, "HI", "HELLO", "START")) {
            return false;
        }

        log.info("Blocked welcome fallback inside active flow phone={} currentStep={} payload={}",
                phone, currentStep, cleanPayload);

        karixApiService.sendTextMessage(
                phone,
                "Please choose one of the options shown above, or type *Confirm details*, *Happy birthday*, *Anniversary*, or *Anniversary Day - T* to start again."
        );

        return true;
    }

    // ---------------------------------------------------------------------
    // Flow starters - only these 4 open the 4 main flows.
    // ---------------------------------------------------------------------

    private boolean startBirthdayT10Flow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        if (!isPayload(cleanPayload, "CONFIRM_DETAILS", "CONFIRM_DETAIL", "CONFIRM")) {
            return false;
        }

        sendBirthdayT10MonthConfirmation(phone, firstName);
        saveStep(session, STEP_BIRTHDAY_T10_CONFIRMATION_SENT);
        return true;
    }

    private boolean startBirthdayTDayFlow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        if (!isPayload(cleanPayload, "HAPPY_BIRTHDAY", "HAPPY_BDAY", "BIRTHDAY_DAY")) {
            return false;
        }

        sendBirthdayTDayBenefitIntro(phone, firstName);
        saveStep(session, STEP_BIRTHDAY_TDAY_TEMPLATE_SENT);
        return true;
    }

    private boolean startAnniversaryT10Flow(
            String phone,
            BotSession session,
            String firstName,
            Long customerId,
            String cleanPayload
    ) {
        if (!isPayload(cleanPayload, "ANNIVERSARY", "ANNIVARY", "ANNIVERSARY_FLOW")) {
            return false;
        }

        sendAnniversaryT10MonthConfirmation(phone, firstName, getAnniversaryMonthForMessage(customerId));
        saveStep(session, STEP_ANNIVERSARY_T10_CONFIRMATION_SENT);
        return true;
    }

    private boolean startAnniversaryTDayFlow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        if (!isPayload(
                cleanPayload,
                "ANNIVERSARY_DAY_T",
                "ANNIVERSARY_T_DAY",
                "ANNIVERSARY_DAY",
                "HAPPY_ANNIVERSARY",
                "ANNIVERSARY_DAY_FLOW"
        )) {
            return false;
        }

        sendAnniversaryTDayBenefitIntro(phone, firstName);
        saveStep(session, STEP_ANNIVERSARY_TDAY_TEMPLATE_SENT);
        return true;
    }

    // ---------------------------------------------------------------------
    // Birthday T-10 Flow
    // Confirm details -> month confirmation -> bridge -> opener -> discovery / offer.
    // ---------------------------------------------------------------------
//    private boolean isBirthdayT10AnyStep(String step) {
//        return isStep(step,
//                STEP_BIRTHDAY_T10_CONFIRMATION_SENT,
//                STEP_BIRTHDAY_T10_DOB_PENDING,
//                STEP_BIRTHDAY_T10_BRIDGE_PENDING,
//                STEP_BIRTHDAY_T10_BRIDGE_SENT,
//                STEP_BIRTHDAY_T10_OPENER_SENT,
//                STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT,
//                STEP_BIRTHDAY_T10_MEN_BRAND_CAROUSEL_SENT,
//                STEP_BIRTHDAY_T10_WOMEN_BRAND_CAROUSEL_SENT,
//                STEP_BIRTHDAY_T10_CATALOGUE_SENT,
//                STEP_BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_SENT,
//                STEP_BIRTHDAY_T10_OFFER_SENT,
//                STEP_BIRTHDAY_T10_STORE_VISIT_SENT,
//                STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED,
//                STEP_BIRTHDAY_T10_GRACEFUL_EXIT
//        );
//    }
    private boolean handleBirthdayT10Flow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        String currentStep = safeStep(session);

        if (isBirthdayT10MonthConfirmationStep(currentStep)) {
            if (isPayload(cleanPayload, "BIRTHDAY_MONTH_YES", "YES_THAT_S_CORRECT", "YES_THATS_CORRECT", "YES_CORRECT")) {
                sendBirthdayT10Opener(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_T10_OPENER_SENT);
                return true;
            }
            if (isPayload(cleanPayload, "BIRTHDAY_MONTH_NO", "NO_THAT_S_NOT_RIGHT", "NO_THATS_NOT_RIGHT", "NO_NOT_RIGHT")) {

                sendBirthdayT10DobCorrectionAsk(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_T10_DOB_PENDING);
                return true;
            }
        }


        if (isBirthdayT10BridgeStep(currentStep)) {
            if (isPayload(cleanPayload, "BIRTHDAY_T10_YES_SHOW_ME", "YES_SHOW_ME", "SHOW_ME")) {
                sendBirthdayT10Opener(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_T10_OPENER_SENT);
                return true;
            }

            if (isPayload(cleanPayload, "BIRTHDAY_T10_MAYBE_LATER", "NO_MAYBE_LATER", "MAYBE_LATER")) {
                sendBirthdayT10GracefulExit(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_T10_GRACEFUL_EXIT);
                return true;
            }
            if (isPayload(cleanPayload, "NO_MAYBE_LATER", "MAYBE_LATER")) {
                sendBirthdayT10GracefulExit(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_T10_GRACEFUL_EXIT);
                return true;
            }
        }

        if (isBirthdayT10OpenerStep(currentStep)) {
            if (isPayload(cleanPayload, "FIND_MY_PERFECT_WATCH", "FIND_PERFECT_WATCH", "FIND_BIRTHDAY_WATCH", "FIND_WATCH", "FIND_MY_WATCH", "FIND_MY_BIRTHDAY_WATCH")) {
                sendBirthdayT10GenderSelection(phone);
                saveStep(session, STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT);
                return true;
            }

            if (isPayload(cleanPayload, "SEE_BIRTHDAY_OFFERS", "BIRTHDAY_OFFERS", "EXCLUSIVE_BIRTHDAY_OFFERS")) {
                sendBirthdayT10Offer(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_T10_OFFER_SENT);
                return true;
            }


            if (isStep(currentStep, STEP_BIRTHDAY_T10_STORE_VISIT_SENT, "STORE_VISIT_SENT")) {

                if (isPayload(cleanPayload,
                        "BIRTHDAY_T10_BOOK_APPOINTMENT",
                        "BOOK_AN_APPOINTMENT")) {

                    sendBirthdayT10CallbackConfirmation(phone);
                    saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
                    return true;
                }
            }
        }

        if (isBirthdayT10GenderSelectionStep(currentStep)) {
            if (isMensCollectionPayload(cleanPayload)) {
                return sendBirthdayT10BrandCarousel(phone, session, firstName, "MEN");
            }

            if (isWomensCollectionPayload(cleanPayload)) {
                return sendBirthdayT10BrandCarousel(phone, session, firstName, "WOMEN");
            }
        }

        if (isBirthdayT10BrandCarouselStep(currentStep)) {
            if (isExploreCarouselPayload(cleanPayload)) {
                return handleBirthdayT10ExploreCollection(phone, session, firstName, cleanPayload);
            }

            if (isCallbackCarouselPayload(cleanPayload)) {
                return handleBirthdayT10CarouselCallback(phone, session, firstName,cleanPayload);
            }
        }

//        if (isBirthdayT10CatalogueOrOfferStep(currentStep)) {
//
//            if (isPayload(cleanPayload, "BIRTHDAY_T10_BOOK_STORE", "BIRTHDAY_T10_VISIT_STORE")
//                    || isStoreVisitPayload(cleanPayload)) {
//
//                sendBirthdayT10StoreVisit(phone);
//                saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
//                return true;
//            }
//
//            if (isPayload(cleanPayload, "BIRTHDAY_T10_CATALOGUE_CALLBACK")) {
//
//                sendBirthdayT10StoreHelp(phone);
//                saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
//                return true;
//            }
//
//            if (isPayload(cleanPayload, "BIRTHDAY_T10_OFFER_CALLBACK", "BIRTHDAY_T10_BOOK_APPOINTMENT")
//                    || isCallbackPayload(cleanPayload)) {
//
//                sendBirthdayT10CallbackConfirmation(phone);
//                saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
//                return true;
//            }
//        }


                        if (isBirthdayT10CatalogueOrOfferStep(currentStep)) {
                            if (isPayload(cleanPayload,
                                    "BIRTHDAY_T10_OFFER_BOOK_STORE",
                                    "BIRTHDAY_T10_CATALOGUE_VISIT_STORE")
                                    || isStoreVisitPayload(cleanPayload)) {

                                saveLead(
                                        phone,
                                        session.getCustomerId(),
                                        firstName,
                                        session,
                                        Lead.LeadType.STORE_VISIT,
                                        "Birthday T-10 store visit requested"
                                );

                                sendBirthdayT10StoreVisit(phone);
                                saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
                                return true;
                            }

            // Offer path Request callback
            // Catalogue follow-up Request callback
            // Dono case me direct Done + Explore again aana chahiye.
            if (isPayload(cleanPayload,
                    "BIRTHDAY_T10_OFFER_CALLBACK",
                    "BIRTHDAY_T10_CATALOGUE_CALLBACK")
                    || isCallbackPayload(cleanPayload)) {


                saveLead(
                        phone,
                        session.getCustomerId(),
                        firstName,
                        session,
                        Lead.LeadType.CALLBACK,
                        "Birthday T-10 callback requested"
                );

                sendBirthdayT10CallbackConfirmation(phone);
                saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
                return true;
            }
        }


        if (isPayload(cleanPayload, "BIRTHDAY_T10_EXPLORE_AGAIN", "EXPLORE_AGAIN", "BROWSE_AGAIN")) {
            if (isBirthdayT10AnyStep(currentStep)) {
                sendBirthdayT10GenderSelection(phone);
                saveStep(session, STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT);
                return true;
            }
        }


        // Safety fallback for old Birthday T-10 messages already sent on WhatsApp.
// Agar user old upper button click kare, welcome flow me mat bhejo.
        if (isBirthdayT10AnyStep(currentStep)) {

            if (isPayload(cleanPayload, "BIRTHDAY_T10_BOOK_STORE", "BIRTHDAY_T10_VISIT_STORE")
                    || isStoreVisitPayload(cleanPayload)) {

                sendBirthdayT10StoreVisit(phone);
                saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
                return true;
            }

            if (isPayload(cleanPayload, "BIRTHDAY_T10_CATALOGUE_CALLBACK")) {

                sendBirthdayT10StoreHelp(phone);
                saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
                return true;
            }

            if (isPayload(cleanPayload, "BIRTHDAY_T10_OFFER_CALLBACK", "BIRTHDAY_T10_BOOK_APPOINTMENT")
                    || isCallbackPayload(cleanPayload)) {

                sendBirthdayT10CallbackConfirmation(phone);
                saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
                return true;
            }

            if (isPayload(cleanPayload, "BIRTHDAY_T10_EXPLORE_AGAIN", "EXPLORE_AGAIN", "BROWSE_AGAIN")) {

                sendBirthdayT10GenderSelection(phone);
                saveStep(session, STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT);
                return true;
            }
        }


        // ----------------------------------------------------
// Birthday T-10 stale old buttons safety
// User agar neeche aane ke baad upar wala old button click kare,
// toh bhi expected response hi aana chahiye.
// ----------------------------------------------------
        if (isBirthdayT10AnyStep(currentStep)) {

            // Old/new Book store visit / Visit nearest store
            if (isPayload(cleanPayload,
                    "BIRTHDAY_T10_OFFER_BOOK_STORE",
                    "BIRTHDAY_T10_BOOK_STORE",
                    "BIRTHDAY_T10_CATALOGUE_VISIT_STORE",
                    "BIRTHDAY_T10_VISIT_STORE",
                    "VISIT_NEAREST_STORE",
                    "BOOK_STORE_VISIT",
                    "VISIT_STORE",
                    "NEARBY_STORE",
                    "PICK_UP_STORE",
                    "STORE_VISIT",
                    "FIND_STORE_NEAR_ME")
                    || (cleanPayload != null && cleanPayload.startsWith("BOOK_STORE_VISIT_"))) {

                sendBirthdayT10StoreVisit(phone);
                saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
                return true;
            }

            // Old/new Request callback
            if (isPayload(cleanPayload,
                    "BIRTHDAY_T10_OFFER_CALLBACK",
                    "BIRTHDAY_T10_CATALOGUE_CALLBACK",
                    "REQUEST_CALLBACK",
                    "REQUEST_A_CALLBACK",
                    "INTERESTED",
                    "SPEAK_WITH_EXPERT")
                    || (cleanPayload != null && cleanPayload.startsWith("REQUEST_CALLBACK_"))) {

                sendBirthdayT10CallbackConfirmation(phone);
                saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
                return true;
            }

            // Old/new Book appointment
            if (isPayload(cleanPayload,
                    "BIRTHDAY_T10_BOOK_APPOINTMENT",
                    "BOOK_AN_APPOINTMENT")) {

                sendBirthdayT10CallbackConfirmation(phone);
                saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
                return true;
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_EXPLORE_AGAIN",
                    "EXPLORE_AGAIN",
                    "BROWSE_AGAIN")) {

                if (isAnniversaryT10AnyStep(currentStep)) {
                    sendAnniversaryT10GenderSelection(phone);
                    saveStep(session, STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT);
                    return true;
                }
            }
        }

        return false;
    }





    // ---------------------------------------------------------------------
// Stale / old button safety fallback
// Ye layer tab chalegi jab normal current-step router payload ko handle nahi kar paya.
// Isse old WhatsApp buttons welcome fallback me nahi jayenge.
// ---------------------------------------------------------------------

    private boolean handleStaleKnownPayloadFallback(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload,
            Long customerId
    ) {
        String currentStep = safeStep(session);

        // ----------------------------------------------------
        // Unique month confirmation payloads: inhe current step ke bina bhi safely route kar sakte hain.
        // ----------------------------------------------------

        if (isPayload(cleanPayload, "BIRTHDAY_MONTH_YES")) {
            sendBirthdayT10Opener(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_OPENER_SENT);
            return true;
        }

        if (isPayload(cleanPayload, "BIRTHDAY_MONTH_NO")) {
            sendBirthdayT10DobCorrectionAsk(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_DOB_PENDING);
            return true;
        }

        if (isPayload(cleanPayload, "ANNIVERSARY_MONTH_YES")) {
            sendAnniversaryT10Bridge(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_BRIDGE_SENT);
            return true;
        }

        if (isPayload(cleanPayload, "ANNIVERSARY_MONTH_NO")) {
            sendAnniversaryT10DateCorrectionAsk(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_DATE_PENDING);
            return true;
        }

        // ----------------------------------------------------
        // Birthday T-10 stale buttons
        // ----------------------------------------------------
        if (isBirthdayT10AnyStep(currentStep)) {
            return handleBirthdayT10StalePayload(phone, session, firstName, cleanPayload);
        }

        // ----------------------------------------------------
        // Birthday T-Day stale buttons
        // ----------------------------------------------------
        if (isBirthdayTDayActiveStep(currentStep)) {
            return handleBirthdayTDayStalePayload(phone, session, firstName, cleanPayload, customerId);
        }

        // ----------------------------------------------------
        // Anniversary T-10 stale buttons
        // ----------------------------------------------------
        if (isAnniversaryT10AnyStep(currentStep)) {
            return handleAnniversaryT10StalePayload(phone, session, firstName, cleanPayload);
        }

        // ----------------------------------------------------
        // Anniversary T-Day stale buttons
        // ----------------------------------------------------
        if (isAnniversaryTDayActiveStep(currentStep)) {
            return handleAnniversaryTDayStalePayload(phone, session, firstName, cleanPayload, customerId);
        }

        return false;
    }

    private boolean handleBirthdayT10StalePayload(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        // Old confirmation buttons
        if (isPayload(cleanPayload, "YES_THAT_S_CORRECT", "YES_THATS_CORRECT", "YES_CORRECT")) {
            sendBirthdayT10Opener(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_OPENER_SENT);
            return true;
        }

        if (isPayload(cleanPayload, "NO_THAT_S_NOT_RIGHT", "NO_THATS_NOT_RIGHT", "NO_NOT_RIGHT")) {
            sendBirthdayT10DobCorrectionAsk(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_DOB_PENDING);
            return true;
        }

        // Bridge buttons
        if (isPayload(cleanPayload, "BIRTHDAY_T10_YES_SHOW_ME", "YES_SHOW_ME", "SHOW_ME")) {
            sendBirthdayT10Opener(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_OPENER_SENT);
            return true;
        }

        if (isPayload(cleanPayload, "BIRTHDAY_T10_MAYBE_LATER", "NO_MAYBE_LATER", "MAYBE_LATER")) {
            sendBirthdayT10GracefulExit(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_GRACEFUL_EXIT);
            return true;
        }

        // Opener buttons
        if (isPayload(cleanPayload,
                "FIND_MY_PERFECT_WATCH",
                "FIND_PERFECT_WATCH",
                "FIND_BIRTHDAY_WATCH",
                "FIND_WATCH",
                "FIND_MY_WATCH",
                "FIND_MY_BIRTHDAY_WATCH")) {

            sendBirthdayT10GenderSelection(phone);
            saveStep(session, STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT);
            return true;
        }

        if (isPayload(cleanPayload, "SEE_BIRTHDAY_OFFERS", "BIRTHDAY_OFFERS", "EXCLUSIVE_BIRTHDAY_OFFERS")) {
            sendBirthdayT10Offer(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_T10_OFFER_SENT);
            return true;
        }

        // Gender buttons
        if (isMensCollectionPayload(cleanPayload)) {
            return sendBirthdayT10BrandCarousel(phone, session, firstName, "MEN");
        }

        if (isWomensCollectionPayload(cleanPayload)) {
            return sendBirthdayT10BrandCarousel(phone, session, firstName, "WOMEN");
        }

        // Carousel buttons
        if (isExploreCarouselPayload(cleanPayload)) {
            return handleBirthdayT10ExploreCollection(phone, session, firstName, cleanPayload);
        }

        if (isCallbackCarouselPayload(cleanPayload)) {
            return handleBirthdayT10CarouselCallback(phone, session,firstName,cleanPayload);
        }

        // Store / callback buttons
        if (isPayload(cleanPayload, "BIRTHDAY_T10_BOOK_STORE", "BIRTHDAY_T10_VISIT_STORE")
                || isStoreVisitPayload(cleanPayload)) {

            sendBirthdayT10StoreVisit(phone);
            saveStep(session, STEP_BIRTHDAY_T10_STORE_VISIT_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "BIRTHDAY_T10_OFFER_CALLBACK",
                "BIRTHDAY_T10_CATALOGUE_CALLBACK")
                || isCallbackPayload(cleanPayload)) {

            sendBirthdayT10CallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
            return true;
        }

        if (isPayload(cleanPayload, "BIRTHDAY_T10_OFFER_CALLBACK", "BIRTHDAY_T10_BOOK_APPOINTMENT")
                || isCallbackPayload(cleanPayload)) {

            sendBirthdayT10CallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
            return true;
        }

        // Explore again must go directly to gender selection, not opener/welcome
        if (isPayload(cleanPayload, "BIRTHDAY_T10_EXPLORE_AGAIN", "EXPLORE_AGAIN", "BROWSE_AGAIN")) {
            sendBirthdayT10GenderSelection(phone);
            saveStep(session, STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT);
            return true;
        }

        return false;
    }
    private boolean isBirthdayTDayExitMessageStep(String step) {
        return isStep(step,
                STEP_BIRTHDAY_TDAY_EXIT_MESSAGE_SENT,

                // old compatibility if needed
                "BIRTHDAY_TDAY_EXIT_MESSAGE_SENT"
        );
    }

    private boolean isBirthdayTDayCompletedStep(String step) {
        return isStep(step,
                STEP_BIRTHDAY_TDAY_FLOW_COMPLETED,
                STEP_BIRTHDAY_TDAY_EXIT_MESSAGE_SENT,

                // old compatibility
                "BIRTHDAY_TDAY_FLOW_COMPLETED",
                "BIRTHDAY_TDAY_FLOW_ENDED"
        );
    }
    private boolean handleBirthdayTDayStalePayload(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload,
            Long customerId
    ) {
//        String currentStep = safeStep(session);


        String currentStep = safeStep(session);

        if (isBirthdayTDayExitMessageStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "BIRTHDAY_TDAY_VISIT_ANYTIME",
                    "VISIT_US_ANYTIME")) {

                karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
                saveStep(session, STEP_BIRTHDAY_TDAY_FLOW_COMPLETED);
                return true;
            }

            log.info("Ignored stale T-Day payload after exit message phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        if (isBirthdayTDayCompletedStep(currentStep)) {
            log.info("Ignored stale T-Day payload after flow completed phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }




        if (isPayload(cleanPayload,
                "BIRTHDAY_TDAY_VIEW_BENEFIT",
                "VIEW_ACCOUNT_BENEFIT",
                "VIEW_BENEFIT")) {

            sendBirthdayTDayWish(phone, firstName);
            saveStep(session, STEP_BIRTHDAY_TDAY_WISH_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "BIRTHDAY_TDAY_LOCATE_STORE_FROM_BENEFIT",
                "BIRTHDAY_TDAY_LOCATE_STORE_FROM_WISH",
                "LOCATE_STORE",
                "LOCATE_NEAREST_STORE",
                "LOCATE_NEAREST_STORE_FROM_WISH")) {

            saveLead(
                    phone,
                    customerId,
                    firstName,
                    session,
                    Lead.LeadType.STORE_VISIT,
                    "Birthday T-Day locate nearest store"
            );


            sendBirthdayTDayStoreLocator(phone, session, customerId, firstName);
            return true;
        }

        if (isPayload(cleanPayload,
                "BIRTHDAY_TDAY_VISIT_STORE_FROM_REMINDER",
                "VISIT_STORE_TODAY")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
            sendBirthdayTDayStoreHelpAfterReminderStoreVisit(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_STORE_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "BIRTHDAY_TDAY_CALLBACK_AFTER_STORE_VISIT",
                "REQUEST_CALLBACK",
                "REQUEST_A_CALLBACK")
                && isStep(currentStep,
                STEP_BIRTHDAY_TDAY_STORE_HELP_SENT,
                "STEP_6B_SENT")) {

            saveLead(
                    phone,
                    customerId,
                    firstName,
                    session,
                    Lead.LeadType.CALLBACK,
                    "Birthday T-Day callback after store visit"
            );


            sendBirthdayTDayCallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_CALLBACK_CONFIRMED);
            scheduleBirthdayTDayFinalReminder(phone, firstName);
            return true;
        }

        if (isPayload(cleanPayload,
                "BIRTHDAY_TDAY_CALLBACK_FROM_REMINDER",
                "BIRTHDAY_TDAY_CALLBACK_AFTER_REMINDER_STORE",
                "REQUEST_CALLBACK",
                "REQUEST_A_CALLBACK")
                && (isBirthdayTDayFinalReminderStep(currentStep)
                || isBirthdayTDayFinalReminderStoreStep(currentStep))) {

            sendBirthdayTDayCallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED);
            scheduleBirthdayTDayExit(phone, firstName);
            return true;
        }

        if (isPayload(cleanPayload,
                "BIRTHDAY_TDAY_VISIT_ANYTIME",
                "VISIT_US_ANYTIME")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
            saveStep(session, STEP_BIRTHDAY_TDAY_EXIT_MESSAGE_SENT);
            return true;
        }

        return false;
    }

    private boolean handleAnniversaryT10StalePayload(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_MONTH_CONFIRMED",
                "ANNIVERSARY_MONTH_YES",
                "YES_THAT_S_CORRECT",
                "YES_THATS_CORRECT",
                "YES_CORRECT")) {

            sendAnniversaryT10Bridge(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_BRIDGE_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_DATE_CORRECTION",
                "ANNIVERSARY_MONTH_NO",
                "NO_THAT_S_NOT_RIGHT",
                "NO_THATS_NOT_RIGHT",
                "NO_NOT_RIGHT")) {

            sendAnniversaryT10DateCorrectionAsk(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_DATE_PENDING);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_EXPLORE_COLLECTIONS",
                "ANNIV_YES_SHOW_ME")) {

            sendAnniversaryT10Opener(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_OPENER_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_EXIT_AFTER_CONFIRMATION",
                "ANNIV_MAYBE_LATER")) {

            sendAnniversaryT10GracefulExit(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_GRACEFUL_EXIT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_FIND_GIFT",
                "FIND_ANNIVERSARY_GIFT",
                "FIND_PERFECT_GIFT",
                "FIND_GIFT")) {

            sendAnniversaryT10GenderSelection(phone);
            saveStep(session, STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_VIEW_OFFERS",
                "SEE_ANNIVERSARY_OFFERS",
                "ANNIVERSARY_OFFERS")) {

            sendAnniversaryT10Offer(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_T10_OFFER_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_MENS_COLLECTION",
                "MENS_COLLECTION",
                "MEN_COLLECTION",
                "MENS")) {

            return sendAnniversaryT10BrandCarousel(phone, session, firstName, "MEN");
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_WOMENS_COLLECTION",
                "WOMENS_COLLECTION",
                "WOMEN_COLLECTION",
                "WOMENS")) {

            return sendAnniversaryT10BrandCarousel(phone, session, firstName, "WOMEN");
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_COUPLE_WATCHES",
                "COUPLE_WATCHES",
                "COUPLE")) {

            sendAnniversaryT10CoupleWatchesCatalogue(phone, firstName);
            session.setSelectedCollection(BotSession.Collection.COUPLES);
            saveStep(session, STEP_ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT);
            scheduleAnniversaryT10CatalogueFollowUp(phone, firstName);
            return true;
        }

        // Carousel common rehne do.
        if (isExploreCarouselPayload(cleanPayload)) {
            return handleAnniversaryT10ExploreCollection(phone, session, firstName, cleanPayload);
        }

        if (isCallbackCarouselPayload(cleanPayload)) {
            return handleAnniversaryT10CarouselCallback(phone, session, firstName ,cleanPayload);
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_CATALOGUE_VISIT_STORE",
                "ANNIVERSARY_T10_OFFER_BOOK_STORE",
                "ANNIV_BOOK_STORE",
                "VISIT_NEAREST_STORE")) {

            sendAnniversaryT10StoreVisit(phone);
            saveStep(session, STEP_ANNIVERSARY_T10_STORE_VISIT_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_CATALOGUE_CALLBACK",
                "ANNIVERSARY_T10_OFFER_CALLBACK",
                "ANNIV_T10_CALLBACK",
                "REQUEST_CALLBACK")) {

            saveLead(
                    phone,
                    session.getCustomerId(),
                    firstName,
                    session,
                    Lead.LeadType.CALLBACK,
                    "Anniversary T-10 callback requested"
            );

            sendAnniversaryT10CallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_BOOK_APPOINTMENT",
                "BOOK_AN_APPOINTMENT")) {

            sendAnniversaryT10CallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_EXPLORE_AGAIN",
                "EXPLORE_AGAIN",
                "BROWSE_AGAIN")) {

            sendAnniversaryT10GenderSelection(phone);
            saveStep(session, STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT);
            return true;
        }

        return false;
    }
    private boolean handleAnniversaryTDayStalePayload(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload,
            Long customerId
    ) {
        String currentStep = safeStep(session);

        // Exit message sent hai: sirf Visit us anytime allowed.
        if (isAnniversaryTDayExitMessageStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_TDAY_VISIT_ANYTIME",
                    "ANNIV_VISIT_US_ANYTIME",
                    "VISIT_US_ANYTIME")) {

                karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
                saveStep(session, STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED);
                return true;
            }

            log.info("Ignored stale Anniversary T-Day payload after exit message phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        // Flow completed hai: kuch bhi reply nahi.
        if (isAnniversaryTDayCompletedStep(currentStep)) {
            log.info("Ignored stale Anniversary T-Day payload after flow completed phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_VIEW_BENEFIT",
                "ANNIV_VIEW_BENEFIT",
                "ANNIVERSARY_VIEW_BENEFIT",
                "ANNIV_VIEW_ACCOUNT_BENEFIT")) {

            sendAnniversaryTDayWish(phone, firstName);
            saveStep(session, STEP_ANNIVERSARY_TDAY_WISH_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_LOCATE_STORE_FROM_BENEFIT",
                "ANNIVERSARY_TDAY_LOCATE_STORE_FROM_WISH",
                "ANNIV_LOCATE_STORE",
                "ANNIV_LOCATE_NEAREST_STORE",
                "ANNIVERSARY_LOCATE_STORE")) {

            saveLead(
                    phone,
                    customerId,
                    firstName,
                    session,
                    Lead.LeadType.STORE_VISIT,
                    "Anniversary T-Day locate nearest store"
            );

            sendAnniversaryTDayStoreLocator(phone, session, customerId, firstName);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_CALLBACK_AFTER_STORE_VISIT",
                "ANNIV_REQUEST_CALLBACK")
                && isStep(currentStep, STEP_ANNIVERSARY_TDAY_STORE_HELP_SENT)) {


            saveLead(
                    phone,
                    customerId,
                    firstName,
                    session,
                    Lead.LeadType.CALLBACK,
                    "Anniversary T-Day callback after store visit"
            );

            sendAnniversaryTDayCallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_CALLBACK_CONFIRMED);
            scheduleAnniversaryTDayFinalReminder(phone, firstName);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_VISIT_STORE_FROM_REMINDER",
                "ANNIV_VISIT_STORE_TODAY")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
            sendAnniversaryTDayStoreHelpAfterReminderStoreVisit(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_STORE_SENT);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_CALLBACK_FROM_REMINDER",
                "ANNIVERSARY_TDAY_CALLBACK_AFTER_REMINDER_STORE",
                "ANNIV_REQUEST_CALLBACK_FROM_REMINDER",
                "ANNIV_REQUEST_CALLBACK_AFTER_REMINDER_STORE")
                && (isAnniversaryTDayFinalReminderStep(currentStep)
                || isAnniversaryTDayFinalReminderStoreStep(currentStep))) {

            saveLead(
                    phone,
                    customerId,
                    firstName,
                    session,
                    Lead.LeadType.CALLBACK,
                    "Anniversary T-Day callback from reminder"
            );


            sendAnniversaryTDayCallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED);
            scheduleAnniversaryTDayExit(phone, firstName);
            return true;
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_VISIT_ANYTIME",
                "ANNIV_VISIT_US_ANYTIME",
                "VISIT_US_ANYTIME")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED);
            return true;
        }

        return false;
    }

    private boolean handleBirthdayT10DobCorrection(
            String phone,
            BotSession session,
            String firstName,
            Long customerId,
            String text
    ) {
        if (!isBirthdayT10DobPendingStep(safeStep(session))) {
            return false;
        }

        if (isValidDate(text)) {
            LocalDate dob = LocalDate.parse(text.trim(), DATE_FORMATTER);
            updateCustomerBirthday(customerId, dob);

            saveStep(session, STEP_BIRTHDAY_T10_BRIDGE_PENDING);

            taskScheduler.schedule(() -> {
                BotSession latest = getLatestSession(phone);

                if (latest == null) {
                    return;
                }

                if (!isCurrentStep(latest, STEP_BIRTHDAY_T10_BRIDGE_PENDING)) {
                    return;
                }

                sendBirthdayT10Bridge(phone, firstName);
                saveStep(latest, STEP_BIRTHDAY_T10_BRIDGE_SENT);

            }, Instant.now().plusSeconds(BIRTHDAY_T10_DOB_BRIDGE_DELAY_SECONDS));

            return true;
        }
        karixApiService.sendTextMessage(
                phone,
                "Please reply with your date of birth in this format:\nDD/MM/YYYY"
        );
        return true;
    }

    private boolean sendBirthdayT10BrandCarousel(
            String phone,
            BotSession session,
            String firstName,
            String gender
    ) {
        boolean sent = sendBrandCarousel(phone, firstName, gender);
        if (!sent) {
            karixApiService.sendTextMessage(phone, "Sorry, collections could not be loaded right now. Please try again.");
            return true;
        }

        session.setSelectedCollection("MEN".equalsIgnoreCase(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS);
        saveStep(session, "MEN".equalsIgnoreCase(gender)
                ? STEP_BIRTHDAY_T10_MEN_BRAND_CAROUSEL_SENT
                : STEP_BIRTHDAY_T10_WOMEN_BRAND_CAROUSEL_SENT);
        return true;
    }

    private boolean handleBirthdayT10ExploreCollection(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        String gender = cleanPayload.startsWith("EX_M_") ? "MEN" : "WOMEN";
        String brandCode = extractShortBrandCode(cleanPayload);
        String brandKey = shortBrandCodeToBrandKey(brandCode);

        sendCatalogue(
                phone,
                firstName,
                gender,
                brandKey,
                BIRTHDAY_CATALOGUE_PDF_URL
        );


        session.setSelectedCollection("MEN".equalsIgnoreCase(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS);
        session.setSelectedBrand(brandKey);
        saveStep(session, STEP_BIRTHDAY_T10_CATALOGUE_SENT);
        scheduleBirthdayT10CatalogueFollowUp(phone, firstName);
        return true;
    }

    private boolean handleBirthdayT10CarouselCallback(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        String gender = cleanPayload.startsWith("CB_M_") ? "MEN" : "WOMEN";
        String brandCode = extractShortBrandCode(cleanPayload);
        String brandKey = shortBrandCodeToBrandKey(brandCode);

        session.setSelectedCollection("MEN".equalsIgnoreCase(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS);
        session.setSelectedBrand(brandKey);


        saveLead(
                phone,
                session.getCustomerId(),
                firstName,
                session,
                Lead.LeadType.CALLBACK,
                "Birthday T-10 carousel request callback | gender=" + gender + " | brand=" + brandKey
        );

        sendBirthdayT10CallbackConfirmationWithExplore(phone, gender, brandCode);
        saveStep(session, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED);
        return true;
    }

    // ---------------------------------------------------------------------
    // Birthday T-Day / T-0 Flow
    // Happy birthday -> benefit intro -> wish/store -> callback/reminder/exit.
    // ---------------------------------------------------------------------

    private boolean handleBirthdayTDayFlow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload,
            Long customerId
    ) {
        String currentStep = safeStep(session);


        // ----------------------------------------------------
// Birthday T-Day exit message button
// Exit message already sent. Only "Visit us anytime" should work.
// Any old upper button should not restart Birthday T-10/welcome.
// ----------------------------------------------------
        if (isBirthdayTDayExitMessageStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "BIRTHDAY_TDAY_VISIT_ANYTIME",
                    "VISIT_US_ANYTIME")) {

                karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
                saveStep(session, STEP_BIRTHDAY_TDAY_FLOW_COMPLETED);
                return true;
            }

            log.info("Ignored old T-Day button after exit message phone={} step={} payload={}",
                    phone, currentStep, cleanPayload);
            return true;
        }

// ----------------------------------------------------
// Birthday T-Day already completed.
// No old upper button should trigger any new response.
// ----------------------------------------------------
        if (isBirthdayTDayCompletedStep(currentStep)) {
            log.info("Ignored payload after Birthday T-Day completed phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        // Birthday benefit intro buttons
        if (isBirthdayTDayTemplateStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "BIRTHDAY_TDAY_VIEW_BENEFIT",
                    "VIEW_ACCOUNT_BENEFIT",
                    "VIEW_BENEFIT")) {

                sendBirthdayTDayWish(phone, firstName);
                saveStep(session, STEP_BIRTHDAY_TDAY_WISH_SENT);
                return true;
            }

            if (isPayload(cleanPayload,
                    "BIRTHDAY_TDAY_LOCATE_STORE_FROM_BENEFIT",
                    "LOCATE_NEAREST_STORE")) {

                sendBirthdayTDayStoreLocator(phone, session, customerId, firstName);
                return true;
            }
        }

        // Birthday wish message → Locate Store
        if (isBirthdayTDayActiveStep(currentStep)
                && isPayload(cleanPayload,
                "BIRTHDAY_TDAY_LOCATE_STORE_FROM_WISH",
                "LOCATE_STORE",
                "LOCATE_NEAREST_STORE_FROM_WISH")) {

            sendBirthdayTDayStoreLocator(phone, session, customerId, firstName);
            return true;
        }

        // Store visit assistance after normal store locator → callback confirmation → reminder
        if (isStep(currentStep,
                STEP_BIRTHDAY_TDAY_STORE_HELP_SENT,
                "STEP_6B_SENT")
                && isPayload(cleanPayload,
                "BIRTHDAY_TDAY_CALLBACK_AFTER_STORE_VISIT",
                "REQUEST_CALLBACK",
                "REQUEST_A_CALLBACK")) {

            sendBirthdayTDayCallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_CALLBACK_CONFIRMED);

            scheduleBirthdayTDayFinalReminder(phone, firstName);
            return true;
        }

        // Reminder → Visit Store Today
        if (isBirthdayTDayFinalReminderStep(currentStep)
                && isPayload(cleanPayload,
                "BIRTHDAY_TDAY_VISIT_STORE_FROM_REMINDER",
                "VISIT_STORE_TODAY")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);

            sendBirthdayTDayStoreHelpAfterReminderStoreVisit(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_STORE_SENT);
            return true;
        }

        // Reminder → Request Callback direct
        if (isBirthdayTDayFinalReminderStep(currentStep)
                && isPayload(cleanPayload,
                "BIRTHDAY_TDAY_CALLBACK_FROM_REMINDER",
                "REQUEST_CALLBACK",
                "REQUEST_A_CALLBACK")) {

            sendBirthdayTDayCallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED);

            scheduleBirthdayTDayExit(phone, firstName);
            return true;
        }

        // Store help after reminder store visit → callback confirmation → exit
        if (isBirthdayTDayFinalReminderStoreStep(currentStep)
                && isPayload(cleanPayload,
                "BIRTHDAY_TDAY_CALLBACK_AFTER_REMINDER_STORE",
                "REQUEST_CALLBACK",
                "REQUEST_A_CALLBACK")) {

            sendBirthdayTDayCallbackConfirmation(phone);
            saveStep(session, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED);

            scheduleBirthdayTDayExit(phone, firstName);
            return true;
        }

        // Exit message → Visit us anytime
        if (isBirthdayTDayActiveStep(currentStep)
                && isPayload(cleanPayload,
                "BIRTHDAY_TDAY_VISIT_ANYTIME",
                "VISIT_US_ANYTIME")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
            saveStep(session, STEP_BIRTHDAY_TDAY_EXIT_MESSAGE_SENT);
            return true;
        }

        return false;
    }

    private void sendBirthdayTDayStoreLocator(
            String phone,
            BotSession session,
            Long customerId,
            String firstName
    ) {
        karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
        saveStep(session, STEP_BIRTHDAY_TDAY_STORE_LOCATOR_SENT);
        scheduleBirthdayTDayStoreHelp(phone, firstName);
    }

    // ---------------------------------------------------------------------
    // Anniversary T-10 Flow
    // Anniversary -> confirmation -> bridge -> opener -> discovery / offer.
    // ---------------------------------------------------------------------

    private boolean handleAnniversaryT10Flow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload,
            Long customerId
    ) {
        String currentStep = safeStep(session);

        if (isAnniversaryT10MonthConfirmationStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_MONTH_CONFIRMED",
                    "ANNIVERSARY_MONTH_YES",
                    "YES_THAT_S_CORRECT",
                    "YES_THATS_CORRECT",
                    "YES_CORRECT")) {

                sendAnniversaryT10Bridge(phone, firstName);
                saveStep(session, STEP_ANNIVERSARY_T10_BRIDGE_SENT);
                return true;
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_DATE_CORRECTION",
                    "ANNIVERSARY_MONTH_NO",
                    "NO_THAT_S_NOT_RIGHT",
                    "NO_THATS_NOT_RIGHT",
                    "NO_NOT_RIGHT")) {

                sendAnniversaryT10DateCorrectionAsk(phone, firstName);
                saveStep(session, STEP_ANNIVERSARY_T10_DATE_PENDING);
                return true;
            }
        }

        if (isAnniversaryT10BridgeStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_EXPLORE_COLLECTIONS",
                    "ANNIV_YES_SHOW_ME")) {

                sendAnniversaryT10Opener(phone, firstName);
                saveStep(session, STEP_ANNIVERSARY_T10_OPENER_SENT);
                return true;
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_EXIT_AFTER_CONFIRMATION",
                    "ANNIV_MAYBE_LATER")) {

                sendAnniversaryT10GracefulExit(phone, firstName);
                saveStep(session, STEP_ANNIVERSARY_T10_GRACEFUL_EXIT);
                return true;
            }
        }

        if (isAnniversaryT10OpenerStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_FIND_GIFT",
                    "FIND_ANNIVERSARY_GIFT",
                    "FIND_PERFECT_GIFT",
                    "FIND_GIFT")) {

                sendAnniversaryT10GenderSelection(phone);
                saveStep(session, STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT);
                return true;
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_VIEW_OFFERS",
                    "SEE_ANNIVERSARY_OFFERS",
                    "ANNIVERSARY_OFFERS")) {

                sendAnniversaryT10Offer(phone, firstName);
                saveStep(session, STEP_ANNIVERSARY_T10_OFFER_SENT);
                return true;
            }
        }

        if (isAnniversaryT10GenderSelectionStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_MENS_COLLECTION",
                    "MENS_COLLECTION",
                    "MEN_COLLECTION",
                    "MENS")) {

                return sendAnniversaryT10BrandCarousel(phone, session, firstName, "MEN");
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_WOMENS_COLLECTION",
                    "WOMENS_COLLECTION",
                    "WOMEN_COLLECTION",
                    "WOMENS")) {

                return sendAnniversaryT10BrandCarousel(phone, session, firstName, "WOMEN");
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_COUPLE_WATCHES",
                    "COUPLE_WATCHES",
                    "COUPLE")) {

                sendAnniversaryT10CoupleWatchesCatalogue(phone, firstName);
                session.setSelectedCollection(BotSession.Collection.COUPLES);
                saveStep(session, STEP_ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT);
                scheduleAnniversaryT10CatalogueFollowUp(phone, firstName);
                return true;
            }
        }

        if (isAnniversaryT10BrandCarouselStep(currentStep)) {

            if (isAnniversaryT10ExploreCarouselPayload(cleanPayload)
                    || isExploreCarouselPayload(cleanPayload)) {

                return handleAnniversaryT10ExploreCollection(phone, session, firstName, cleanPayload);
            }

            if (isAnniversaryT10CallbackCarouselPayload(cleanPayload)
                    || isCallbackCarouselPayload(cleanPayload)) {

                return handleAnniversaryT10CarouselCallback(phone, session,firstName, cleanPayload);
            }
        }

        if (isAnniversaryT10CatalogueOrOfferStep(currentStep)) {

            // Catalogue follow-up: Visit nearest store
            // Offer path: Book store visit
            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_CATALOGUE_VISIT_STORE",
                    "ANNIVERSARY_T10_OFFER_BOOK_STORE",
                    "ANNIV_BOOK_STORE",
                    "VISIT_NEAREST_STORE")) {

                saveLead(
                        phone,
                        session.getCustomerId(),
                        firstName,
                        session,
                        Lead.LeadType.STORE_VISIT,
                        "Anniversary T-10 store visit requested"
                );



                sendAnniversaryT10StoreVisit(phone);
                saveStep(session, STEP_ANNIVERSARY_T10_STORE_VISIT_SENT);
                return true;
            }

            // Catalogue follow-up: Request callback
            // Offer path: Request callback
            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_CATALOGUE_CALLBACK",
                    "ANNIVERSARY_T10_OFFER_CALLBACK",
                    "ANNIV_T10_CALLBACK",
                    "REQUEST_CALLBACK")) {

                sendAnniversaryT10CallbackConfirmation(phone);
                saveStep(session, STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED);
                return true;
            }
        }


        if (isStep(currentStep, STEP_ANNIVERSARY_T10_STORE_VISIT_SENT, "ANNIVERSARY_STORE_VISIT_SENT")) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_T10_BOOK_APPOINTMENT",
                    "BOOK_AN_APPOINTMENT")) {

                saveLead(
                        phone,
                        session.getCustomerId(),
                        firstName,
                        session,
                        Lead.LeadType.CALLBACK,
                        "Anniversary T-10 book appointment"
                );

                sendAnniversaryT10CallbackConfirmation(phone);
                saveStep(session, STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED);
                return true;
            }
        }

        if (isPayload(cleanPayload,
                "ANNIVERSARY_T10_EXPLORE_AGAIN",
                "EXPLORE_AGAIN",
                "BROWSE_AGAIN")) {

            if (isAnniversaryT10AnyStep(currentStep)) {
                sendAnniversaryT10GenderSelection(phone);
                saveStep(session, STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT);
                return true;
            }
        }
        return false;
    }

    private boolean handleAnniversaryT10DateCorrection(
            String phone,
            BotSession session,
            String firstName,
            Long customerId,
            String text
    ) {
        if (!isAnniversaryT10DatePendingStep(safeStep(session))) {
            return false;
        }

        if (isValidDate(text)) {
            LocalDate anniversaryDate = LocalDate.parse(text.trim(), DATE_FORMATTER);
            updateCustomerAnniversary(customerId, anniversaryDate);

            saveStep(session, STEP_ANNIVERSARY_T10_BRIDGE_PENDING);

            taskScheduler.schedule(() -> {
                BotSession latest = getLatestSession(phone);

                if (latest == null) {
                    return;
                }

                if (!isCurrentStep(latest, STEP_ANNIVERSARY_T10_BRIDGE_PENDING)) {
                    return;
                }

                sendAnniversaryT10Bridge(phone, firstName);
                saveStep(latest, STEP_ANNIVERSARY_T10_BRIDGE_SENT);

            }, Instant.now().plusSeconds(ANNIVERSARY_T10_DATE_BRIDGE_DELAY_SECONDS));

            return true;
        }

        karixApiService.sendTextMessage(
                phone,
                "Please reply with your anniversary date in this format:\nDD/MM/YYYY"
        );
        return true;
    }

    private boolean isAnniversaryT10ExploreCarouselPayload(String payload) {
        return payload != null && payload.startsWith("ANNIV10_EXPLORE_");
    }

    private boolean isAnniversaryT10CallbackCarouselPayload(String payload) {
        return payload != null && payload.startsWith("ANNIV10_CALLBACK_");
    }

    private boolean sendAnniversaryT10BrandCarousel(
            String phone,
            BotSession session,
            String firstName,
            String gender
    ) {
        boolean sent = sendBrandCarousel(phone, firstName, gender);

        if (!sent) {
            karixApiService.sendTextMessage(
                    phone,
                    "Sorry, collections could not be loaded right now. Please try again."
            );
            return true;
        }

        session.setSelectedCollection("MEN".equalsIgnoreCase(gender)
                ? BotSession.Collection.MENS
                : BotSession.Collection.WOMENS);

        saveStep(session, "MEN".equalsIgnoreCase(gender)
                ? STEP_ANNIVERSARY_T10_MEN_BRAND_CAROUSEL_SENT
                : STEP_ANNIVERSARY_T10_WOMEN_BRAND_CAROUSEL_SENT);

        return true;
    }

    private boolean handleAnniversaryT10ExploreCollection(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        String gender = cleanPayload.contains("_M_") ? "MEN" : "WOMEN";
        String brandCode = extractShortBrandCode(cleanPayload);
        String brandKey = shortBrandCodeToBrandKey(brandCode);

        sendCatalogue(
                phone,
                firstName,
                gender,
                brandKey,
                ANNIVERSARY_CATALOGUE_PDF_URL
        );

        session.setSelectedCollection("MEN".equalsIgnoreCase(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS);
        session.setSelectedBrand(brandKey);
        saveStep(session, STEP_ANNIVERSARY_T10_CATALOGUE_SENT);
        scheduleAnniversaryT10CatalogueFollowUp(phone, firstName);
        return true;
    }

    private boolean handleAnniversaryT10CarouselCallback(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload
    ) {
        String gender = cleanPayload.contains("_M_") ? "MEN" : "WOMEN";
        String brandCode = extractShortBrandCode(cleanPayload);
        String brandKey = shortBrandCodeToBrandKey(brandCode);

        session.setSelectedCollection("MEN".equalsIgnoreCase(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS);
        session.setSelectedBrand(brandKey);

        saveLead(
                phone,
                session.getCustomerId(),
                firstName,
                session,
                Lead.LeadType.CALLBACK,
                "Anniversary T-10 carousel request callback | gender=" + gender + " | brand=" + brandKey
        );

        sendAnniversaryT10CallbackConfirmation(phone);
        saveStep(session, STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED);
        return true;
    }

    // ---------------------------------------------------------------------
    // Anniversary T-Day / T-0 Flow
    // Anniversary Day - T -> benefit intro -> wish/store -> callback/reminder/exit.
    // ---------------------------------------------------------------------

    private boolean handleAnniversaryTDayFlow(
            String phone,
            BotSession session,
            String firstName,
            String cleanPayload,
            Long customerId
    ) {
        String currentStep = safeStep(session);

        // ----------------------------------------------------
        // Exit message sent.
        // Sirf Visit us anytime allowed. Baaki old upper buttons ignore.
        // ----------------------------------------------------
        if (isAnniversaryTDayExitMessageStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_TDAY_VISIT_ANYTIME",
                    "ANNIV_VISIT_US_ANYTIME",
                    "VISIT_US_ANYTIME")) {

                karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
                saveStep(session, STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED);
                return true;
            }

            log.info("Ignored old Anniversary T-Day button after exit message phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        // ----------------------------------------------------
        // Flow completed.
        // No further messages.
        // ----------------------------------------------------
        if (isAnniversaryTDayCompletedStep(currentStep)) {
            log.info("Ignored payload after Anniversary T-Day completed phone={} payload={}",
                    phone, cleanPayload);
            return true;
        }

        // ----------------------------------------------------
        // Benefit intro buttons
        // ----------------------------------------------------
        if (isAnniversaryTDayTemplateStep(currentStep)) {

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_TDAY_VIEW_BENEFIT",
                    "ANNIV_VIEW_BENEFIT",
                    "ANNIVERSARY_VIEW_BENEFIT",
                    "ANNIV_VIEW_ACCOUNT_BENEFIT")) {

                sendAnniversaryTDayWish(phone, firstName);
                saveStep(session, STEP_ANNIVERSARY_TDAY_WISH_SENT);
                return true;
            }

            if (isPayload(cleanPayload,
                    "ANNIVERSARY_TDAY_LOCATE_STORE_FROM_BENEFIT",
                    "ANNIV_LOCATE_STORE",
                    "ANNIV_LOCATE_NEAREST_STORE",
                    "ANNIVERSARY_LOCATE_STORE")) {

                sendAnniversaryTDayStoreLocator(phone, session, customerId, firstName);
                return true;
            }
        }

        // ----------------------------------------------------
        // Anniversary wish message -> Locate Store
        // ----------------------------------------------------
        if (isAnniversaryTDayActiveStep(currentStep)
                && isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_LOCATE_STORE_FROM_WISH",
                "ANNIV_LOCATE_STORE_FROM_WISH",
                "ANNIV_LOCATE_STORE",
                "ANNIV_LOCATE_NEAREST_STORE",
                "ANNIVERSARY_LOCATE_STORE")) {

            sendAnniversaryTDayStoreLocator(phone, session, customerId, firstName);
            return true;
        }

        // ----------------------------------------------------
        // Store help after normal store locator -> callback -> reminder
        // ----------------------------------------------------
        if (isStep(currentStep,
                STEP_ANNIVERSARY_TDAY_STORE_HELP_SENT,
                "ANNIVERSARY_TDAY_STORE_HELP_SENT")
                && isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_CALLBACK_AFTER_STORE_VISIT",
                "ANNIV_REQUEST_CALLBACK")) {

            sendAnniversaryTDayCallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_CALLBACK_CONFIRMED);

            scheduleAnniversaryTDayFinalReminder(phone, firstName);
            return true;
        }

        // ----------------------------------------------------
        // Reminder -> Visit Store Today
        // ----------------------------------------------------
        if (isAnniversaryTDayFinalReminderStep(currentStep)
                && isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_VISIT_STORE_FROM_REMINDER",
                "ANNIV_VISIT_STORE_TODAY")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);

            sendAnniversaryTDayStoreHelpAfterReminderStoreVisit(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_STORE_SENT);
            return true;
        }

        // ----------------------------------------------------
        // Reminder -> Request Callback direct
        // ----------------------------------------------------
        if (isAnniversaryTDayFinalReminderStep(currentStep)
                && isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_CALLBACK_FROM_REMINDER",
                "ANNIV_REQUEST_CALLBACK_FROM_REMINDER")) {

            sendAnniversaryTDayCallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED);

            scheduleAnniversaryTDayExit(phone, firstName);
            return true;
        }

        // ----------------------------------------------------
        // Store help after reminder store visit -> callback -> exit
        // ----------------------------------------------------
        if (isAnniversaryTDayFinalReminderStoreStep(currentStep)
                && isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_CALLBACK_AFTER_REMINDER_STORE",
                "ANNIV_REQUEST_CALLBACK_AFTER_REMINDER_STORE")) {

            sendAnniversaryTDayCallbackConfirmation(phone);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED);

            scheduleAnniversaryTDayExit(phone, firstName);
            return true;
        }

        // ----------------------------------------------------
        // Exit CTA -> Visit us anytime
        // ----------------------------------------------------
        if (isAnniversaryTDayActiveStep(currentStep)
                && isPayload(cleanPayload,
                "ANNIVERSARY_TDAY_VISIT_ANYTIME",
                "ANNIV_VISIT_US_ANYTIME",
                "VISIT_US_ANYTIME")) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
            saveStep(session, STEP_ANNIVERSARY_TDAY_FLOW_COMPLETED);
            return true;
        }

        return false;
    }

    private void sendAnniversaryTDayStoreLocator(
            String phone,
            BotSession session,
            Long customerId,
            String firstName
    ) {
        karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
        saveStep(session, STEP_ANNIVERSARY_TDAY_STORE_LOCATOR_SENT);
        scheduleAnniversaryTDayStoreHelp(phone, firstName);
    }

    // ---------------------------------------------------------------------
    // Birthday T-10 messages
    // ---------------------------------------------------------------------

    private void sendBirthdayT10MonthConfirmation(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎂 *Birthday Month Confirmation*\n\n"
                        + "Hi *" + firstName + "*,\n\n"
                        + "Our records show that this is your *birthday month*.\n\n"
                        + "Can you confirm this for us?",
                List.of(
                        Map.of("title", "Yes, correct", "payload", "BIRTHDAY_MONTH_YES"),
                        Map.of("title", "No, not right", "payload", "BIRTHDAY_MONTH_NO")
                )
        );
    }

    private void sendBirthdayT10DobCorrectionAsk(String phone, String firstName) {
        karixApiService.sendTextMessage(
                phone,
                "No worries, " + firstName + "!\n"
                        + "Could you help us update your birthday?\n"
                        + "Please reply with your date of birth in this format:\n"
                        + "DD/MM/YYYY"
        );
    }

    private void sendBirthdayT10Bridge(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Thank you for confirming, " + firstName + "! 🎂\n"
                        + "Your birthday month is noted.\n\n"
                        + "Would you still like to explore our curated collections?",
                List.of(
                        Map.of("title", "Yes, show me", "payload", "BIRTHDAY_T10_YES_SHOW_ME"),
                        Map.of("title", "Maybe later", "payload", "BIRTHDAY_T10_MAYBE_LATER")
                )
        );
    }

    private void sendBirthdayT10Opener(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Great to hear from you, *" + firstName + "*! 🎂\n"
                        + "We have got something special waiting for you from the *Titan World* collection. ✨\n"
                        + "👉 *What would you like to do?*",
                List.of(
                        Map.of("title", "Find my watch", "payload", "FIND_MY_PERFECT_WATCH"),
                        Map.of("title", "Birthday offers", "payload", "SEE_BIRTHDAY_OFFERS")
                )
        );
    }

    private void sendBirthdayT10GenderSelection(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "Let's find the perfect watch for you.\n"
                        + "Browsing for:",
                List.of(
                        Map.of("title", "Men's collection", "payload", "MENS_COLLECTION"),
                        Map.of("title", "Women's collection", "payload", "WOMENS_COLLECTION")
                )
        );
    }

    private void sendBirthdayT10Offer(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Here's your birthday offer, " + firstName + " 🎁\n\n"
                        + "Up to *10% off* on selected Titan brands - yours to use for *21 days before and after your birthday*.\n\n"
                        + "Your nearest *Titan World* store is ready for you.",
                List.of(
                        Map.of("title", "Book store visit", "payload", "BIRTHDAY_T10_OFFER_BOOK_STORE"),
                        Map.of("title", "Request callback", "payload", "BIRTHDAY_T10_OFFER_CALLBACK")
                )
        );
    }
    private void sendBirthdayT10StoreVisit(String phone) {
        karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
        taskScheduler.schedule(() -> sendBirthdayT10StoreHelp(phone), Instant.now().plusSeconds(2));
    }

    private void sendBirthdayT10StoreHelp(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Book appointment", "payload", "BIRTHDAY_T10_BOOK_APPOINTMENT")
                )
        );
    }

    private void sendBirthdayT10CallbackConfirmation(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection.",
                List.of(
                        Map.of("title", "Explore again", "payload", "BIRTHDAY_T10_EXPLORE_AGAIN")
                )
        );
    }

    private void sendBirthdayT10CallbackConfirmationWithExplore(String phone, String gender, String brandCode) {
        karixApiService.sendButtonMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection.",
                List.of(
                        Map.of("title", "Explore again", "payload", "BIRTHDAY_T10_EXPLORE_AGAIN")
                )
        );
    }

    private void sendBirthdayT10GracefulExit(String phone, String firstName) {
        karixApiService.sendTextMessage(
                phone,
                "No worries, " + firstName + "! 🎂 We're here whenever you're ready."
        );
    }

    // ---------------------------------------------------------------------
    // Birthday T-Day messages
    // ---------------------------------------------------------------------

    private void sendBirthdayTDayBenefitIntro(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎉 *Birthday Benefit Activated*\n\n"
                        + "Hi *" + firstName + "*, our records show that your annual account benefit has been updated.\n\n"
                        + "A *10% discount code* is now active on your profile and can be redeemed at the nearest *Titan World* store.\n\n"
                        + "This benefit is valid for the next *21 days*.\n\n"
                        + "Tap below:",
                List.of(
                        Map.of("title", "View Account Benefit", "payload", "BIRTHDAY_TDAY_VIEW_BENEFIT"),
                        Map.of("title", "Locate Nearest Store", "payload", "BIRTHDAY_TDAY_LOCATE_STORE_FROM_BENEFIT")
                )
        );
    }

    private void sendBirthdayTDayWish(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎉 *HAPPY BIRTHDAY, " + firstName + "!*\n\n"
                        + "May your special day be filled with exceptional moments.\n\n"
                        + "Enjoy up to *10% discount* on your purchase at the nearest *Titan World* store for the next *21 days*.\n\n"
                        + "Once again, a very happy birthday from all of us at *Titan World*!",
                List.of(
                        Map.of("title", "Locate Store", "payload", "BIRTHDAY_TDAY_LOCATE_STORE_FROM_WISH")

                )
        );
    }

//    private void sendBirthdayTDayStoreHelp(String phone) {
//        karixApiService.sendButtonMessage(
//                phone,
//                "📍 *Store Visit Assistance*\n\n"
//                        + "Hope you found your nearest store.\n\n"
//                        + "If you need help, our expert can call you and book an appointment.",
//                List.of(
//                        Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK")
//                )
//        );
//    }



    private void sendBirthdayTDayStoreHelpAfterStoreLocator(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Request Callback", "payload", "BIRTHDAY_TDAY_CALLBACK_AFTER_STORE_VISIT")
                )
        );
    }

    private void sendBirthdayTDayStoreHelpAfterReminderStoreVisit(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Request Callback", "payload", "BIRTHDAY_TDAY_CALLBACK_AFTER_REMINDER_STORE")
                )
        );
    }


    private void sendBirthdayTDayCallbackConfirmation(String phone) {
        karixApiService.sendTextMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection."
        );
    }

    private void sendBirthdayTDayFinalReminder(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "⏳ *Your birthday offer is still live, " + firstName + ".*\n\n"
                        + "A few hours left today — and we'd hate for you to miss it.\n\n"
                        + "Visit your nearest store or book an appointment with an expert.",
                List.of(
                        Map.of("title", "Visit Store Today", "payload", "BIRTHDAY_TDAY_VISIT_STORE_FROM_REMINDER"),
                        Map.of("title", "Request Callback", "payload", "BIRTHDAY_TDAY_CALLBACK_FROM_REMINDER")
                )
        );
    }

    private void sendBirthdayTDayExit(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Hope you had a wonderful birthday, " + firstName + ". 🎂\n\n"
                        + "The Titan World family is always here when you're ready.\n\n"
                        + "Visit us anytime - no birthday needed.",
                List.of(
                        Map.of("title", "Visit us anytime", "payload", "BIRTHDAY_TDAY_VISIT_ANYTIME")
                )
        );
    }

    // ---------------------------------------------------------------------
    // Anniversary T-10 messages
    // ---------------------------------------------------------------------

    private void sendAnniversaryT10MonthConfirmation(String phone, String firstName, String anniversaryMonth) {
        String monthText = (anniversaryMonth == null || anniversaryMonth.isBlank()) ? "this" : anniversaryMonth;

        karixApiService.sendButtonMessage(
                phone,
                "✨ 💍 *Anniversary Month Confirmation*\n\n"
                        + "Hi *" + firstName + "*,\n\n"
                        + "Our records show that *" + monthText + "* is your anniversary month. 🎉\n\n"
                        + "👉 *Can you confirm this for us?*",
                List.of(
                        Map.of("title", "Yes, correct", "payload", "ANNIVERSARY_T10_MONTH_CONFIRMED"),
                        Map.of("title", "No, not right", "payload", "ANNIVERSARY_T10_DATE_CORRECTION")
                )
        );
    }

    private void sendAnniversaryT10DateCorrectionAsk(String phone, String firstName) {
        karixApiService.sendTextMessage(
                phone,
                "No worries, " + firstName + "!\n"
                        + "Could you help us update your anniversary date?\n"
                        + "Please reply with your anniversary date in this format:\n"
                        + "DD/MM/YYYY"
        );
    }

    private void sendAnniversaryT10Bridge(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "✨ 💍 *Thank you for confirming, " + firstName + "!*\n\n"
                        + "Your *anniversary month* is noted. 💖\n\n"
                        + "👉 *Would you still like to explore our curated collections?*",
                List.of(
                        Map.of("title", "Yes, show me", "payload", "ANNIVERSARY_T10_EXPLORE_COLLECTIONS"),
                        Map.of("title", "Maybe later", "payload", "ANNIVERSARY_T10_EXIT_AFTER_CONFIRMATION")
                )
        );
    }

    private void sendAnniversaryT10Opener(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "💍 *Great to hear from you, " + firstName + "!*\n\n"
                        + "We have got something special waiting for you from the *Titan World* collection - perfect for your *anniversary*.\n\n"
                        + "👉 *What would you like to do?*",
                List.of(
                        Map.of("title", "Find perfect gift", "payload", "ANNIVERSARY_T10_FIND_GIFT"),
                        Map.of("title", "Anniv offers", "payload", "ANNIVERSARY_T10_VIEW_OFFERS")
                )
        );
    }

    private void sendAnniversaryT10GenderSelection(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "💍 *Let's find the perfect watch for the occasion.*\n\n"
                        + "👉Browsing for:",
                List.of(
                        Map.of("title", "Men's collection", "payload", "ANNIVERSARY_T10_MENS_COLLECTION"),
                        Map.of("title", "Women's collection", "payload", "ANNIVERSARY_T10_WOMENS_COLLECTION"),
                        Map.of("title", "Couple Watches 💍", "payload", "ANNIVERSARY_T10_COUPLE_WATCHES")
                )
        );
    }

    private void sendAnniversaryT10CoupleWatchesCatalogue(String phone, String firstName) {
        karixApiService.sendTextMessage(
                phone,
                "💍 *Here are our Couple Watches collections, " + firstName + ".*\n\n"
                        + "Choose whose catalogue you'd like to explore - or both!\n\n"
                        + "📖 " + SAMPLE_ANNIVERSARY_PDF_URL
        );
    }

    private void sendAnniversaryT10Offer(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎁 *Here's your anniversary offer, " + firstName + "*\n\n"
                        + "Up to *10% off* on selected Titan brands - yours to use for *21 days before and after your anniversary*.\n\n"
                        + "Your nearest *Titan World* store is ready for you.",
                List.of(
                        Map.of("title", "Book store visit", "payload", "ANNIVERSARY_T10_OFFER_BOOK_STORE"),
                        Map.of("title", "Request callback", "payload", "ANNIVERSARY_T10_OFFER_CALLBACK")
                )
        );
    }

    private void sendAnniversaryT10CatalogueFollowUp(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "👀 *Did anything catch your eye, " + firstName + "?*\n\n"
                        + "We can help you get it - visit a nearby *Titan* store or have one of our experts call you.",
                List.of(
                        Map.of("title", "Visit nearest store", "payload", "ANNIVERSARY_T10_CATALOGUE_VISIT_STORE"),
                        Map.of("title", "Request callback", "payload", "ANNIVERSARY_T10_CATALOGUE_CALLBACK")
                )
        );
    }

    private void sendAnniversaryT10StoreVisit(String phone) {
        karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);
        taskScheduler.schedule(() -> sendAnniversaryT10StoreHelp(phone), Instant.now().plusSeconds(2));
    }

    private void sendAnniversaryT10StoreHelp(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Book appointment", "payload", "ANNIVERSARY_T10_BOOK_APPOINTMENT")                )
        );
    }

    private void sendAnniversaryT10CallbackConfirmation(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection.",
                List.of(
                        Map.of("title", "Explore again", "payload", "ANNIVERSARY_T10_EXPLORE_AGAIN")
                )
        );
    }


    private void sendAnniversaryTDayStoreHelpAfterStoreLocator(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Request Callback", "payload", "ANNIVERSARY_TDAY_CALLBACK_AFTER_STORE_VISIT")
                )
        );
    }

    private void sendAnniversaryTDayStoreHelpAfterReminderStoreVisit(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Request Callback", "payload", "ANNIVERSARY_TDAY_CALLBACK_AFTER_REMINDER_STORE")
                )
        );
    }

    private void sendAnniversaryT10GracefulExit(String phone, String firstName) {
        karixApiService.sendTextMessage(
                phone,
                "💍 No worries, " + firstName + "!\nWe're here whenever you're ready."
        );
    }

    // ---------------------------------------------------------------------
    // Anniversary T-Day messages
    // ---------------------------------------------------------------------

    private void sendAnniversaryTDayBenefitIntro(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "💍 *Anniversary Benefit Activated*\n\n"
                        + "Hi *" + firstName + "*, our records show that your annual account benefit has been updated.\n\n"
                        + "A *10% discount code* is now active on your profile and can be redeemed at the nearest *Titan World* store.\n\n"
                        + "This benefit is valid for the next *21 days*.\n\n"
                        + "Tap below:",
                List.of(
                        Map.of("title", "View Account Benefit", "payload", "ANNIVERSARY_TDAY_VIEW_BENEFIT"),
                        Map.of("title", "Locate Nearest Store", "payload", "ANNIVERSARY_TDAY_LOCATE_STORE_FROM_BENEFIT")
                )
        );
    }

    private void sendAnniversaryTDayWish(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎉 *HAPPY ANNIVERSARY, " + firstName + "!*\n\n"
                        + "May this day be filled with exceptional moments - and many more years of beautiful time together.\n\n"
                        + "Enjoy up to *10% discount* on your purchase at the nearest *Titan World* Store for the next *21 days*.\n\n"
                        + "Once again, a very happy anniversary from all of us at *Titan World*!",
                List.of(
                        Map.of("title", "Locate Store", "payload", "ANNIVERSARY_TDAY_LOCATE_STORE_FROM_WISH")
                )
        );
    }

    private void sendAnniversaryTDayStoreHelp(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Request Callback", "payload", "ANNIV_REQUEST_CALLBACK")
                )
        );
    }

    private void sendAnniversaryTDayCallbackConfirmation(String phone) {
        karixApiService.sendTextMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection."
        );
    }

    private void sendAnniversaryTDayFinalReminder(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "⏳ *Your anniversary offer is still live, " + firstName + ".*\n\n"
                        + "A few hours left today - and we'd hate for you to miss it.\n\n"
                        + "Visit your nearest store or book an appointment with an expert.",
                List.of(
                        Map.of("title", "Visit Store Today", "payload", "ANNIVERSARY_TDAY_VISIT_STORE_FROM_REMINDER"),
                        Map.of("title", "Request Callback", "payload", "ANNIVERSARY_TDAY_CALLBACK_FROM_REMINDER")
                )
        );
    }

    private void sendAnniversaryTDayExit(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Hope you had a wonderful anniversary, " + firstName + ". 💍\n\n"
                        + "The Titan World family is always here when you're ready.\n\n"
                        + "Visit us anytime - no anniversary needed.",
                List.of(
                        Map.of("title", "Visit us anytime", "payload", "ANNIVERSARY_TDAY_VISIT_ANYTIME")
                )
        );
    }

    // ---------------------------------------------------------------------
    // Catalogue / Carousel common utility used by separated flow handlers.
    // Payload routing remains flow-specific above.
    // ---------------------------------------------------------------------

    private boolean sendBrandCarousel(String phone, String firstName, String gender) {
        List<BrandCarouselCard> cards =
                brandCarouselCardRepository.findByGenderIgnoreCaseAndActiveTrueOrderByDisplayOrderAscIdAsc(gender);

        log.info("Brand carousel cards from DB gender={} count={}", gender, cards.size());

        return karixApiService.sendBrandCarouselMessageFromDbCards(
                phone,
                firstName,
                gender,
                cards
        );
    }

    private void sendCatalogue(String phone, String firstName, String gender, String brandKey) {
        String brandName = toBrandDisplayName(brandKey);
        String catalogueUrl = karixApiService.getCatalogueUrl(gender, brandKey);

        String message =
                "📖 *Here's the " + brandName + " collection, " + firstName + ".*\n\n"
                        + "Take your time browsing - we'll check back in a moment.\n\n"
                        + "🔗 " + catalogueUrl;

        log.info("SENDING CATALOGUE phone={} gender={} brandKey={} url={}",
                phone, gender, brandKey, catalogueUrl);

        karixApiService.sendTextMessage(phone, message);
    }


    private void sendCatalogue(
            String phone,
            String firstName,
            String gender,
            String brandKey,
            String catalogueUrl
    ) {
        String brandName = toBrandDisplayName(brandKey);

        String message =
                "📖 *Here's the " + brandName + " collection, " + firstName + ".*\n\n"
                        + "Take your time browsing - we'll check back in a moment.\n\n"
                        + "🔗 " + catalogueUrl;

        log.info("SENDING CATALOGUE phone={} gender={} brandKey={} url={}",
                phone, gender, brandKey, catalogueUrl);

        karixApiService.sendTextMessage(phone, message);
    }


    private String toBrandDisplayName(String brandKey) {
        if (brandKey == null || brandKey.isBlank()) {
            return "Titan";
        }

        return switch (brandKey) {
            case "TITAN_EDGE" -> "Titan Edge";
            case "TITAN_STELLAR" -> "Titan Stellar";
            case "TITAN_AUTOMATIC" -> "Titan Automatic";
            case "XYLYS" -> "Xylys";
            case "TITAN_DIVERS" -> "Titan Divers";
            case "TITAN_SMART" -> "Titan Smart";
            case "TITAN_RAGA" -> "Titan Raga";
            case "FASTRACK" -> "Fastrack";
            case "TITAN" -> "Titan";
            default -> brandKey.replace("_", " ");
        };
    }

    private String extractShortBrandCode(String payload) {
        if (payload == null || payload.isBlank()) {
            return "TTN";
        }

        String[] parts = payload.split("_");

        for (int i = 0; i < parts.length; i++) {
            if (("M".equals(parts[i]) || "W".equals(parts[i])) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }

        return parts.length >= 3 ? parts[2] : "TTN";
    }
    private String shortBrandCodeToBrandKey(String brandCode) {
        if (brandCode == null || brandCode.isBlank()) {
            return "TITAN";
        }

        return switch (brandCode) {
            case "EDG" -> "TITAN_EDGE";
            case "STL" -> "TITAN_STELLAR";
            case "ATM" -> "TITAN_AUTOMATIC";
            case "XYL" -> "XYLYS";
            case "DIV" -> "TITAN_DIVERS";
            case "SMT" -> "TITAN_SMART";
            case "RAG" -> "TITAN_RAGA";
            case "FST" -> "FASTRACK";
            case "TTN" -> "TITAN";
            default -> "TITAN";
        };
    }

    // ---------------------------------------------------------------------
    // Schedulers - meaningful names.
    // ---------------------------------------------------------------------

    private void scheduleBirthdayT10CatalogueFollowUp(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);
            if (latest == null || !isCurrentStep(latest, STEP_BIRTHDAY_T10_CATALOGUE_SENT)) {
                return;
            }

            karixApiService.sendButtonMessage(
                    phone,
                    "Did anything catch your eye, " + firstName + "? 👀\n"
                            + "We can help you get it - visit a nearby Titan World store or have one of our experts call you.",
                    List.of(
                            Map.of("title", "Visit nearest store", "payload", "BIRTHDAY_T10_CATALOGUE_VISIT_STORE"),
                            Map.of("title", "Request callback", "payload", "BIRTHDAY_T10_CATALOGUE_CALLBACK")
                    )
            );
            saveStep(latest, STEP_BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_SENT);
        }, Instant.now().plusSeconds(BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_DELAY_SECONDS));
    }

    private void scheduleAnniversaryT10CatalogueFollowUp(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);
            if (latest == null || !isAnniversaryT10CatalogueSentBeforeFollowUp(safeStep(latest))) {
                return;
            }

            sendAnniversaryT10CatalogueFollowUp(phone, firstName);
            saveStep(latest, STEP_ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_SENT);
        }, Instant.now().plusSeconds(ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_DELAY_SECONDS));
    }

    private void scheduleBirthdayTDayStoreHelp(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);
            if (latest == null || !isCurrentStep(latest, STEP_BIRTHDAY_TDAY_STORE_LOCATOR_SENT)) {
                return;
            }

            sendBirthdayTDayStoreHelpAfterStoreLocator(phone);
            saveStep(latest, STEP_BIRTHDAY_TDAY_STORE_HELP_SENT);
        }, Instant.now().plusSeconds(BIRTHDAY_TDAY_STORE_HELP_DELAY_SECONDS));
    }

    private void scheduleBirthdayTDayFinalReminder(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);
            if (latest == null || !isCurrentStep(latest, STEP_BIRTHDAY_TDAY_CALLBACK_CONFIRMED)) {
                return;
            }

            sendBirthdayTDayFinalReminder(phone, firstName);
            saveStep(latest, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_SENT);
        }, Instant.now().plusSeconds(BIRTHDAY_TDAY_FINAL_REMINDER_DELAY_SECONDS));
    }

    private void scheduleBirthdayTDayExit(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);
            if (latest == null || !isCurrentStep(latest, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED)) {
                return;
            }

            sendBirthdayTDayExit(phone, firstName);

            saveStep(latest, STEP_BIRTHDAY_TDAY_EXIT_MESSAGE_SENT);
        }, Instant.now().plusSeconds(BIRTHDAY_TDAY_EXIT_DELAY_SECONDS));
    }

    private void scheduleAnniversaryTDayStoreHelp(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);

            if (latest == null) {
                return;
            }

            if (!isCurrentStep(latest, STEP_ANNIVERSARY_TDAY_STORE_LOCATOR_SENT)) {
                return;
            }

            sendAnniversaryTDayStoreHelpAfterStoreLocator(phone);
            saveStep(latest, STEP_ANNIVERSARY_TDAY_STORE_HELP_SENT);

        }, Instant.now().plusSeconds(ANNIVERSARY_TDAY_STORE_HELP_DELAY_SECONDS));
    }

    private void scheduleAnniversaryTDayFinalReminder(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);

            if (latest == null) {
                return;
            }

            if (!isCurrentStep(latest, STEP_ANNIVERSARY_TDAY_CALLBACK_CONFIRMED)) {
                return;
            }

            sendAnniversaryTDayFinalReminder(phone, firstName);
            saveStep(latest, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_SENT);

        }, Instant.now().plusSeconds(ANNIVERSARY_TDAY_FINAL_REMINDER_DELAY_SECONDS));
    }

    private void scheduleAnniversaryTDayExit(String phone, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = getLatestSession(phone);

            if (latest == null) {
                return;
            }

            if (!isCurrentStep(latest, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_CALLBACK_CONFIRMED)) {
                return;
            }

            sendAnniversaryTDayExit(phone, firstName);
            saveStep(latest, STEP_ANNIVERSARY_TDAY_EXIT_MESSAGE_SENT);

        }, Instant.now().plusSeconds(ANNIVERSARY_TDAY_EXIT_DELAY_SECONDS));
    }

    // ---------------------------------------------------------------------
    // Step helper checks - includes legacy aliases only to avoid breaking
    // already-active sessions. New sessions save only meaningful names above.
    // ---------------------------------------------------------------------

    private boolean isBirthdayT10MonthConfirmationStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_CONFIRMATION_SENT, "BIRTHDAY_CONFIRM_DETAILS");
    }

    private boolean isBirthdayT10DobPendingStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_DOB_PENDING, "DOB_CORRECTION_PENDING");
    }

    private boolean isBirthdayT10BridgeStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_BRIDGE_SENT, "DOB_CONFIRMED", "BIRTHDAY_MONTH_CONFIRMED");
    }

    private boolean isBirthdayT10OpenerStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_OPENER_SENT, "CHATBOT_OPENER", STEP_WELCOME_SENT);
    }

    private boolean isBirthdayT10GenderSelectionStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_GENDER_SELECTION_SENT, "GENDER_SELECTION");
    }

    private boolean isBirthdayT10BrandCarouselStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_MEN_BRAND_CAROUSEL_SENT, STEP_BIRTHDAY_T10_WOMEN_BRAND_CAROUSEL_SENT,
                "MEN_BRAND_CAROUSEL", "WOMEN_BRAND_CAROUSEL");
    }

    private boolean isBirthdayT10CatalogueOrOfferStep(String step) {
        return isStep(step,
                STEP_BIRTHDAY_T10_CATALOGUE_SENT,
                STEP_BIRTHDAY_T10_CATALOGUE_FOLLOW_UP_SENT,
                STEP_BIRTHDAY_T10_OFFER_SENT,
                STEP_BIRTHDAY_T10_STORE_VISIT_SENT,
                "CATALOGUE_SENT",
                "STORE_VISIT_SENT",
                "BIRTHDAY_OFFER_PATH"
        );
    }

    private boolean isBirthdayT10CallbackOrExitStep(String step) {
        return isStep(step, STEP_BIRTHDAY_T10_CALLBACK_CONFIRMED, STEP_BIRTHDAY_T10_GRACEFUL_EXIT, "CALLBACK_CONFIRMED", "CALLBACK_REQUESTED");
    }

    private boolean isBirthdayTDayTemplateStep(String step) {
        return isStep(step, STEP_BIRTHDAY_TDAY_TEMPLATE_SENT, "T_DAY_TEMPLATE_SENT");
    }

    private boolean isBirthdayTDayFinalReminderStep(String step) {
        return isStep(step, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_SENT, "STEP_7_SENT");
    }

    private boolean isBirthdayTDayFinalReminderStoreStep(String step) {
        return isStep(step, STEP_BIRTHDAY_TDAY_FINAL_REMINDER_STORE_SENT, "STEP_7_STORE_SENT");
    }

    private boolean isBirthdayTDayActiveStep(String step) {
        return isStep(step,
                STEP_BIRTHDAY_TDAY_TEMPLATE_SENT,
                STEP_BIRTHDAY_TDAY_WISH_SENT,
                STEP_BIRTHDAY_TDAY_STORE_LOCATOR_SENT,
                STEP_BIRTHDAY_TDAY_STORE_HELP_SENT,
                STEP_BIRTHDAY_TDAY_CALLBACK_CONFIRMED,
                STEP_BIRTHDAY_TDAY_FINAL_REMINDER_SENT,
                STEP_BIRTHDAY_TDAY_FINAL_REMINDER_STORE_SENT,
                "T_DAY_TEMPLATE_SENT",
                "BIRTHDAY_WISH_SENT",
                "T_DAY_STORE_LOCATOR_SENT",
                "STEP_6B_SENT",
                "CALLBACK_CONFIRMED",
                "STEP_7_SENT",
                "STEP_7_STORE_SENT"
        );
    }

    private boolean isAnniversaryT10MonthConfirmationStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_CONFIRMATION_SENT, "ANNIVERSARY_CONFIRMATION_SENT");
    }

    private boolean isAnniversaryT10DatePendingStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_DATE_PENDING, "ANNIVERSARY_DATE_PENDING");
    }

    private boolean isAnniversaryT10BridgeStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_BRIDGE_SENT, "ANNIVERSARY_BRIDGE_SENT", "ANNIVERSARY_MONTH_CONFIRMED", "ANNIVERSARY_DATE_CONFIRMED");
    }

    private boolean isAnniversaryT10OpenerStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_OPENER_SENT, "ANNIVERSARY_OPENER");
    }

    private boolean isAnniversaryT10GenderSelectionStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_GENDER_SELECTION_SENT, "ANNIVERSARY_GENDER_SELECTION");
    }

    private boolean isAnniversaryT10BrandCarouselStep(String step) {
        return isStep(step,
                STEP_ANNIVERSARY_T10_MEN_BRAND_CAROUSEL_SENT,
                STEP_ANNIVERSARY_T10_WOMEN_BRAND_CAROUSEL_SENT,
                "ANNIVERSARY_MEN_BRAND_CAROUSEL",
                "ANNIVERSARY_WOMEN_BRAND_CAROUSEL"
        );
    }

    private boolean isAnniversaryT10CatalogueSentBeforeFollowUp(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_CATALOGUE_SENT, STEP_ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT, "ANNIVERSARY_CATALOGUE_SENT", "ANNIVERSARY_COUPLE_CATALOGUE_SENT");
    }

    private boolean isAnniversaryT10CatalogueFollowUpStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_SENT, "ANNIVERSARY_STEP_4A_SENT");
    }

    private boolean isAnniversaryT10CatalogueOrOfferStep(String step) {
        return isStep(step,
                STEP_ANNIVERSARY_T10_CATALOGUE_SENT,
                STEP_ANNIVERSARY_T10_COUPLE_CATALOGUE_SENT,
                STEP_ANNIVERSARY_T10_CATALOGUE_FOLLOW_UP_SENT,
                STEP_ANNIVERSARY_T10_OFFER_SENT,
                STEP_ANNIVERSARY_T10_STORE_VISIT_SENT,
                "ANNIVERSARY_CATALOGUE_SENT",
                "ANNIVERSARY_COUPLE_CATALOGUE_SENT",
                "ANNIVERSARY_STEP_4A_SENT",
                "ANNIVERSARY_OFFERS_SENT",
                "ANNIVERSARY_STORE_VISIT_SENT"
        );
    }

    private boolean isAnniversaryT10CallbackOrExitStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_T10_CALLBACK_CONFIRMED, STEP_ANNIVERSARY_T10_GRACEFUL_EXIT, "ANNIVERSARY_CALLBACK_CONFIRMED");
    }

    private boolean isAnniversaryTDayTemplateStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_TDAY_TEMPLATE_SENT, "ANNIVERSARY_T_DAY_TEMPLATE_SENT");
    }

    private boolean isAnniversaryTDayFinalReminderStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_SENT, "ANNIVERSARY_T_DAY_STEP_7_SENT");
    }

    private boolean isAnniversaryTDayFinalReminderStoreStep(String step) {
        return isStep(step, STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_STORE_SENT, "ANNIVERSARY_T_DAY_STEP_7_STORE_SENT");
    }

    private boolean isAnniversaryTDayActiveStep(String step) {
        return isStep(step,
                STEP_ANNIVERSARY_TDAY_TEMPLATE_SENT,
                STEP_ANNIVERSARY_TDAY_WISH_SENT,
                STEP_ANNIVERSARY_TDAY_STORE_LOCATOR_SENT,
                STEP_ANNIVERSARY_TDAY_STORE_HELP_SENT,
                STEP_ANNIVERSARY_TDAY_CALLBACK_CONFIRMED,
                STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_SENT,
                STEP_ANNIVERSARY_TDAY_FINAL_REMINDER_STORE_SENT,
                "ANNIVERSARY_T_DAY_TEMPLATE_SENT",
                "ANNIVERSARY_T_DAY_WISH_SENT",
                "ANNIVERSARY_T_DAY_STORE_LOCATOR_SENT",
                "ANNIVERSARY_T_DAY_STEP_6B_SENT",
                "ANNIVERSARY_T_DAY_CALLBACK_CONFIRMED",
                "ANNIVERSARY_T_DAY_STEP_7_SENT",
                "ANNIVERSARY_T_DAY_STEP_7_STORE_SENT"
        );
    }

    // ---------------------------------------------------------------------
    // Payload helpers
    // ---------------------------------------------------------------------

    private String getCleanPayload(String text, String payload) {
        String cleanPayload = normalizePayload(payload);
        if (cleanPayload.isBlank()) {
            cleanPayload = normalizePayload(text);
        }
        return cleanPayload;
    }

    private String normalizePayload(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim()
                .replace('’', '\'')
                .toUpperCase(Locale.ENGLISH)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private boolean isPayload(String cleanPayload, String... expectedPayloads) {
        if (cleanPayload == null || cleanPayload.isBlank()) {
            return false;
        }

        for (String expected : expectedPayloads) {
            if (cleanPayload.equalsIgnoreCase(expected)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMensCollectionPayload(String cleanPayload) {
        return isPayload(cleanPayload, "MEN_S_COLLECTION", "MENS_COLLECTION", "MEN_COLLECTION", "MENS");
    }

    private boolean isWomensCollectionPayload(String cleanPayload) {
        return isPayload(cleanPayload, "WOMEN_S_COLLECTION", "WOMENS_COLLECTION", "WOMEN_COLLECTION", "WOMENS");
    }

    private boolean isStoreVisitPayload(String cleanPayload) {
        return isPayload(cleanPayload,
                "VISIT_NEAREST_STORE",
                "VISIT_STORE",
                "BOOK_STORE_VISIT",
                "NEARBY_STORE",
                "PICK_UP_STORE",
                "STORE_VISIT",
                "FIND_STORE_NEAR_ME"
        ) || (cleanPayload != null && cleanPayload.startsWith("BOOK_STORE_VISIT_"));
    }

    private boolean isCallbackPayload(String cleanPayload) {
        return isPayload(cleanPayload,
                "REQUEST_CALLBACK",
                "REQUEST_A_CALLBACK",
                "BOOK_AN_APPOINTMENT",
                "INTERESTED",
                "SPEAK_WITH_EXPERT"
        ) || (cleanPayload != null && cleanPayload.startsWith("REQUEST_CALLBACK_"));
    }

    private boolean isExploreCarouselPayload(String cleanPayload) {
        return cleanPayload != null && (cleanPayload.startsWith("EX_M_") || cleanPayload.startsWith("EX_W_"));
    }

    private boolean isCallbackCarouselPayload(String cleanPayload) {
        return cleanPayload != null && (cleanPayload.startsWith("CB_M_") || cleanPayload.startsWith("CB_W_"));
    }

    // ---------------------------------------------------------------------
    // Customer / session / persistence helpers
    // ---------------------------------------------------------------------

    private String getFirstName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return "there";
        }
        return customerName.trim().split("\\s+")[0];
    }

    private boolean isValidDate(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        try {
            LocalDate.parse(text.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String formatMonthName(String month) {
        if (month == null || month.isBlank()) {
            return "this";
        }

        String clean = month.trim().replace("_", " ").toLowerCase(Locale.ENGLISH);
        return clean.substring(0, 1).toUpperCase(Locale.ENGLISH) + clean.substring(1);
    }

    private String getAnniversaryMonthForMessage(Long customerId) {
        if (customerId == null) {
            return "this";
        }

        return customerRepository.findById(customerId)
                .map(customer -> {
                    if (customer.getAnniversaryMonth() != null && !customer.getAnniversaryMonth().isBlank()) {
                        return formatMonthName(customer.getAnniversaryMonth());
                    }

                    if (customer.getAnniversaryDate() != null) {
                        return formatMonthName(customer.getAnniversaryDate().getMonth().name());
                    }

                    return "this";
                })
                .orElse("this");
    }

    private void updateCustomerBirthday(Long customerId, LocalDate dob) {
        if (customerId == null || dob == null) {
            return;
        }

        customerRepository.findById(customerId).ifPresent(customer -> {
            customer.setDateOfBirth(dob);
            customer.setBirthdayMonth(dob.getMonth().name());
            customerRepository.save(customer);
        });
    }

    private void updateCustomerAnniversary(Long customerId, LocalDate anniversaryDate) {
        if (customerId == null || anniversaryDate == null) {
            return;
        }

        customerRepository.findById(customerId).ifPresent(customer -> {
            customer.setAnniversaryDate(anniversaryDate);
            customer.setAnniversaryMonth(anniversaryDate.getMonth().name());
            customerRepository.save(customer);
        });
    }

    private void saveIncomingMessage(String phone, String text, String payload, Long customerId, String sourceStep) {
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
        message.setStepName(sourceStep == null || sourceStep.isBlank() ? "INCOMING" : sourceStep);
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
                    session.setCurrentStep(STEP_WELCOME);
                    session.setIsActive(true);
                    session.setSessionStart(LocalDateTime.now());
                    session.setLastActivity(LocalDateTime.now());
                    return botSessionRepository.save(session);
                });
    }

    private BotSession getLatestSession(String phone) {
        return botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);
    }

    private void saveStep(BotSession session, String step) {
        session.setCurrentStep(step);
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);
    }

    private boolean isCurrentStep(BotSession session, String step) {
        return session != null && isStep(session.getCurrentStep(), step);
    }

    private String safeStep(BotSession session) {
        if (session == null || session.getCurrentStep() == null) {
            return "";
        }
        return session.getCurrentStep();
    }

    private boolean isStep(String currentStep, String... expectedSteps) {
        if (currentStep == null || currentStep.isBlank()) {
            return false;
        }

        for (String expected : expectedSteps) {
            if (currentStep.equalsIgnoreCase(expected)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // Interface methods / welcome fallback
    // ---------------------------------------------------------------------

    @Override
    public void sendWelcomeMessage(String phone, String customerName, BotSession session) {
        handleWelcome(phone, customerName, session);
    }

    @Override
    public void sendBirthdayDayMessage(String phone, String customerName, BotSession session) {
        String firstName = getFirstName(customerName);
        sendBirthdayTDayBenefitIntro(phone, firstName);
        saveStep(session, STEP_BIRTHDAY_TDAY_TEMPLATE_SENT);
    }

    private void handleWelcome(String phone, String customerName, BotSession session) {
        String name = customerName == null || customerName.isBlank() ? "there" : customerName;

        karixApiService.sendButtonMessage(
                phone,
                "Happy to hear from you, *" + name + "!* 🎂\n\n"
                        + "Your birthday month is here — and we've put together something from the Titan collection just for you.\n\n"
                        + "What would you like to do?",
                List.of(
                        Map.of("title", "Find my watch", "payload", "FIND_BIRTHDAY_WATCH"),
                        Map.of("title", "Birthday offers", "payload", "BIRTHDAY_OFFERS")
                )
        );

        saveStep(session, STEP_WELCOME_SENT);
    }
}
