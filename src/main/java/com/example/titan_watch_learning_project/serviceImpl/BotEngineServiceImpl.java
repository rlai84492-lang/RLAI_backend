package com.example.titan_watch_learning_project.serviceImpl;//package com.example.titan.serviceImpl;
//
//import com.example.titan.entity.*;
//import com.example.titan.repository.*;
//import com.example.titan.service.BotEngineService;
//import com.example.titan.service.KarixApiService;
//import com.example.titan.service.StoreService;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final KarixApiService karix;
    private final StoreService storeService;

    @Value("${titan.banner.image:https://picsum.photos/800/400}")
    private String bannerImage;

    @Value("${titan.catalogue.url:https://www.titanwatch.in}")
    private String catalogueUrl;

    @Value("${titan.website.url:https://www.titanwatch.in}")
    private String websiteUrl;

    // ══════════════════════════════════════════════════════════════
    // MAIN ENTRY
    // ══════════════════════════════════════════════════════════════
    @Override
    public void processIncomingMessage(String phone, String messageText,
                                       String buttonPayload, Long customerId, String customerName) {
        log.info("processMessage phone={} payload={} text={}", phone, buttonPayload, messageText);

        // Save inbound message
        saveMsg(phone, customerId, null, Message.Direction.INBOUND,
                Message.MessageType.TEXT, messageText, buttonPayload, "INBOUND");

        sessionRepo.findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(phone)
                .ifPresent(oldSession -> {
                    oldSession.setIsActive(false);
                    sessionRepo.save(oldSession);
                });

        BotSession session = sessionRepo.findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(phone)
                .orElseGet(() -> {
                    BotSession newSession = BotSession.builder()
                            .phone(phone)
                            .customerId(customerId)
                            .currentStep("WELCOME")
                            .campaignType(BotSession.CampaignType.T10)
                            .isActive(true)
                            .build();
                    return sessionRepo.save(newSession);
                });

        String step = session.getCurrentStep();
        log.info("Step for {}: {}", phone, step);

        String p = extractPayload(buttonPayload, messageText);

        switch (step) {
            case "WELCOME"          -> handleWelcome(phone, p, session, customerName);
            case "COLLECTION"       -> handleCollection(phone, p, session, customerName);
            case "STYLE"            -> handleStyle(phone, p, session, customerName);
            case "CAROUSEL"         -> handleCarousel(phone, p, session, customerName);
            case "PRICE_FILTER"     -> handlePriceFilter(phone, p, session, customerName);
            case "CALLBACK_CONFIRM" -> handleCallbackConfirm(phone, p, session, customerName);
            case "OFFER"            -> handleOffer(phone, p, session, customerName);
            case "COMPLETED"        -> restartJourney(phone, customerName, session);
            default                 -> sendWelcomeMessage(phone, customerName, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 1 — WELCOME (T-10 outbound trigger)
    // ══════════════════════════════════════════════════════════════
    @Override
    public void sendWelcomeMessage(String phone, String customerName, BotSession session) {
        String text = "Dear " + customerName + ", your birthday is just 10 days away! 🎂\n\n"
                + "At Titan, we would love to celebrate your special occasion "
                + "with timeless watches crafted for every moment.\n\n"
                + "Choose what you would like to explore today:";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Find my Birthday Watch",   "payload", "FIND_WATCH"),
                Map.of("title", "Exclusive Birthday Offers","payload", "BIRTHDAY_OFFERS")
        );

        String mid = karix.sendImageButtonMessage(phone, bannerImage, text, buttons);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "WELCOME");

        session.setCurrentStep("WELCOME");
        sessionRepo.save(session);
    }

    private void handleWelcome(String phone, String p, BotSession session, String name) {
        switch (p) {
            case "FIND_WATCH"        -> sendCollectionSelection(phone, session);
            case "BIRTHDAY_OFFERS"   -> sendBirthdayOffers(phone, session);
            default                  -> sendWelcomeMessage(phone, name, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 2 — COLLECTION SELECTION
    // ══════════════════════════════════════════════════════════════
    private void sendCollectionSelection(String phone, BotSession session) {
        String text = "Celebrate your special day with a watch that matches your personality! 🎉\n\n"
                + "Choose a collection:";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Men's Collection",   "payload", "MENS"),
                Map.of("title", "Women's Collection", "payload", "WOMENS")
        );

        String mid = karix.sendImageButtonMessage(phone, bannerImage, text, buttons);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "COLLECTION");

        session.setCurrentStep("COLLECTION");
        sessionRepo.save(session);
    }

    private void handleCollection(String phone, String p, BotSession session, String name) {
        if ("MENS".equals(p) || "WOMENS".equals(p)) {
            session.setSelectedCollection(BotSession.Collection.valueOf(p));
            sessionRepo.save(session);
            sendStyleDiscovery(phone, session);
        } else {
            sendCollectionSelection(phone, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 3 — STYLE DISCOVERY (4 options → list message)
    // ══════════════════════════════════════════════════════════════
    private void sendStyleDiscovery(String phone, BotSession session) {
        String text = "Tell us your preferred style ✨\n\nChoose the one that feels most like you:";

        List<Map<String, Object>> options = List.of(
                Map.of("id", "MINIMAL_CHIC",       "name", "✨ Minimal & Chic",        "price", ""),
                Map.of("id", "BOLD_EDGY",           "name", "🖤 Bold & Edgy",            "price", ""),
                Map.of("id", "LUXE_CLASSY",         "name", "👑 Luxe & Classy",          "price", ""),
                Map.of("id", "SPORTY_ADVENTUROUS",  "name", "⚡ Sporty & Adventurous",   "price", "")
        );

        String mid = karix.sendCarouselCards(phone, text, options);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "STYLE");

        session.setCurrentStep("STYLE");
        sessionRepo.save(session);
    }

    private void handleStyle(String phone, String p, BotSession session, String name) {
        try {
            session.setSelectedStyle(BotSession.Style.valueOf(p));
            sessionRepo.save(session);
            sendProductCarousel(phone, session);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown style: {}", p);
            sendStyleDiscovery(phone, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 4 — PRODUCT CAROUSEL (6 bestsellers by collection+style)
    // ══════════════════════════════════════════════════════════════
    private void sendProductCarousel(String phone, BotSession session) {
        List<Watch> watches = watchRepo.findByCollectionAndStyleAndIsActiveTrue(
                Watch.Collection.valueOf(session.getSelectedCollection().name()),
                Watch.Style.valueOf(session.getSelectedStyle().name())
        );

        List<Watch> top6 = watches.stream().limit(6).collect(Collectors.toList());

        if (top6.isEmpty()) {
            karix.sendTextMessage(phone,
                    "We are curating the perfect watches for you. "
                            + "Our team will reach out shortly! ⌚");
            session.setCurrentStep("COMPLETED");
            sessionRepo.save(session);
            return;
        }

        // Build product list
        List<Map<String, Object>> products = top6.stream().map(w -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",    w.getId().toString());
            m.put("name",  w.getName());
            m.put("price", w.getPrice());
            return m;
        }).collect(Collectors.toList());

        String header = "Perfect! 👏 Here are your "
                + session.getSelectedCollection().name().toLowerCase()
                + " watches — " + session.getSelectedStyle().name().replace("_", " ").toLowerCase();

        String mid = karix.sendCarouselCards(phone, header, products);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.CAROUSEL, header, null, "CAROUSEL");

        // Action buttons after carousel
        List<Map<String, String>> actions = List.of(
                Map.of("title", "Get a Callback",    "payload", "CALLBACK"),
                Map.of("title", "See by Price",      "payload", "PRICE_FILTER"),
                Map.of("title", "Download Catalogue","payload", "CATALOGUE")
        );
        String mid2 = karix.sendButtonMessage(phone, "What would you like to do?", actions);
        saveMsg(phone, session.getCustomerId(), mid2, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, "Carousel actions", null, "CAROUSEL_ACTION");

        session.setCurrentStep("CAROUSEL");
        sessionRepo.save(session);
    }

    private void handleCarousel(String phone, String p, BotSession session, String name) {
        switch (p) {
            case "CALLBACK"      -> sendCallbackConfirmation(phone, session);
            case "PRICE_FILTER"  -> sendPriceFilter(phone, session);
            case "SEE_PRICE"     -> sendPriceFilter(phone, session);   // user typed "price"
            case "CATALOGUE"     -> sendCatalogue(phone, session);
            case "RESTART_STYLE" -> sendStyleDiscovery(phone, session);
            default -> {
                // User may have selected a watch from the list — check if it's a watch ID
                tryHandleWatchSelection(phone, p, session);
            }
        }
    }

    // Handle when user selects a watch from the carousel list
    private void tryHandleWatchSelection(String phone, String p, BotSession session) {
        try {
            long watchId = Long.parseLong(p);
            Watch watch = watchRepo.findById(watchId).orElse(null);
            if (watch != null) {
                String text = "Great choice! 🎉\n\n"
                        + "⌚ *" + watch.getName() + "*\n"
                        + "💰 Price: ₹" + watch.getPrice() + "\n\n"
                        + "Would you like our team to assist you?";
                List<Map<String, String>> buttons = List.of(
                        Map.of("title", "Get a Callback",  "payload", "CALLBACK"),
                        Map.of("title", "Visit Store",     "payload", "STORE_VISIT"),
                        Map.of("title", "Browse More",     "payload", "RESTART_STYLE")
                );
                karix.sendButtonMessage(phone, text, buttons);
                session.setCurrentStep("CALLBACK_CONFIRM");
                sessionRepo.save(session);
            } else {
                sendProductCarousel(phone, session);
            }
        } catch (NumberFormatException e) {
            sendProductCarousel(phone, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 5 — PRICE FILTER
    // ══════════════════════════════════════════════════════════════
    private void sendPriceFilter(String phone, BotSession session) {
        List<Map<String, Object>> options = List.of(
                Map.of("id", "2000-5000",     "name", "₹2,000 – ₹5,000",   "price", ""),
                Map.of("id", "5000-10000",    "name", "₹5,000 – ₹10,000",  "price", ""),
                Map.of("id", "10000-25000",   "name", "₹10,000 – ₹25,000", "price", ""),
                Map.of("id", "25000-999999",  "name", "Above ₹25,000",      "price", "")
        );

        String mid = karix.sendCarouselCards(phone, "Choose your preferred budget range: 💰", options);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, "Price filter", null, "PRICE_FILTER");

        session.setCurrentStep("PRICE_FILTER");
        sessionRepo.save(session);
    }

    private void handlePriceFilter(String phone, String p, BotSession session, String name) {
        if (!p.matches("\\d+-\\d+")) {
            sendPriceFilter(phone, session);
            return;
        }

        session.setSelectedPriceRange(p);
        sessionRepo.save(session);

        String[] parts = p.split("-");
        BigDecimal min = new BigDecimal(parts[0]);
        BigDecimal max = new BigDecimal(parts[1]);

        List<Watch> watches = watchRepo.findByCollectionAndStyleAndPriceBetweenAndIsActiveTrue(
                Watch.Collection.valueOf(session.getSelectedCollection().name()),
                Watch.Style.valueOf(session.getSelectedStyle().name()),
                min, max
        );

        if (watches.isEmpty()) {
            karix.sendTextMessage(phone,
                    "We don't have exact matches right now, "
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

        String mid = karix.sendCarouselCards(phone, "Watches in your budget ⌚", products);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.CAROUSEL, "Price filtered", null, "PRICE_CAROUSEL");

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Get a Callback",     "payload", "CALLBACK"),
                Map.of("title", "Download Catalogue", "payload", "CATALOGUE"),
                Map.of("title", "Explore Other Styles","payload", "RESTART_STYLE")
        );
        karix.sendButtonMessage(phone, "What would you like to do next?", buttons);

        session.setCurrentStep("CAROUSEL");
        sessionRepo.save(session);
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 5a — CALLBACK CONFIRMATION
    // ══════════════════════════════════════════════════════════════
    private void sendCallbackConfirmation(String phone, BotSession session) {
        String text = "Thank you! 📞 Our Titan team will be in touch with you shortly.\n\n"
                + "We will help you find the perfect watch for your birthday celebration! 🎁";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Book a Store Visit", "payload", "STORE_VISIT"),
                Map.of("title", "Browse Again",       "payload", "RESTART_STYLE"),
                Map.of("title", "Explore Website",    "payload", "WEBSITE")
        );

        String mid = karix.sendButtonMessage(phone, text, buttons);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "CALLBACK_CONFIRM");

        saveLead(session, phone, Lead.LeadType.CALLBACK);

        session.setCurrentStep("CALLBACK_CONFIRM");
        sessionRepo.save(session);
    }

    private void handleCallbackConfirm(String phone, String p, BotSession session, String name) {
        switch (p) {
            case "STORE_VISIT"   -> sendStoreLocator(phone, session);
            case "RESTART_STYLE" -> sendStyleDiscovery(phone, session);
            case "WEBSITE"       -> sendWebsite(phone, session);
            default              -> sendCallbackConfirmation(phone, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // STEP 6 — BIRTHDAY OFFER FLOW
    // ══════════════════════════════════════════════════════════════
    private void sendBirthdayOffers(String phone, BotSession session) {
        String text = "A special birthday deserves special rewards! 🎉\n\n"
                + "Your exclusive birthday benefits:\n"
                + "🎁 Birthday discounts on premium watches\n"
                + "⭐ Early access to new collections\n"
                + "🏪 Special in-store birthday experience\n\n"
                + "How would you like to avail these?";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Request a Callback",  "payload", "CALLBACK"),
                Map.of("title", "Book a Store Visit",  "payload", "STORE_VISIT"),
                Map.of("title", "Explore on Website",  "payload", "WEBSITE")
        );

        String mid = karix.sendButtonMessage(phone, text, buttons);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "OFFER");

        session.setCurrentStep("OFFER");
        sessionRepo.save(session);
    }

    private void handleOffer(String phone, String p, BotSession session, String name) {
        switch (p) {
            case "CALLBACK"    -> sendCallbackConfirmation(phone, session);
            case "STORE_VISIT" -> sendStoreLocator(phone, session);
            case "WEBSITE"     -> sendWebsite(phone, session);
            default            -> sendBirthdayOffers(phone, session);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // T-DAY — Birthday day wish (Phase 2)
    // ══════════════════════════════════════════════════════════════
    @Override
    public void sendBirthdayDayMessage(String phone, String customerName, BotSession session) {
        String text = "🎂 Happy Birthday, " + customerName + "!\n\n"
                + "The entire Titan family wishes you a wonderful day! 🎉\n\n"
                + "Your exclusive birthday privileges are active today.\n"
                + "What would you like to do?";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Visit Store Today",    "payload", "STORE_VISIT"),
                Map.of("title", "Avail Birthday Offer", "payload", "BIRTHDAY_OFFERS"),
                Map.of("title", "Request a Callback",   "payload", "CALLBACK")
        );

        String mid = karix.sendImageButtonMessage(phone, bannerImage, text, buttons);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "TDAY_WISH");

        session.setCurrentStep("OFFER");
        sessionRepo.save(session);
    }

    // ══════════════════════════════════════════════════════════════
    // STORE LOCATOR
    // ══════════════════════════════════════════════════════════════
    private void sendStoreLocator(String phone, BotSession session) {
        String storeInfo = storeService.getNearestStoreInfo(session.getCustomerId());
        String text = "We would love to welcome you! 🏪\n\n" + storeInfo
                + "\n\nYour birthday benefits are ready to be redeemed! 🎁";

        List<Map<String, String>> buttons = List.of(
                Map.of("title", "Get Directions", "payload", "DIRECTIONS"),
                Map.of("title", "Browse Again",   "payload", "RESTART_STYLE")
        );

        String mid = karix.sendButtonMessage(phone, text, buttons);
        saveMsg(phone, session.getCustomerId(), mid, Message.Direction.OUTBOUND,
                Message.MessageType.BUTTON, text, null, "STORE_LOCATOR");

        saveLead(session, phone, Lead.LeadType.STORE_VISIT);
        session.setCurrentStep("COMPLETED");
        sessionRepo.save(session);
    }

    // ══════════════════════════════════════════════════════════════
    // CATALOGUE
    // ══════════════════════════════════════════════════════════════
    private void sendCatalogue(String phone, BotSession session) {
        karix.sendTextMessage(phone,
                "📖 Download our exclusive birthday catalogue:\n" + catalogueUrl
                        + "\n\nExplore timeless collections curated just for you! ⌚");
        session.setCurrentStep("CAROUSEL");
        sessionRepo.save(session);
    }

    // ══════════════════════════════════════════════════════════════
    // WEBSITE
    // ══════════════════════════════════════════════════════════════
    private void sendWebsite(String phone, BotSession session) {
        karix.sendTextMessage(phone,
                "🌐 Explore Titan's full collection:\n" + websiteUrl
                        + "\n\nHappy Birthday from the Titan family! 🎂");
        session.setCurrentStep("COMPLETED");
        sessionRepo.save(session);
    }

    // ══════════════════════════════════════════════════════════════
    // Restart journey (after COMPLETED)
    // ══════════════════════════════════════════════════════════════
    private void restartJourney(String phone, String customerName, BotSession session) {
        session.setCurrentStep("WELCOME");
        session.setSelectedCollection(null);
        session.setSelectedStyle(null);
        session.setSelectedPriceRange(null);
        sessionRepo.save(session);
        sendWelcomeMessage(phone, customerName, session);
    }

    // ══════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════
    private BotSession createSession(String phone, Long customerId) {
        BotSession s = BotSession.builder()
                .phone(phone).customerId(customerId)
                .currentStep("WELCOME").isActive(true).build();
        return sessionRepo.save(s);
    }

    private void saveLead(BotSession session, String phone, Lead.LeadType type) {
        leadRepo.save(Lead.builder()
                .phone(phone).customerId(session.getCustomerId())
                .leadType(type).status(Lead.LeadStatus.NEW)
                .selectedCollection(session.getSelectedCollection() != null
                        ? session.getSelectedCollection().name() : null)
                .selectedStyle(session.getSelectedStyle() != null
                        ? session.getSelectedStyle().name() : null)
                .priceRange(session.getSelectedPriceRange())
                .build());
        log.info("Lead saved: {} {}", phone, type);
    }

    private void saveMsg(String phone, Long customerId, String mid,
                         Message.Direction dir, Message.MessageType type,
                         String content, String payload, String step) {
        messageRepo.save(Message.builder()
                .phone(phone).customerId(customerId).mid(mid)
                .direction(dir).messageType(type)
                .messageContent(content).buttonPayload(payload).stepName(step)
                .status(dir == Message.Direction.OUTBOUND
                        ? Message.Status.SENT : Message.Status.RECEIVED)
                .build());
    }

    /**
     * Normalize payload — handles:
     * - Simple payload "FIND_WATCH"
     * - List reply IDs "MINIMAL_CHIC"
     * - Price ranges "2000-5000"
     * - Watch IDs (numbers)
     * Falls back to normalizing message text
     */
    private String extractPayload(String buttonPayload, String messageText) {
        if (buttonPayload != null && !buttonPayload.isBlank()) {
            return buttonPayload.trim().toUpperCase();
        }
        return messageText != null ? messageText.trim().toUpperCase() : "";
    }
}