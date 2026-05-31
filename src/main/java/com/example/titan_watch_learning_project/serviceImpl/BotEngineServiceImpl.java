package com.example.titan_watch_learning_project.serviceImpl;
import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.CustomerRepository;
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

import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.entity.WatchProduct;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import com.example.titan_watch_learning_project.service.ProductCatalogService;


@Slf4j
@Service
@RequiredArgsConstructor
public class BotEngineServiceImpl implements BotEngineService {

    private final MessageRepository messageRepository;
    private final BotSessionRepository botSessionRepository;
    private final KarixApiServiceImpl karixApiService;
private  final ProductCatalogService productCatalogService;
    private final CustomerRepository customerRepository;
    private final TaskScheduler taskScheduler;




    private void sendDobConfirmationButtons(String phone, String firstName) {
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
    private void sendTDayAccountBenefitIntro(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎉 *Birthday Benefit Activated*\n\n"
                        + "Hi *" + firstName + "*, our records show that your annual account benefit has been updated.\n\n"
                        + "A *10% discount code* is now active on your profile and can be redeemed at the nearest *Titan World* store.\n\n"
                        + "This benefit is valid for the next *21 days*.\n\n"
                        + "Tap below:",
                List.of(
                        Map.of("title", "View Account Benefit", "payload", "VIEW_ACCOUNT_BENEFIT"),
                        Map.of("title", "Locate Nearest Store", "payload", "LOCATE_NEAREST_STORE")
                )
        );
    }

    private void sendCallbackConfirmationWithExplore(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection.",
                List.of(
                        Map.of("title", "Explore Again", "payload", "EXPLORE_AGAIN")
                )
        );
    }

    private void sendAnniversaryConfirmationButtons(String phone, String firstName, String anniversaryMonth) {
        String monthText = (anniversaryMonth == null || anniversaryMonth.isBlank())
                ? "this"
                : anniversaryMonth;

        karixApiService.sendButtonMessage(
                phone,
                "✨ 💍 *Anniversary Month Confirmation*\n\n"
                        + "Hi *" + firstName + "*,\n\n"
                        + "Our records show that *" + monthText + "* is your anniversary month. 🎉\n\n"
                        + "👉 *Can you confirm this for us?*" + "* is your anniversary month.\n\n"
                        + "Can you confirm this for us?",
                List.of(
                        Map.of("title", "✅Yes, correct", "payload", "ANNIVERSARY_MONTH_YES"),
                        Map.of("title", "❌No, not right", "payload", "ANNIVERSARY_MONTH_NO")
                )
        );
    }
    private void sendAnniversaryBridgeMessage(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "✨ 💍 *Thank you for confirming, " + firstName + "!*\n\n"
                        + "Your *anniversary month* is noted. 💖\n\n"
                        + "👉 *Would you still like to explore our curated collections?*",
                List.of(
                        Map.of("title", "✅Yes, show me", "payload", "YES_SHOW_ME"),
                        Map.of("title", "❌No, maybe later", "payload", "NO_MAYBE_LATER")
                )
        );
    }
    private void sendAnniversaryOpener(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "✨ 💍 *Great to hear from you, " + firstName + "!*\n\n"
                        + "We have got something special waiting for you from the *Titan World* collection - perfect for your *anniversary*. 🎁\n\n"
                        + "👉 *What would you like to do?*",
                List.of(
                        Map.of("title", "🔍 Find perfect gift", "payload", "FIND_ANNIVERSARY_GIFT"),
                        Map.of("title", "🎉 Anniv offers", "payload", "SEE_ANNIVERSARY_OFFERS")                )
        );
    }



    private void sendAnniversaryGenderSelection(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "💍 *Let's find the perfect watch for the occasion.*\n\n"
                        + "👉Browsing for:",
                List.of(
                        Map.of("title", "Men's collection", "payload", "MENS_COLLECTION"),
                        Map.of("title", "Women's collection", "payload", "WOMENS_COLLECTION"),
                        Map.of("title", "Couple watches", "payload", "COUPLE_WATCHES")
                )
        );
    }

    private void sendCoupleWatchesCatalogue(String phone, String firstName) {
        karixApiService.sendTextMessage(
                phone,
                "💍 *Here are our Couple Watches collections, " + firstName + ".*\n\n"
                        + "Choose whose catalogue you'd like to explore - or both!\n\n"
                        + "📖 " + SAMPLE_ANNIVERSARY_PDF_URL
        );
    }


    private void sendAnniversaryCatalogueFollowUp(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "👀 *Did anything catch your eye, " + firstName + "?*\n\n"
                        + "We can help you get it - visit a nearby *Titan* store or have one of our experts call you.",
                List.of(
                        Map.of("title", "Visit nearest store", "payload", "VISIT_NEAREST_STORE"),
                        Map.of("title", "Request callback", "payload", "REQUEST_CALLBACK")
                )
        );
    }

    private void sendAnniversaryStoreHelpMessage(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Book appointment", "payload", "BOOK_AN_APPOINTMENT")
                )
        );
    }

    private void sendAnniversaryCallbackConfirmation(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection.",
                List.of(
                        Map.of("title", "Explore again", "payload", "EXPLORE_AGAIN")
                )
        );
    }


    private void sendAnniversaryOffers(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "💍 *Anniversary Offers for you, " + firstName + "*\n\n"
                        + "Celebrate your occasion with curated Titan World picks and exclusive store benefits.\n\n"
                        + "Would you like to visit a nearby store or speak with an expert?",
                List.of(
                        Map.of("title", "Visit nearest store", "payload", "VISIT_NEAREST_STORE"),
                        Map.of("title", "Request callback", "payload", "REQUEST_CALLBACK")
                )
        );
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


    private void scheduleAnniversaryStep4a(String phone, Long customerId, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);

            if (latest == null) {
                return;
            }

            if (!"ANNIVERSARY_CATALOGUE_SENT".equalsIgnoreCase(latest.getCurrentStep())
                    && !"ANNIVERSARY_COUPLE_CATALOGUE_SENT".equalsIgnoreCase(latest.getCurrentStep())) {
                return;
            }

            sendAnniversaryCatalogueFollowUp(phone, firstName);

            latest.setCurrentStep("ANNIVERSARY_STEP_4A_SENT");
            latest.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(latest);

        }, Instant.now().plusSeconds(ANNIVERSARY_STEP_4A_DELAY_SECONDS));
    }


    public void processIncomingMessage(
            String phone,
            String text,
            String payload,
            Long customerId,
            String customerName
    ) {
        log.info("processMessage phone={} payload={} text={}", phone, payload, text);

        // ✅ User ka har incoming message DB me store hoga.
        saveIncomingMessage(phone, text, payload, customerId);

        BotSession session = getOrCreateSession(phone, customerId);
        String firstName = getFirstName(customerName);

        String cleanPayload = normalizePayload(payload);
        String cleanText = normalizePayload(text);

        if (cleanPayload.isBlank()) {
            cleanPayload = cleanText;
        }

        log.info("Current session step for {}: {} cleanPayload={}",
                phone, session.getCurrentStep(), cleanPayload);



        // ----------------------------------------------------
// ANNIVERSARY DATE CORRECTION PENDING
// ----------------------------------------------------
        if ("ANNIVERSARY_DATE_PENDING".equalsIgnoreCase(session.getCurrentStep())) {
            if (isValidDob(text)) {
                LocalDate anniversaryDate = LocalDate.parse(text.trim(), DOB_FORMATTER);

                customerRepository.findById(customerId).ifPresent(customer -> {
                    customer.setAnniversaryDate(anniversaryDate);
                    customer.setAnniversaryMonth(anniversaryDate.getMonth().name());
                    customerRepository.save(customer);
                });

                session.setCurrentStep("ANNIVERSARY_DATE_CONFIRMED");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);


//                if ("ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)
//                        || "ANNIVERSARY_STORE_VISIT_SENT".equalsIgnoreCase(previousStep)
//                        || "ANNIVERSARY_CATALOGUE_SENT".equalsIgnoreCase(previousStep)
//                        || "ANNIVERSARY_COUPLE_CATALOGUE_SENT".equalsIgnoreCase(previousStep)) {
//
//                    sendAnniversaryCallbackConfirmation(phone);
//
//                    session.setCurrentStep("ANNIVERSARY_CALLBACK_CONFIRMED");
//                    session.setLastActivity(LocalDateTime.now());
//                    botSessionRepository.save(session);
//                    return;
//                }


                sendAnniversaryBridgeMessage(phone, firstName);
                return;
            }

            karixApiService.sendTextMessage(
                    phone,
                    "Please reply with your anniversary date in this format:\nDD/MM/YYYY"
            );
            return;
        }

        // ----------------------------------------------------
        // DOB CORRECTION PENDING
        // Isko session create hone ke baad aur welcome se pehle rakhna hai.
        // ----------------------------------------------------
        if ("DOB_CORRECTION_PENDING".equalsIgnoreCase(session.getCurrentStep())) {
            if (isValidDob(text)) {
                LocalDate dob = LocalDate.parse(text.trim(), DOB_FORMATTER);

                customerRepository.findById(customerId).ifPresent(customer -> {
                    customer.setDateOfBirth(dob);
                    customer.setBirthdayMonth(dob.getMonth().name());
                    customerRepository.save(customer);
                });

                session.setCurrentStep("DOB_CONFIRMED");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);

                sendBridgeMessage(phone, firstName);
                return;
            }

            karixApiService.sendTextMessage(
                    phone,
                    "Please reply with your date of birth in this format:\nDD/MM/YYYY"
            );
            return;
        }
