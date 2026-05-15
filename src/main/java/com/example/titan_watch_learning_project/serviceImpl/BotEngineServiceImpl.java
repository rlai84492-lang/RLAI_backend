package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.entity.Watch;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.LeadRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.example.titan_watch_learning_project.repository.WatchRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import com.example.titan_watch_learning_project.service.KarixApiService;
import com.example.titan_watch_learning_project.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotEngineServiceImpl implements BotEngineService {

    private final BotSessionRepository sessionRepo;
    private final MessageRepository messageRepo;
    private final WatchRepository watchRepo;
    private final LeadRepository leadRepo;
    private final KarixApiService karixService;
    private final StoreService storeService;

    // =============================================
    // MAIN ENTRY POINT — called by WebhookService
    // =============================================
    @Override
    public void processIncomingMessage(String phone, String messageText,
                                       String buttonPayload, Long customerId,
                                       String customerName) {
        log.info("Processing message from: {} | text: {} | payload: {}",
                phone, messageText, buttonPayload);

        // Save every inbound message
        saveMessage(phone, customerId, null,
                Message.Direction.INBOUND, Message.MessageType.TEXT,
                messageText, buttonPayload, "INBOUND");

        // Get existing active session or create a fresh one
        // Get existing active session or create a fresh one
        BotSession session = sessionRepo
                .findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(phone)
                .orElseGet(() -> createNewSession(phone, customerId));

        String step = session.getCurrentStep();
        log.info("Current step for {}: {}", phone, step);

        switch (step) {
            case "WELCOME":
                handleWelcomeStep(phone, buttonPayload, session, customerName);
                break;
            case "COLLECTION":
                handleCollectionStep(phone, buttonPayload, session);
                break;
            case "STYLE":
                handleStyleStep(phone, buttonPayload, session);
                break;
            case "CAROUSEL":
                handleCarouselStep(phone, buttonPayload, session);
                break;
            case "PRICE_FILTER":
                handlePriceFilterStep(phone, buttonPayload, session);
                break;
            case "CALLBACK_CONFIRM":
                handleCallbackConfirm(phone, buttonPayload, session);
                break;
            case "OFFER":
                handleOfferStep(phone, buttonPayload, session);
                break;
            case "COMPLETED":
                // User messages again after completion — restart the journey
                sendWelcomeMessage(phone, customerName, session);
                break;
            default:
                sendWelcomeMessage(phone, customerName, session);
                break;
        }
    }

    // =============================================
    // STEP 1 — WELCOME
    // T-10 outbound trigger or any fresh conversation
    // =============================================
    @Override
    public void sendWelcomeMessage(String phone, String customerName, BotSession session) {
        String text = "Dear " + customerName + ", your birthday is just 10 days away! 🎂\n\n"
                + "At Titan, we would love to celebrate your special occasion with timeless "
                + "watches crafted for every moment.\n\n"
                + "Choose what you would like to explore today:";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Find My Watch", "payload", "FIND_WATCH"),
                Map.of("title", "Birthday Offers", "payload", "BIRTHDAY_OFFERS")
        );

        String mid = karixService.sendButtonMessage(phone, text, buttons);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                text, null, "WELCOME");

        session.setCurrentStep("WELCOME");
        sessionRepo.save(session);
    }

    private void handleWelcomeStep(String phone, String payload,
                                   BotSession session, String customerName) {
        String p = extractPayload(payload);
        switch (p) {
            case "FIND_WATCH":
                sendCollectionSelection(phone, session);
                break;
            case "BIRTHDAY_OFFERS":
                sendBirthdayOffers(phone, session);
                break;
            default:
                // Unrecognised input — re-show welcome
                sendWelcomeMessage(phone, customerName, session);
        }
    }

    // =============================================
    // STEP 2 — COLLECTION SELECTION
    // =============================================
    private void sendCollectionSelection(String phone, BotSession session) {
        String text = "Celebrate your special day with a watch that matches your personality! 🎉\n\n"
                + "Choose a collection:";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Men's Collection",   "payload", "MENS"),
                Map.of("title", "Women's Collection", "payload", "WOMENS")
        );

        String mid = karixService.sendButtonMessage(phone, text, buttons);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                text, null, "COLLECTION");

        session.setCurrentStep("COLLECTION");
        sessionRepo.save(session);
    }

    private void handleCollectionStep(String phone, String payload, BotSession session) {
        String p = extractPayload(payload);
        if ("MENS".equals(p) || "WOMENS".equals(p)) {
            session.setSelectedCollection(BotSession.Collection.valueOf(p));
            sessionRepo.save(session);
            sendStyleDiscovery(phone, session);
        } else {
            sendCollectionSelection(phone, session); // re-ask on invalid input
        }
    }

    // =============================================
    // STEP 3 — STYLE DISCOVERY
    // 4 options → use list message (WhatsApp max 3 for buttons)
    // =============================================
    private void sendStyleDiscovery(String phone, BotSession session) {
        List<Map<String, Object>> styleOptions = List.of(
                Map.of("id", "MINIMAL_CHIC",        "name", "Minimal & Chic",       "price", ""),
                Map.of("id", "BOLD_EDGY",            "name", "Bold & Edgy",           "price", ""),
                Map.of("id", "LUXE_CLASSY",          "name", "Luxe & Classy",         "price", ""),
                Map.of("id", "SPORTY_ADVENTUROUS",   "name", "Sporty & Adventurous",  "price", "")
        );

        String mid = karixService.sendCarouselMessage(
                phone, "Tell us your preferred style 👇", styleOptions);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                "Style selection", null, "STYLE");

        session.setCurrentStep("STYLE");
        sessionRepo.save(session);
    }

    private void handleStyleStep(String phone, String payload, BotSession session) {
        String p = extractPayload(payload);
        try {
            session.setSelectedStyle(BotSession.Style.valueOf(p));
            sessionRepo.save(session);
            sendProductCarousel(phone, session);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown style payload: {}", p);
            sendStyleDiscovery(phone, session); // re-ask on invalid
        }
    }

    // =============================================
    // STEP 4 — PRODUCT CAROUSEL
    // Up to 6 watches matched by collection + style
    // =============================================
    private void sendProductCarousel(String phone, BotSession session) {
        List<Watch> watches = watchRepo.findByCollectionAndStyleAndIsActiveTrue(
                Watch.Collection.valueOf(session.getSelectedCollection().name()),
                Watch.Style.valueOf(session.getSelectedStyle().name())
        );

        List<Watch> top6 = watches.stream().limit(6).collect(Collectors.toList());

        if (top6.isEmpty()) {
            karixService.sendTextMessage(phone,
                    "We are curating the perfect watches for your style. "
                            + "Our team will reach out shortly! ⌚");
            session.setCurrentStep("COMPLETED");
            sessionRepo.save(session);
            return;
        }

        List<Map<String, Object>> products = top6.stream().map(w -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",    w.getId().toString());
            m.put("name",  w.getName());
            m.put("price", w.getPrice());
            return m;
        }).collect(Collectors.toList());

        String header = session.getSelectedCollection().name()
                + " — " + session.getSelectedStyle().name().replace("_", " ");

        String mid = karixService.sendCarouselMessage(phone, header, products);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.CAROUSEL,
                header, null, "CAROUSEL");

        // Action buttons after the carousel
        String actionText = "What would you like to do? 👇";
        List<Map<String, String>> actionButtons = List.of(
                Map.of("title", "Get a Callback",     "payload", "CALLBACK"),
                Map.of("title", "Filter by Price",    "payload", "PRICE_FILTER"),
                Map.of("title", "Download Catalogue", "payload", "CATALOGUE")
        );
        String mid2 = karixService.sendButtonMessage(phone, actionText, actionButtons);
        saveMessage(phone, session.getCustomerId(), mid2,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                actionText, null, "CAROUSEL_ACTION");

        session.setCurrentStep("CAROUSEL");
        sessionRepo.save(session);
    }

    private void handleCarouselStep(String phone, String payload, BotSession session) {
        String p = extractPayload(payload);
        switch (p) {
            case "CALLBACK":
                sendCallbackConfirmation(phone, session);
                break;
            case "PRICE_FILTER":
                sendPriceFilter(phone, session);
                break;
            case "CATALOGUE":
                sendCatalogueLink(phone, session);
                break;
            case "RESTART_STYLE":
                sendStyleDiscovery(phone, session);
                break;
            default:
                // Unknown — re-show action buttons
                sendProductCarousel(phone, session);
        }
    }

    // =============================================
    // STEP 5 — PRICE FILTER
    // =============================================
    private void sendPriceFilter(String phone, BotSession session) {
        List<Map<String, Object>> options = List.of(
                Map.of("id", "2000-5000",     "name", "₹2,000 - ₹5,000",   "price", ""),
                Map.of("id", "5000-10000",    "name", "₹5,000 - ₹10,000",  "price", ""),
                Map.of("id", "10000-25000",   "name", "₹10,000 - ₹25,000", "price", ""),
                Map.of("id", "25000-999999",  "name", "Above ₹25,000",      "price", "")
        );

        String mid = karixService.sendCarouselMessage(
                phone, "Choose your preferred budget range 💰", options);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                "Price filter", null, "PRICE_FILTER");

        session.setCurrentStep("PRICE_FILTER");
        sessionRepo.save(session);
    }

    private void handlePriceFilterStep(String phone, String payload, BotSession session) {
        String priceRange = extractPayload(payload);

        // Must match "digits-digits" format
        if (!priceRange.matches("\\d+-\\d+")) {
            sendPriceFilter(phone, session);
            return;
        }

        session.setSelectedPriceRange(priceRange);
        sessionRepo.save(session);

        String[] parts    = priceRange.split("-");
        BigDecimal minPrice = new BigDecimal(parts[0]);
        BigDecimal maxPrice = new BigDecimal(parts[1]);

        List<Watch> watches = watchRepo.findByCollectionAndStyleAndPriceBetweenAndIsActiveTrue(
                Watch.Collection.valueOf(session.getSelectedCollection().name()),
                Watch.Style.valueOf(session.getSelectedStyle().name()),
                minPrice, maxPrice
        );

        if (watches.isEmpty()) {
            karixService.sendTextMessage(phone,
                    "We don't have exact matches for this range right now, "
                            + "but our team can find the perfect watch for you! 📞");
            sendCallbackConfirmation(phone, session);
            return;
        }

        List<Map<String, Object>> products = watches.stream().limit(6).map(w -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",    w.getId().toString());
            m.put("name",  w.getName());
            m.put("price", w.getPrice());
            return m;
        }).collect(Collectors.toList());

        String mid = karixService.sendCarouselMessage(
                phone, "Watches in Your Budget ⌚", products);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.CAROUSEL,
                "Price filtered carousel", null, "PRICE_CAROUSEL");

        // Follow-up actions
        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Get a Callback",       "payload", "CALLBACK"),
                Map.of("title", "Download Catalogue",   "payload", "CATALOGUE"),
                Map.of("title", "Explore Other Styles", "payload", "RESTART_STYLE")
        );
        String mid2 = karixService.sendButtonMessage(
                phone, "What would you like to do next?", buttons);
        saveMessage(phone, session.getCustomerId(), mid2,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                "Post price filter actions", null, "PRICE_ACTION");

        session.setCurrentStep("CAROUSEL");
        sessionRepo.save(session);
    }

    // =============================================
    // STEP 5a — CALLBACK CONFIRMATION
    // =============================================
    private void sendCallbackConfirmation(String phone, BotSession session) {
        String text = "Thank you! Our Titan team will be in touch with you shortly. 📞\n\n"
                + "We will help you find the perfect watch for your birthday celebration! 🎁";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Book a Store Visit", "payload", "STORE_VISIT"),
                Map.of("title", "Browse Again",       "payload", "RESTART"),
                Map.of("title", "Explore Website",    "payload", "WEBSITE")
        );

        String mid = karixService.sendButtonMessage(phone, text, buttons);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                text, null, "CALLBACK_CONFIRM");

        saveLead(session, phone, Lead.LeadType.CALLBACK);

        session.setCurrentStep("CALLBACK_CONFIRM");
        sessionRepo.save(session);
    }

    private void handleCallbackConfirm(String phone, String payload, BotSession session) {
        String p = extractPayload(payload);
        switch (p) {
            case "STORE_VISIT":
                sendStoreLocator(phone, session);
                break;
            case "RESTART":
                sendCollectionSelection(phone, session);
                break;
            case "WEBSITE":
                karixService.sendTextMessage(phone,
                        "Explore Titan's full collection here: https://www.titanwatch.in 🌐\n\n"
                                + "Happy Birthday from the Titan family! 🎂");
                session.setCurrentStep("COMPLETED");
                sessionRepo.save(session);
                break;
            default:
                sendCallbackConfirmation(phone, session);
        }
    }

    // =============================================
    // STEP 6 — BIRTHDAY OFFER FLOW
    // =============================================
    private void sendBirthdayOffers(String phone, BotSession session) {
        String text = "A special birthday deserves special rewards! 🎉\n\n"
                + "Your exclusive birthday benefits:\n"
                + "• Birthday discounts on premium watches\n"
                + "• Early access to new collections\n"
                + "• Special in-store birthday experience\n\n"
                + "How would you like to avail these?";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Request a Callback",  "payload", "CALLBACK"),
                Map.of("title", "Book a Store Visit",  "payload", "STORE_VISIT"),
                Map.of("title", "Explore on Website",  "payload", "WEBSITE")
        );

        String mid = karixService.sendButtonMessage(phone, text, buttons);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                text, null, "OFFER");

        session.setCurrentStep("OFFER");
        sessionRepo.save(session);
    }

    private void handleOfferStep(String phone, String payload, BotSession session) {
        String p = extractPayload(payload);
        switch (p) {
            case "CALLBACK":
                sendCallbackConfirmation(phone, session);
                break;
            case "STORE_VISIT":
                sendStoreLocator(phone, session);
                break;
            case "WEBSITE":
                karixService.sendTextMessage(phone,
                        "Browse our full collection: https://www.titanwatch.in 🌐\n\n"
                                + "Enjoy exclusive birthday benefits online! 🎁");
                session.setCurrentStep("COMPLETED");
                sessionRepo.save(session);
                break;
            default:
                sendBirthdayOffers(phone, session);
        }
    }

    // =============================================
    // T-DAY BIRTHDAY WISH (Phase 2 trigger)
    // =============================================
    @Override
    public void sendBirthdayDayMessage(String phone, String customerName, BotSession session) {
        String text = "🎂 Happy Birthday, " + customerName + "!\n\n"
                + "The entire Titan family wishes you a wonderful birthday! 🎉\n\n"
                + "Your special birthday privileges are active today.\n\n"
                + "What would you like to do?";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Visit Store Today",    "payload", "STORE_VISIT"),
                Map.of("title", "Avail Birthday Offer", "payload", "BIRTHDAY_OFFERS"),
                Map.of("title", "Request a Callback",   "payload", "CALLBACK")
        );

        String mid = karixService.sendButtonMessage(phone, text, buttons);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                text, null, "TDAY_WISH");

        session.setCurrentStep("OFFER");
        sessionRepo.save(session);
    }

    // =============================================
    // STORE LOCATOR
    // =============================================
    private void sendStoreLocator(String phone, BotSession session) {
        String storeInfo = storeService.getNearestStoreInfo(session.getCustomerId());
        String text = "We would love to welcome you in person! 🏪\n\n"
                + storeInfo
                + "\n\nYour birthday benefits are ready to be redeemed! 🎁";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Get Directions", "payload", "DIRECTIONS"),
                Map.of("title", "Browse Again",   "payload", "RESTART")
        );

        String mid = karixService.sendButtonMessage(phone, text, buttons);
        saveMessage(phone, session.getCustomerId(), mid,
                Message.Direction.OUTBOUND, Message.MessageType.BUTTON,
                text, null, "STORE_LOCATOR");

        saveLead(session, phone, Lead.LeadType.STORE_VISIT);
        session.setCurrentStep("COMPLETED");
        sessionRepo.save(session);
    }

    // =============================================
    // CATALOGUE LINK
    // =============================================
    private void sendCatalogueLink(String phone, BotSession session) {
        karixService.sendTextMessage(phone,
                "Download our exclusive birthday catalogue here:\n"
                        + "https://www.titanwatch.in/birthday-catalogue\n\n"
                        + "Explore timeless collections curated just for you! ⌚");
        // Keep CAROUSEL step so user can still use other action buttons
        session.setCurrentStep("CAROUSEL");
        sessionRepo.save(session);
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================
    private BotSession createNewSession(String phone, Long customerId) {
        BotSession session = BotSession.builder()
                .phone(phone)
                .customerId(customerId)
                .currentStep("WELCOME")
                .isActive(true)
                .build();
        return sessionRepo.save(session);
    }

    private void saveLead(BotSession session, String phone, Lead.LeadType type) {
        Lead lead = Lead.builder()
                .phone(phone)
                .customerId(session.getCustomerId())
                .leadType(type)
                .status(Lead.LeadStatus.NEW)
                .selectedCollection(session.getSelectedCollection() != null
                        ? session.getSelectedCollection().name() : null)
                .selectedStyle(session.getSelectedStyle() != null
                        ? session.getSelectedStyle().name() : null)
                .priceRange(session.getSelectedPriceRange())
                .build();
        leadRepo.save(lead);
        log.info("Lead saved: {} - {}", phone, type);
    }

    private void saveMessage(String phone, Long customerId, String mid,
                             Message.Direction direction, Message.MessageType type,
                             String content, String buttonPayload, String stepName) {
        Message msg = Message.builder()
                .phone(phone)
                .customerId(customerId)
                .mid(mid)
                .direction(direction)
                .messageType(type)
                .messageContent(content)
                .buttonPayload(buttonPayload)
                .stepName(stepName)
                .status(direction == Message.Direction.OUTBOUND
                        ? Message.Status.SENT : Message.Status.RECEIVED)
                .build();
        messageRepo.save(msg);
    }

    /**
     * Karix quick-reply buttons send ID as "BTN_0_FIND_WATCH".
     * List message rows send the row id directly (e.g. "MINIMAL_CHIC").
     * This method normalises both to the clean payload token.
     */
    private String extractPayload(String payload) {
        if (payload == null) return "";
        if (payload.startsWith("BTN_")) {
            String[] parts = payload.split("_", 3);
            return parts.length >= 3 ? parts[2] : payload;
        }
        return payload.toUpperCase().trim();
    }







}