// ----------------------------------------------------
// FALLBACK TEMPLATE TESTING FLOW
// User manually types/clicks: Confirm Details
// Future approved fallback template CTA payload: CONFIRM_DETAILS
// ----------------------------------------------------
        if ("CONFIRM_DETAILS".equals(cleanPayload)
                || "CONFIRM_DETAIL".equals(cleanPayload)
                || "CONFIRM".equals(cleanPayload)) {

            session.setCurrentStep("BIRTHDAY_CONFIRM_DETAILS");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            sendDobConfirmationButtons(phone,firstName);
            return;
        }

        // ----------------------------------------------------
// ANNIVERSARY YES / NO HANDLING
// ----------------------------------------------------
        if ("ANNIVERSARY_CONFIRMATION_SENT".equalsIgnoreCase(session.getCurrentStep())) {

            if ("YES_THAT_S_CORRECT".equals(cleanPayload)
                    || "YES_THATS_CORRECT".equals(cleanPayload)
                    || "YES_CORRECT".equals(cleanPayload)
                    || "ANNIVERSARY_MONTH_YES".equals(cleanPayload)) {

                session.setCurrentStep("ANNIVERSARY_MONTH_CONFIRMED");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);

                sendAnniversaryBridgeMessage(phone, firstName);
                return;
            }

            if ("NO_THAT_S_NOT_RIGHT".equals(cleanPayload)
                    || "NO_THATS_NOT_RIGHT".equals(cleanPayload)
                    || "NO_NOT_RIGHT".equals(cleanPayload)
                    || "ANNIVERSARY_MONTH_NO".equals(cleanPayload)) {

                session.setCurrentStep("ANNIVERSARY_DATE_PENDING");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);

                karixApiService.sendTextMessage(
                        phone,
                        "No worries, " + firstName + "!\n"
                                + "Could you help us update your anniversary date?\n"
                                + "Please reply with your anniversary date in this format:\n"
                                + "DD/MM/YYYY"
                );
                return;
            }
        }

// ----------------------------------------------------
// ANNIVERSARY TESTING START
// User manually types: Anniversary
// Future approved template CTA payloads:
// Yes -> ANNIVERSARY_MONTH_YES
// No  -> ANNIVERSARY_MONTH_NO
// ----------------------------------------------------
        if ("ANNIVERSARY".equals(cleanPayload)
                || "ANNIVARY".equals(cleanPayload)
                || "ANNIVERSARY_FLOW".equals(cleanPayload)) {

            session.setCurrentStep("ANNIVERSARY_CONFIRMATION_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            sendAnniversaryConfirmationButtons(phone, firstName, getAnniversaryMonthForMessage(customerId));
            return;
        }


        // ----------------------------------------------------
        // T10 TESTING START
        // User manually types: Yes, that's correct / No, that's not right
        // Future approved template payloads:
        // Yes -> BIRTHDAY_MONTH_YES
        // No  -> BIRTHDAY_MONTH_NO
        // ----------------------------------------------------
        if ("YES_THAT_S_CORRECT".equals(cleanPayload)
                || "YES_THATS_CORRECT".equals(cleanPayload)
                || "BIRTHDAY_MONTH_YES".equals(cleanPayload)) {

            session.setCurrentStep("BIRTHDAY_MONTH_CONFIRMED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            sendChatbotOpener(phone, firstName);
            return;
        }

        if ("NO_THAT_S_NOT_RIGHT".equals(cleanPayload)
                || "NO_THATS_NOT_RIGHT".equals(cleanPayload)
                || "BIRTHDAY_MONTH_NO".equals(cleanPayload)) {

            session.setCurrentStep("DOB_CORRECTION_PENDING");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            karixApiService.sendTextMessage(
                    phone,
                    "No worries, " + firstName + "!\n"
                            + "Could you help us update your birthday?\n"
                            + "Please reply with your date of birth in this format:\n"
                            + "DD/MM/YYYY"
            );
            return;
        }

        // ----------------------------------------------------
        // STEP 0b bridge message buttons
        // ----------------------------------------------------
        if ("YES_SHOW_ME".equals(cleanPayload)
                || "SHOW_ME".equals(cleanPayload)) {

            if ("ANNIVERSARY_BRIDGE_SENT".equalsIgnoreCase(session.getCurrentStep())
                    || "ANNIVERSARY_MONTH_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())
                    || "ANNIVERSARY_DATE_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())) {

                sendAnniversaryOpener(phone, firstName);

                session.setCurrentStep("ANNIVERSARY_OPENER");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);
                return;
            }

            sendChatbotOpener(phone, firstName);

            session.setCurrentStep("CHATBOT_OPENER");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        if ("NO_MAYBE_LATER".equals(cleanPayload)
                || "MAYBE_LATER".equals(cleanPayload)) {

            if ("ANNIVERSARY_BRIDGE_SENT".equalsIgnoreCase(session.getCurrentStep())
                    || "ANNIVERSARY_MONTH_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())
                    || "ANNIVERSARY_DATE_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())) {

                karixApiService.sendTextMessage(
                        phone,
                        "💍 No worries, " + firstName + "!\n"
                                + "We're here whenever you're ready."
                );

                session.setCurrentStep("ANNIVERSARY_GRACEFUL_EXIT");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);
                return;
            }

            karixApiService.sendTextMessage(
                    phone,
                    "No worries, " + firstName + "! 🎂 We're here whenever you're ready."
            );

            session.setCurrentStep("GRACEFUL_EXIT_T10");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // T-DAY TESTING START
        // User manually types: View Account Benefit / Locate Nearest Store
        // Future approved template payloads:
        // View Account Benefit -> VIEW_ACCOUNT_BENEFIT
        // Locate Nearest Store -> LOCATE_NEAREST_STORE
        // ----------------------------------------------------

        if ("HAPPY_BIRTHDAY".equals(cleanPayload)
                || "HAPPY_BDAY".equals(cleanPayload)
                || "BIRTHDAY_DAY".equals(cleanPayload)) {

            sendTDayAccountBenefitIntro(phone, firstName);

            session.setCurrentStep("T_DAY_TEMPLATE_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }


        if ("VIEW_ACCOUNT_BENEFIT".equals(cleanPayload)
                || "VIEW_BENEFIT".equals(cleanPayload)) {

            sendBirthdayWish(phone, firstName);

            session.setCurrentStep("BIRTHDAY_WISH_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        if ("LOCATE_NEAREST_STORE".equals(cleanPayload)) {

            sendStoreHelpBirthdayMessage(phone);

            session.setCurrentStep("T_DAY_STORE_LOCATOR_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }


        // ----------------------------------------------------
// ANNIVERSARY STEP 1
// ----------------------------------------------------
        if ("FIND_ANNIVERSARY_GIFT".equals(cleanPayload)
                || "FIND_PERFECT_GIFT".equals(cleanPayload)
                || "FIND_GIFT".equals(cleanPayload)) {

            sendAnniversaryGenderSelection(phone);

            session.setCurrentStep("ANNIVERSARY_GENDER_SELECTION");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        if ("SEE_ANNIVERSARY_OFFERS".equals(cleanPayload)
                || "ANNIVERSARY_OFFERS".equals(cleanPayload)) {

            sendAnniversaryOffers(phone, firstName);

            session.setCurrentStep("ANNIVERSARY_OFFERS_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }


        // ----------------------------------------------------
        // STEP 1: Find my perfect watch
        // ----------------------------------------------------
        if ("FIND_MY_PERFECT_WATCH".equals(cleanPayload)
                || "FIND_PERFECT_WATCH".equals(cleanPayload)
                || "FIND_BIRTHDAY_WATCH".equals(cleanPayload)
                || "FIND_WATCH".equals(cleanPayload)
                || "FIND_MY_WATCH".equals(cleanPayload)
                || "FIND_MY_BIRTHDAY_WATCH".equals(cleanPayload)) {

            sendGenderSelection(phone);

            session.setCurrentStep("GENDER_SELECTION");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // STEP 1: See birthday offers
        // ----------------------------------------------------
        if ("SEE_BIRTHDAY_OFFERS".equals(cleanPayload)
                || "BIRTHDAY_OFFERS".equals(cleanPayload)
                || "EXCLUSIVE_BIRTHDAY_OFFERS".equals(cleanPayload)) {

            sendBirthdayOfferPath(phone, firstName);

            session.setCurrentStep("BIRTHDAY_OFFER_PATH");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // STEP 2: Men's collection -> brand carousel
        // ----------------------------------------------------
        if ("MEN_S_COLLECTION".equals(cleanPayload)
                || "MENS_COLLECTION".equals(cleanPayload)
                || "MEN_COLLECTION".equals(cleanPayload)
                || "MENS".equals(cleanPayload)) {

            boolean sent = sendBrandCarousel(phone, firstName, "MEN");

            if (!sent) {
                karixApiService.sendTextMessage(
                        phone,
                        "Sorry, collections could not be loaded right now. Please try again."
                );
                return;
            }

            if ("ANNIVERSARY_GENDER_SELECTION".equalsIgnoreCase(session.getCurrentStep())) {
                session.setCurrentStep("ANNIVERSARY_MEN_BRAND_CAROUSEL");
            } else {
                session.setCurrentStep("MEN_BRAND_CAROUSEL");
            }

            session.setSelectedCollection(BotSession.Collection.MENS);
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // STEP 2: Women's collection -> brand carousel
        // ----------------------------------------------------
        if ("WOMEN_S_COLLECTION".equals(cleanPayload)
                || "WOMENS_COLLECTION".equals(cleanPayload)
                || "WOMEN_COLLECTION".equals(cleanPayload)
                || "WOMENS".equals(cleanPayload)) {

            boolean sent = sendBrandCarousel(phone, firstName, "WOMEN");

            if (!sent) {
                karixApiService.sendTextMessage(
                        phone,
                        "Sorry, collections could not be loaded right now. Please try again."
                );
                return;
            }
            if ("ANNIVERSARY_GENDER_SELECTION".equalsIgnoreCase(session.getCurrentStep())) {
                session.setCurrentStep("ANNIVERSARY_WOMEN_BRAND_CAROUSEL");
            } else {
                session.setCurrentStep("WOMEN_BRAND_CAROUSEL");
            }

            session.setSelectedCollection(BotSession.Collection.WOMENS);
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
// ANNIVERSARY COUPLE WATCHES
// ----------------------------------------------------
        if ("COUPLE_WATCHES".equals(cleanPayload)
                || "COUPLE_WATCHES_💍".equals(cleanPayload)
                || "COUPLE".equals(cleanPayload)) {

            sendCoupleWatchesCatalogue(phone, firstName);

            session.setCurrentStep("ANNIVERSARY_COUPLE_CATALOGUE_SENT");
            session.setSelectedCollection(BotSession.Collection.COUPLES);
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            scheduleAnniversaryStep4a(phone, customerId, firstName);
            return;
        }


        // ----------------------------------------------------
        // STEP 3: Brand carousel Explore
        // Payload: EXPLORE_MEN_TITAN_EDGE / EXPLORE_WOMEN_TITAN_RAGA
        // ----------------------------------------------------
        if (cleanPayload.startsWith("EX_M_")
                || cleanPayload.startsWith("EX_W_")) {

            log.info("EXPLORE COLLECTION CLICKED phone={} payload={}", phone, cleanPayload);

            String previousStep = session.getCurrentStep();

            String gender = cleanPayload.startsWith("EX_M_") ? "MEN" : "WOMEN";
            String brandCode = extractShortBrandCode(cleanPayload);
            String brandKey = shortBrandCodeToBrandKey(brandCode);

            log.info("EXPLORE resolved gender={} brandCode={} brandKey={}",
                    gender, brandCode, brandKey);

            sendCatalogue(phone, firstName, gender, brandKey);

            if ("ANNIVERSARY_MEN_BRAND_CAROUSEL".equalsIgnoreCase(previousStep)
                    || "ANNIVERSARY_WOMEN_BRAND_CAROUSEL".equalsIgnoreCase(previousStep)) {

                session.setCurrentStep("ANNIVERSARY_CATALOGUE_SENT");
                session.setSelectedCollection(
                        "MEN".equals(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS
                );
                session.setSelectedBrand(brandKey);
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);

                scheduleAnniversaryStep4a(phone, customerId, firstName);
                return;
            }

            session.setCurrentStep("CATALOGUE_SENT");
            session.setSelectedCollection(
                    "MEN".equals(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS
            );
            session.setSelectedBrand(brandKey);
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            scheduleStep4a(phone, customerId, firstName);
            return;
        }

        // ----------------------------------------------------
        // STEP 3: Brand carousel callback
        // Payload: CALLBACK_MEN_TITAN_EDGE / CALLBACK_WOMEN_TITAN_RAGA
        // ----------------------------------------------------
     if (cleanPayload.startsWith("CB_M_")
             || cleanPayload.startsWith("CB_W_")) {

         String previousStep = session.getCurrentStep();

        String gender = cleanPayload.startsWith("CB_M_") ? "MEN" : "WOMEN";
        String brandCode = extractShortBrandCode(cleanPayload);
        String brandKey = shortBrandCodeToBrandKey(brandCode);

        session.setCurrentStep("CALLBACK_REQUESTED");
         session.setSelectedCollection(
                 "MEN".equals(gender) ? BotSession.Collection.MENS : BotSession.Collection.WOMENS
         );

         session.setSelectedBrand(brandKey);
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);

        karixApiService.sendButtonMessage(
                phone,
                "Done. Our team will be in touch.\n\n"
                        + "One of our Titan experts will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection.",
                List.of(
                        Map.of("title", "Explore again", "payload", "EX_" + ("MEN".equals(gender) ? "M" : "W") + "_" + brandCode + "_0")
                )
        );
        return;
    }

        // ----------------------------------------------------
        // STEP 4a / STEP 5 / Store visit
        // ----------------------------------------------------
        if ("VISIT_NEAREST_STORE".equals(cleanPayload)
                || "VISIT_STORE".equals(cleanPayload)
                || "BOOK_STORE_VISIT".equals(cleanPayload)
                || cleanPayload.startsWith("BOOK_STORE_VISIT_")
                || "NEARBY_STORE".equals(cleanPayload)
                || "PICK_UP_STORE".equals(cleanPayload)
                || "STORE_VISIT".equals(cleanPayload)
                || "FIND_STORE_NEAR_ME".equals(cleanPayload)) {

            String previousStep = session.getCurrentStep();

            if ("ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)) {

                sendAnniversaryStoreHelpMessage(phone);

                session.setCurrentStep("ANNIVERSARY_STORE_VISIT_SENT");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);
                return;
            }

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);

            sendStoreHelpMessage(phone);

            session.setCurrentStep("STORE_VISIT_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // Birthday wish locate store button
        // ----------------------------------------------------
        if ("LOCATE_STORE".equals(cleanPayload)
                || "LOCATE_NEAREST_STORE_FROM_WISH".equals(cleanPayload)) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);

            sendStoreHelpBirthdayMessage(phone);

            session.setCurrentStep("T_DAY_STORE_LOCATOR_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            return;
        }

        // ----------------------------------------------------
        // STEP 7 Visit Store Today
        // ----------------------------------------------------
        if ("VISIT_STORE_TODAY".equals(cleanPayload)) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);

            sendStoreHelpBirthdayMessage(phone);

            session.setCurrentStep("STEP_7_STORE_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // Callback / Book appointment
        // ----------------------------------------------------
        if ("REQUEST_CALLBACK".equals(cleanPayload)
                || cleanPayload.startsWith("REQUEST_CALLBACK_")
                || "REQUEST_A_CALLBACK".equals(cleanPayload)
                || "BOOK_AN_APPOINTMENT".equals(cleanPayload)
                || "INTERESTED".equals(cleanPayload)
                || "SPEAK_WITH_EXPERT".equals(cleanPayload)) {


            String previousStep = session.getCurrentStep();


            if ("ANNIVERSARY_STEP_4A_SENT".equalsIgnoreCase(previousStep)
                    || "ANNIVERSARY_STORE_VISIT_SENT".equalsIgnoreCase(previousStep)
                    || "ANNIVERSARY_CATALOGUE_SENT".equalsIgnoreCase(previousStep)
                    || "ANNIVERSARY_COUPLE_CATALOGUE_SENT".equalsIgnoreCase(previousStep)) {

                sendAnniversaryCallbackConfirmation(phone);

                session.setCurrentStep("ANNIVERSARY_CALLBACK_CONFIRMED");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);
                return;
            }




            sendCallbackConfirmation(phone);

            // Step 7 ke baad user callback click kare,
            // then Step 8 scheduler trigger hoga.
            if ("STEP_7_SENT".equalsIgnoreCase(previousStep)
                    || "STEP_7_STORE_SENT".equalsIgnoreCase(previousStep)) {

                session.setCurrentStep("STEP_7_CALLBACK_CONFIRMED");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);

                scheduleStep8(phone, customerId, firstName);
                return;
            }

            // T-Day Step 6b ke baad callback click kare,
            // then Step 7 scheduler trigger hoga.
            session.setCurrentStep("CALLBACK_CONFIRMED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);

            if ("STEP_6B_SENT".equalsIgnoreCase(previousStep)
                    || "T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(previousStep)
                    || "BIRTHDAY_WISH_SENT".equalsIgnoreCase(previousStep)) {

                scheduleStep7(phone, customerId, firstName);
            }

            return;
        }

        // ----------------------------------------------------
        // Explore again -> Step 2
        // ----------------------------------------------------
        if ("EXPLORE_AGAIN".equals(cleanPayload)
                || "BROWSE_AGAIN".equals(cleanPayload)
                || "EXPLORE_OTHER_STYLES".equals(cleanPayload)) {

            if ("ANNIVERSARY_CALLBACK_CONFIRMED".equalsIgnoreCase(session.getCurrentStep())) {
                sendAnniversaryGenderSelection(phone);

                session.setCurrentStep("ANNIVERSARY_GENDER_SELECTION");
                session.setLastActivity(LocalDateTime.now());
                botSessionRepository.save(session);
                return;
            }

            sendGenderSelection(phone);

            session.setCurrentStep("GENDER_SELECTION");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // STEP 8 Visit us anytime
        // ----------------------------------------------------
        if ("VISIT_US_ANYTIME".equals(cleanPayload)) {

            karixApiService.sendTextMessage(phone, STORE_LOCATOR_URL);

            session.setCurrentStep("FLOW_ENDED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // ----------------------------------------------------
        // OLD EXISTING FUNCTIONALITY FALLBACK
        // Agar koi old payload aata hai toh break nahi hoga.
        // ----------------------------------------------------

        if (cleanPayload.isBlank()
                || "HI".equals(cleanPayload)
                || "HELLO".equals(cleanPayload)
                || "START".equals(cleanPayload)) {

            handleWelcome(phone, customerName, session);
            return;
        }

        if ("STYLE_MINIMAL_CHIC".equals(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_MINIMAL_CHIC", "Minimal & Chic");
            return;
        }

        if ("STYLE_BOLD_EDGY".equals(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_BOLD_EDGY", "Bold & Edgy");
            return;
        }

        if ("STYLE_LUXE_CLASSY".equals(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_LUXE_CLASSY", "Luxe & Classy");
            return;
        }

        if ("STYLE_SPORTY_ADVENTUROUS".equals(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_SPORTY_ADVENTUROUS", "Sporty & Adventurous");
            return;
        }

        if ("SEE_BY_PRICE".equals(cleanPayload)) {
            sendPriceSelection(phone);

            String collectionType = getCollectionFromSession(session);
            String style = getStyleFromSession(session);

            session.setCurrentStep("PRICE_SELECTION_" + collectionType + "_" + style);
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        if ("PRICE_BELOW_10K".equals(cleanPayload)
                || "PRICE_2K_5K".equals(cleanPayload)
                || "PRICE_5K_10K".equals(cleanPayload)
                || "PRICE_10K_25K".equals(cleanPayload)
                || "PRICE_25K_PLUS".equals(cleanPayload)) {

            handlePriceSelected(phone, session, cleanPayload);
            return;
        }

        if ("DOWNLOAD_CATALOGUE".equals(cleanPayload)) {
            karixApiService.sendImageButtonMessage(
                    phone,
                    "https://www.titan.co.in/dw/image/v2/BKDD_PRD/on/demandware.static/-/Library-Sites-TitanSharedLibrary/default/dwd83ec67e/images/homepage/All_Banners/WYS_D.jpg",
                    "🎂 *Titan Birthday Collection 2026*\n\nExplore our exclusive birthday watch catalogue.\n\n🔗 Visit: www.titan.co.in",
                    List.of(
                            Map.of("title", "Explore Now", "payload", "BROWSE_COLLECTION"),
                            Map.of("title", "Callback", "payload", "REQUEST_CALLBACK")
                    )
            );
            session.setCurrentStep("CATALOGUE_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        if ("EXPLORE_WEBSITE".equals(cleanPayload)
                || "BROWSE_COLLECTION".equals(cleanPayload)) {

            karixApiService.sendTextMessage(
                    phone,
                    "Explore Titan’s full collection here: https://www.titan.co.in"
            );

            session.setCurrentStep("WEBSITE_REDIRECTED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        log.info("No matching payload found. Starting welcome flow for phone={}", phone);
        handleWelcome(phone, customerName, session);
    }





    private String getFirstName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return "there";
        }

        return customerName.trim().split("\\s+")[0];
    }

    private boolean isValidDob(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        try {
            LocalDate.parse(text.trim(), DOB_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void sendBridgeMessage(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Thank you for confirming, " + firstName + "! 🎂\n"
                        + "Your birthday month is noted.\n\n"
                        + "Would you still like to explore our curated collections?",
                List.of(
                        Map.of("title", "Yes, show me", "payload", "YES_SHOW_ME"),
                        Map.of("title", "No, maybe later", "payload", "NO_MAYBE_LATER")
                )
        );
    }


    private void sendChatbotOpener(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Great to hear from you, *" + firstName + "*! 🎂\n"
                        + "We have got something special waiting for you from the *Titan World* collection. ✨\n"
                        + "👉 *What would you like to do?*",
                List.of(
                        Map.of("title", "🔍 Find my watch", "payload", "FIND_MY_PERFECT_WATCH"),
                        Map.of("title", "🎁 Birthday offers", "payload", "SEE_BIRTHDAY_OFFERS")
                )
        );
    }

    private void sendGenderSelection(String phone) {
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

    private boolean sendBrandCarousel(String phone, String firstName, String gender) {
    if ("MEN".equalsIgnoreCase(gender)) {
        return karixApiService.sendBrandCarouselMessage(phone, firstName, "MEN", MEN_BRANDS);
    }

    return karixApiService.sendBrandCarouselMessage(phone, firstName, "WOMEN", WOMEN_BRANDS);
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

    // Expected:
    // EX_M_EDG_0
    // CB_W_RAG_1
    if (parts.length >= 3) {
        return parts[2];
    }

    return "TTN";
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


    private void sendCatalogueFollowUp(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Did anything catch your eye, " + firstName + "? 👀\n"
                        + "We can help you get it - visit a nearby Titan World store or have one of our experts call you.",
                List.of(
                        Map.of("title", "Visit nearest store", "payload", "VISIT_NEAREST_STORE"),
                        Map.of("title", "Request a callback", "payload", "REQUEST_CALLBACK")
                )
        );
    }
    private void sendStoreHelpMessage(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "Hope you found your nearest store. If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Book appointment", "payload", "BOOK_AN_APPOINTMENT")
                )
        );
    }

    private void sendStoreHelpBirthdayMessage(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "📍 *Store Visit Assistance*\n\n"
                        + "Hope you found your nearest store.\n\n"
                        + "If you need help, our expert can call you and book an appointment.",
                List.of(
                        Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK")
                )
        );
    }
    private void sendCallbackConfirmation(String phone) {
        karixApiService.sendTextMessage(
                phone,
                "✅ *Done. Our team will be in touch.*\n\n"
                        + "One of our *Titan experts* will call you to help you with the one that caught your eye.\n\n"
                        + "In the meantime, feel free to see the curated collection."
        );
    }
    private void sendBirthdayOfferPath(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Here's your birthday offer, " + firstName + " 🎁\n\n"
                        + "Up to 10% off on selected Titan brands - yours to use for 21 days before and after your birthday.\n\n"
                        + "Your nearest Titan store is ready for you.",
                List.of(
                        Map.of("title", "Book store visit", "payload", "VISIT_NEAREST_STORE"),
                        Map.of("title", "Request callback", "payload", "REQUEST_CALLBACK")
                )
        );
    }

    private void sendBirthdayWish(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "🎉 *HAPPY BIRTHDAY, " + firstName + "!*\n\n"
                        + "May your special day be filled with exceptional moments.\n\n"
                        + "Enjoy up to *10% discount* on your purchase at the nearest *Titan World* store for the next *21 days*.\n\n"
                        + "Once again, a very happy birthday from all of us at *Titan World*!",
                List.of(
                        Map.of("title", "Locate Store", "payload", "LOCATE_STORE")
                )
        );
    }
    private void sendBirthdayFollowUp(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "⏳ *Your birthday offer is still live, " + firstName + ".*\n\n"
                        + "A few hours left today — and we'd hate for you to miss it.\n\n"
                        + "Visit your nearest store or book an appointment with an expert.",
                List.of(
                        Map.of("title", "Visit Store Today", "payload", "VISIT_STORE_TODAY"),
                        Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK")
                )
        );
    }

    private void sendBirthdayExit(String phone, String firstName) {
        karixApiService.sendButtonMessage(
                phone,
                "Hope you had a wonderful birthday, " + firstName + ". 🎂\n\n"
                        + "The Titan World family is always here when you're ready.\n\n"
                        + "Visit us anytime - no birthday needed.",
                List.of(
                        Map.of("title", "Visit us anytime", "payload", "VISIT_US_ANYTIME")
                )
        );
    }

    private boolean isBirthdayDayFlow(BotSession session) {
        if (session == null || session.getCurrentStep() == null) {
            return false;
        }

        String step = session.getCurrentStep();

        return step.startsWith("T_DAY")
                || "BIRTHDAY_WISH_SENT".equalsIgnoreCase(step)
                || "STEP_6B_SENT".equalsIgnoreCase(step);
    }



    private String mapStylePayloadToDbStyle(String stylePayload) {
        if ("STYLE_MINIMAL_CHIC".equalsIgnoreCase(stylePayload)) {
            return "Minimal & Chic";
        }

        if ("STYLE_BOLD_EDGY".equalsIgnoreCase(stylePayload)) {
            return "Bold & Edgy";
        }

        if ("STYLE_LUXE_CLASSY".equalsIgnoreCase(stylePayload)) {
            return "Luxe & Classy";
        }

        if ("STYLE_SPORTY_ADVENTUROUS".equalsIgnoreCase(stylePayload)) {
            return "Sporty & Adventurous";
        }

        return "";
    }

    private String normalizePayload(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("’", "")
                .replace("'", "");
    }



    private void scheduleStep4a(String phone, Long customerId, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);

            if (latest == null) {
                return;
            }

            if (!"CATALOGUE_SENT".equalsIgnoreCase(latest.getCurrentStep())) {
                return;
            }

            sendCatalogueFollowUp(phone, firstName);

            latest.setCurrentStep("CATALOGUE_FOLLOW_UP_SENT");
            latest.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(latest);

        }, Instant.now().plusSeconds(STEP_4A_DELAY_SECONDS));
    }

    private void scheduleStep6b(String phone, Long customerId, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);

            if (latest == null) {
                return;
            }

            if (!"T_DAY_STORE_LOCATOR_SENT".equalsIgnoreCase(latest.getCurrentStep())) {
                return;
            }

            sendStoreHelpBirthdayMessage(phone);

            latest.setCurrentStep("STEP_6B_SENT");
            latest.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(latest);

        }, Instant.now().plusSeconds(STEP_6B_DELAY_SECONDS));
    }

    private void scheduleStep7(String phone, Long customerId, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);

            if (latest == null) {
                return;
            }

            if (!"CALLBACK_CONFIRMED".equalsIgnoreCase(latest.getCurrentStep())) {
                return;
            }

            sendBirthdayFollowUp(phone, firstName);

            latest.setCurrentStep("STEP_7_SENT");
            latest.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(latest);

        }, Instant.now().plusSeconds(STEP_7_DELAY_SECONDS));
    }

    private void scheduleStep8(String phone, Long customerId, String firstName) {
        taskScheduler.schedule(() -> {
            BotSession latest = botSessionRepository.findTopByPhoneOrderByLastActivityDesc(phone).orElse(null);

            if (latest == null) {
                return;
            }

            if (!"STEP_7_CALLBACK_CONFIRMED".equalsIgnoreCase(latest.getCurrentStep())) {
                return;
            }

            sendBirthdayExit(phone, firstName);

            latest.setCurrentStep("FLOW_ENDED");
            latest.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(latest);

        }, Instant.now().plusSeconds(STEP_8_DELAY_SECONDS));
    }
    private String getStyleFromSession(BotSession session) {
        String step = session.getCurrentStep();

        if (step == null) {
            return "";
        }

        if (step.contains("STYLE_MINIMAL_CHIC")) {
            return "STYLE_MINIMAL_CHIC";
        }

        if (step.contains("STYLE_BOLD_EDGY")) {
            return "STYLE_BOLD_EDGY";
        }

        if (step.contains("STYLE_LUXE_CLASSY")) {
            return "STYLE_LUXE_CLASSY";
        }

        if (step.contains("STYLE_SPORTY_ADVENTUROUS")) {
            return "STYLE_SPORTY_ADVENTUROUS";
        }

        return "";
    }




    private void sendProductRecommendations(
            String phone,
            List<WatchProduct> products,
            String collectionType,
            String priceBucket
    ) {
        if (products == null || products.isEmpty()) {
            karixApiService.sendTextMessage(
                    phone,
                    "Sorry, we could not find watches in this price range right now."
            );

            karixApiService.sendButtonMessage(
                    phone,
                    "Would you like to continue?",
                    List.of(
                            Map.of("title", "Callback", "payload", "REQUEST_CALLBACK"),
                            Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
                    )
            );
            return;
        }

        boolean carouselSent = karixApiService.sendCarouselMessage(phone, products);

        if (carouselSent) {
            log.info("Carousel sent successfully to phone={} productCount={}", phone, products.size());
            return;
        }

        log.error("Carousel failed for phone={}. Not sending old individual cards because fallback is disabled.", phone);

        karixApiService.sendButtonMessage(
                phone,
                "Sorry, product carousel could not be loaded right now.\n\nWould you like our expert to help you?",
                List.of(
                        Map.of("title", "Callback", "payload", "REQUEST_CALLBACK"),
                        Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
                )
        );
    }



    private String getCollectionFromSession(BotSession session) {
        String step = session.getCurrentStep();

        if (step == null) {
            return "MALE";
        }



        if (step.contains("FEMALE") || step.contains("WOMEN")) {
            return "FEMALE";
        }

        if (step.contains("COUPLES")) {
            return "COUPLES";
        }

        return "MALE";
    }





    private String mapPricePayloadToBucket(String pricePayload) {
        if (pricePayload == null || pricePayload.isBlank()) {
            return "";
        }

        if ("PRICE_BELOW_10K".equalsIgnoreCase(pricePayload)) {
            return "PRICE_BELOW_10K";
        }

        return pricePayload.trim().toUpperCase();
    }






    private void handlePriceSelected(String phone, BotSession session, String pricePayload) {
        String collectionType = getCollectionFromSession(session);
        String priceBucket = mapPricePayloadToBucket(pricePayload);

        if (collectionType == null || collectionType.isBlank()) {
            collectionType = "MALE";
        }

        log.info("Fetching products collectionType={} pricePayload={} priceBucket={}",
                collectionType, pricePayload, priceBucket);

        List<WatchProduct> products;

        if ("PRICE_BELOW_10K".equalsIgnoreCase(pricePayload)
                || "PRICE_2K_5K".equalsIgnoreCase(pricePayload)
                || "PRICE_5K_10K".equalsIgnoreCase(pricePayload)) {

            products = productCatalogService.getProductsByCollectionAndPriceBuckets(
                    collectionType,
                    List.of("PRICE_2K_5K", "PRICE_5K_10K")
            );

            priceBucket = "PRICE_BELOW_10K";
        } else {
            products = productCatalogService.getProductsByCollectionAndPrice(
                    collectionType,
                    priceBucket
            );
        }

        if (products == null || products.isEmpty()) {
            karixApiService.sendTextMessage(
                    phone,
                    "Sorry, we could not find watches in this price range right now. Our team can help you personally."
            );

            karixApiService.sendButtonMessage(
                    phone,
                    "Would you like a callback?",
                    List.of(
                            Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK"),
                            Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
                    )
            );

            session.setCurrentStep("NO_PRODUCTS_FOUND");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        sendProductRecommendations(phone, products, collectionType, priceBucket);

        session.setCurrentStep("PRODUCTS_SENT_" + collectionType + "_" + priceBucket);
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);
    }




    private void sendPriceSelection(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "💎 *Let's also find the right fit for your budget.*\n\n"
                        + "Pick a price range:",
                List.of(
                        Map.of("title", "Below ₹10,000", "payload", "PRICE_BELOW_10K"),
                        Map.of("title", "₹10,000 - ₹25,000", "payload", "PRICE_10K_25K"),
                        Map.of("title", "Above ₹25,000", "payload", "PRICE_25K_PLUS")
                )
        );
    }

    private void handleStyleSelected(
            String phone,
            BotSession session,
            String stylePayload,
            String styleName
    ) {
        String collectionType = getCollectionFromSession(session);
        if (collectionType == null || collectionType.isBlank()) collectionType = "MALE";

        karixApiService.sendButtonMessage(
                phone,
                "🌟 *" + styleName + " — A perfect choice.*\n\n"
                        + "Your taste speaks volumes.\n\n"
                        + "Now let's find the watch that matches your budget — because luxury should feel right.",
                List.of(
                        Map.of("title", "💰 See by Price", "payload", "SEE_BY_PRICE"),
                        Map.of("title", "📞 Request Callback", "payload", "REQUEST_CALLBACK"),
                        Map.of("title", "📖 Catalogue", "payload", "DOWNLOAD_CATALOGUE")
                )
        );

        session.setCurrentStep("STYLE_SELECTED_" + collectionType + "_" + stylePayload);
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);
    }


    private void sendCollectionSelection(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "⌚ *Let's find the one for you.*\n\n"
                        + "Browsing for:",
                List.of(
                        Map.of("title", "Men's Collection", "payload", "MENS_COLLECTION"),
                        Map.of("title", "Women's Collection", "payload", "WOMENS_COLLECTION")
                )
        );
    }
    private void sendBirthdayOffers(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "🎁 *Here's what we have for your birthday —*\n\n"
                        + "🏷️ Special birthday discount on select collections\n"
                        + "👑 Exclusive early access to new arrivals\n"
                        + "🥂 In-store birthday celebration\n\n"
                        + "Walk in today — your nearest Titan store is ready for you.",
                List.of(
                        Map.of("title", "Book Store", "payload", "BOOK_STORE_VISIT"),
                        Map.of("title", "Callback", "payload", "REQUEST_CALLBACK")
                )
        );
    }

    private void sendStyleDemoBySession(String phone, BotSession session, String style) {
        String currentStep = session.getCurrentStep() == null ? "" : session.getCurrentStep();

        String gender;

        if ("STYLE_SELECTION_MEN".equalsIgnoreCase(currentStep)) {
            gender = "MEN";
        } else if ("STYLE_SELECTION_WOMEN".equalsIgnoreCase(currentStep)) {
            gender = "WOMEN";
        } else {
            // fallback demo ke liye
            gender = "MEN";
        }

        karixApiService.sendDemoStyleProductMessage(phone, gender, style);

        session.setCurrentStep("PRODUCT_DEMO_" + gender + "_" + style.toUpperCase().replace(" ", "_").replace("&", "AND"));
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);
    }

    @Override
    public void sendWelcomeMessage(String phone, String customerName, BotSession session) {

    }

    @Override
    public void sendBirthdayDayMessage(String phone, String customerName, BotSession session) {

    }





    private void handleWelcome(String phone, String customerName, BotSession session) {
        String name = customerName == null || customerName.isBlank()
                ? "there"
                : customerName;

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


    private static final String STORE_LOCATOR_URL = "https://www.titan.co.in/store-locator";



    private static final String SAMPLE_ANNIVERSARY_PDF_URL =
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";

    // TESTING DELAYS
// Production me:
// STEP_4A_DELAY_SECONDS = 30 * 60 ya 10 * 60
// STEP_6B_DELAY_SECONDS = 10 * 60
// STEP_7_DELAY_SECONDS = 4 * 60 * 60 ya 6 * 60 * 60
// STEP_8_DELAY_SECONDS = 4 * 60 * 60
    private static final long STEP_4A_DELAY_SECONDS = 10;
    private static final long STEP_6B_DELAY_SECONDS = 10;
    private static final long STEP_7_DELAY_SECONDS = 10;
    private static final long STEP_8_DELAY_SECONDS = 10;

    // Testing: 10 seconds.
// Production: 30-40 seconds as per document.
    private static final long ANNIVERSARY_STEP_4A_DELAY_SECONDS = 10;

    private static final DateTimeFormatter DOB_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);

    private static final List<Map<String, String>> MEN_BRANDS = List.of(
            Map.of("key", "TITAN_EDGE", "name", "Titan Edge"),
            Map.of("key", "TITAN_STELLAR", "name", "Titan Stellar"),
            Map.of("key", "TITAN_AUTOMATIC", "name", "Titan Automatic"),
            Map.of("key", "XYLYS", "name", "Xylys"),
            Map.of("key", "TITAN_DIVERS", "name", "Titan Divers"),
            Map.of("key", "TITAN", "name", "Titan"),
            Map.of("key", "TITAN_SMART", "name", "Titan Smart")
    );

    private static final List<Map<String, String>> WOMEN_BRANDS = List.of(
            Map.of("key", "TITAN_EDGE", "name", "Titan Edge"),
            Map.of("key", "TITAN_RAGA", "name", "Titan Raga"),
            Map.of("key", "TITAN_SMART", "name", "Titan Smart"),
            Map.of("key", "XYLYS", "name", "Xylys"),
            Map.of("key", "FASTRACK", "name", "Fastrack"),
            Map.of("key", "TITAN", "name", "Titan")
    );

}


//WEBHOOK_FORWARD_URL = https://quack-freestyle-slashed.ngrok-free.dev/webhook