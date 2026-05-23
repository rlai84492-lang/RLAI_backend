package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.BotSession;
import com.example.titan_watch_learning_project.entity.Message;
import com.example.titan_watch_learning_project.entity.WatchProduct;
import com.example.titan_watch_learning_project.repository.BotSessionRepository;
import com.example.titan_watch_learning_project.repository.MessageRepository;
import com.example.titan_watch_learning_project.service.BotEngineService;
import com.example.titan_watch_learning_project.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotEngineServiceImpl implements BotEngineService {

    private final MessageRepository messageRepository;
    private final BotSessionRepository botSessionRepository;
    private final KarixApiServiceImpl karixApiService;
private  final ProductCatalogService productCatalogService;



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

        String cleanPayload = payload == null ? "" : payload.trim();

        if (cleanPayload.isBlank()) {
            cleanPayload = text == null ? "" : text.trim();
        }

        cleanPayload = normalizePayload(cleanPayload);

        log.info("Current session step for {}: {}", phone, session.getCurrentStep());

        // Welcome / unknown message
        if (cleanPayload.isBlank()
                || "HI".equalsIgnoreCase(cleanPayload)
                || "HELLO".equalsIgnoreCase(cleanPayload)
                || "START".equalsIgnoreCase(cleanPayload)) {

            handleWelcome(phone, customerName, session);
            return;
        }

        // STEP 1: Find Birthday Watch
        if ("FIND_BIRTHDAY_WATCH".equalsIgnoreCase(cleanPayload)
                || "FIND_WATCH".equalsIgnoreCase(cleanPayload)) {

            sendCollectionSelection(phone);

            session.setCurrentStep("COLLECTION_SELECTION");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 1: Birthday Offers
        if ("BIRTHDAY_OFFERS".equalsIgnoreCase(cleanPayload)
                || "EXCLUSIVE_BIRTHDAY_OFFERS".equalsIgnoreCase(cleanPayload)) {

            sendBirthdayOffers(phone);

            session.setCurrentStep("BIRTHDAY_OFFERS");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 2: Men's Collection
        if ("MENS_COLLECTION".equalsIgnoreCase(cleanPayload)
                || "MEN_COLLECTION".equalsIgnoreCase(cleanPayload)
                || "MEN’S_COLLECTION".equalsIgnoreCase(cleanPayload)) {

            karixApiService.sendStyleSelectionMessage(phone, "Men’s Collection");

            session.setCurrentStep("STYLE_SELECTION_MALE");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 2: Women's Collection
        if ("WOMENS_COLLECTION".equalsIgnoreCase(cleanPayload)
                || "WOMEN_COLLECTION".equalsIgnoreCase(cleanPayload)
                || "WOMEN’S_COLLECTION".equalsIgnoreCase(cleanPayload)) {

            karixApiService.sendStyleSelectionMessage(phone, "Women’s Collection");

            session.setCurrentStep("STYLE_SELECTION_FEMALE");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 2: Couples Watches
        if ("COUPLES_WATCHES".equalsIgnoreCase(cleanPayload)
                || "COUPLE_WATCHES".equalsIgnoreCase(cleanPayload)) {

            sendPriceSelection(phone);

            session.setCurrentStep("PRICE_SELECTION_COUPLES");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 3: Style Selection
        if ("STYLE_MINIMAL_CHIC".equalsIgnoreCase(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_MINIMAL_CHIC", "Minimal & Chic");
            return;
        }

        if ("STYLE_BOLD_EDGY".equalsIgnoreCase(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_BOLD_EDGY", "Bold & Edgy");
            return;
        }

        if ("STYLE_LUXE_CLASSY".equalsIgnoreCase(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_LUXE_CLASSY", "Luxe & Classy");
            return;
        }

        if ("STYLE_SPORTY_ADVENTUROUS".equalsIgnoreCase(cleanPayload)) {
            handleStyleSelected(phone, session, "STYLE_SPORTY_ADVENTUROUS", "Sporty & Adventurous");
            return;
        }

        // STEP 4: See by Price
        if ("SEE_BY_PRICE".equalsIgnoreCase(cleanPayload)) {
            sendPriceSelection(phone);

            String collectionType = getCollectionFromSession(session);
            String style = getStyleFromSession(session);

            session.setCurrentStep("PRICE_SELECTION_" + collectionType + "_" + style);
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 5: Price selected
        if ("PRICE_2K_5K".equalsIgnoreCase(cleanPayload)
                || "PRICE_5K_10K".equalsIgnoreCase(cleanPayload)
                || "PRICE_10K_25K".equalsIgnoreCase(cleanPayload)
                || "PRICE_25K_PLUS".equalsIgnoreCase(cleanPayload)) {

            handlePriceSelected(phone, session, cleanPayload);
            return;
        }

        // Callback
        if ("REQUEST_CALLBACK".equalsIgnoreCase(cleanPayload)) {
            karixApiService.sendTextMessage(
                    phone,
                    "Thank you! Our Titan team will call you shortly to help you find your perfect birthday watch. 📞"
            );

            session.setCurrentStep("CALLBACK_REQUESTED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // Book store visit
        if ("BOOK_STORE_VISIT".equalsIgnoreCase(cleanPayload)) {
            karixApiService.sendTextMessage(
                    phone,
                    "We would love to welcome you in person! Please visit your nearest Titan store here: https://www.titan.co.in/store-locator"
            );

            session.setCurrentStep("STORE_VISIT_REQUESTED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // Download catalogue
        if ("DOWNLOAD_CATALOGUE".equalsIgnoreCase(cleanPayload)) {
            karixApiService.sendTextMessage(
                    phone,
                    "Here is your birthday catalogue: https://www.titanwatch.in/birthday-catalogue"
            );

            session.setCurrentStep("CATALOGUE_SENT");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // Website
        if ("EXPLORE_WEBSITE".equalsIgnoreCase(cleanPayload)
                || "BROWSE_COLLECTION".equalsIgnoreCase(cleanPayload)) {

            karixApiService.sendTextMessage(
                    phone,
                    "Explore Titan’s full collection here: https://www.titan.co.in"
            );

            session.setCurrentStep("WEBSITE_REDIRECTED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // Browse again
        if ("BROWSE_AGAIN".equalsIgnoreCase(cleanPayload)
                || "EXPLORE_OTHER_STYLES".equalsIgnoreCase(cleanPayload)) {

            String collectionType = getCollectionFromSession(session);

            if ("FEMALE".equalsIgnoreCase(collectionType)) {
                karixApiService.sendStyleSelectionMessage(phone, "Women’s Collection");
                session.setCurrentStep("STYLE_SELECTION_FEMALE");
            } else {
                karixApiService.sendStyleSelectionMessage(phone, "Men’s Collection");
                session.setCurrentStep("STYLE_SELECTION_MALE");
            }

            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        log.info("No matching payload found. Starting welcome flow for phone={}", phone);
        handleWelcome(phone, customerName, session);
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
                            Map.of("title", "Browse again", "payload", "BROWSE_AGAIN")
                    )
            );
            return;
        }

        int index = 1;

        for (WatchProduct product : products) {
            String brand = (product.getBrand() == null || product.getBrand().isBlank())
                    ? "Titan Watch"
                    : product.getBrand();

            String productUrl = (product.getProductUrl() == null)
                    ? ""
                    : product.getProductUrl();

            String imageUrl = product.getImageUrl();

            System.out.println(imageUrl + "here samarth image");

            // ✅ CLEAN caption - no number prefix, premium feel
            String caption =
                    "✨ *TITAN | " + brand.toUpperCase() + "*\n\n"
                            + "Celebrate your special day with a timepiece crafted for you.\n\n"
                            + "🔗 " + productUrl;

            log.info("Sending product id={} brand={} imageUrl={}",
                    product.getId(), brand, imageUrl);

            // ✅ IMAGE CHECK - log karke dekho kya aa raha hai
            if (imageUrl != null && !imageUrl.isBlank()) {
                log.info("Attempting image card for product id={} url={}",
                        product.getId(), imageUrl);

                boolean sent = karixApiService.sendImageButtonMessage(
                        phone,
                        imageUrl,
                        caption,
                        List.of(
                                Map.of("title", "Callback", "payload", "REQUEST_CALLBACK"),
                                Map.of("title", "Catalogue", "payload", "DOWNLOAD_CATALOGUE")
                        )
                );

                if (sent) {
                    log.info("✅ Image card sent for product id={}", product.getId());
                } else {
                    // ✅ Fallback - text + link
                    log.warn("❌ Image card failed for product id={}. Sending text fallback.",
                            product.getId());
                    karixApiService.sendTextMessage(phone, caption);
                }
            } else {
                // ✅ No image URL - direct text
                log.warn("No imageUrl for product id={}. Sending text.", product.getId());
                karixApiService.sendTextMessage(phone, caption);
            }

            index++;
        }

        karixApiService.sendButtonMessage(
                phone,
                "Liked a watch? What would you like to do next?",
                List.of(
                        Map.of("title", "Callback", "payload", "REQUEST_CALLBACK"),
                        Map.of("title", "Catalogue", "payload", "DOWNLOAD_CATALOGUE"),
                        Map.of("title", "Browse again", "payload", "BROWSE_AGAIN")
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
        if ("PRICE_2K_5K".equalsIgnoreCase(pricePayload)) {
            return "BELOW_10K";
        }

        if ("PRICE_5K_10K".equalsIgnoreCase(pricePayload)) {
            return "BELOW_10K";
        }

        if ("PRICE_10K_25K".equalsIgnoreCase(pricePayload)) {
            return "10K_25K";
        }

        if ("PRICE_25K_PLUS".equalsIgnoreCase(pricePayload)) {
            return "25K_PLUS";
        }

        return "BELOW_10K";
    }
    private void handlePriceSelected(String phone, BotSession session, String pricePayload) {
        String collectionType = getCollectionFromSession(session);
        String priceBucket = mapPricePayloadToBucket(pricePayload);

        if (collectionType == null || collectionType.isBlank()) {
            collectionType = "MALE";
        }
        List<WatchProduct> products =
                productCatalogService.getProductsByCollectionAndPrice(collectionType, priceBucket);

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
                "Choose your preferred budget range:",
                List.of(
                        Map.of("title", "₹2k - ₹5k", "payload", "PRICE_2K_5K"),
                        Map.of("title", "₹5k - ₹10k", "payload", "PRICE_5K_10K"),
                        Map.of("title", "₹10k - ₹25k", "payload", "PRICE_10K_25K"),
                        Map.of("title", "₹25k+", "payload", "PRICE_25K_PLUS")
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

        if (collectionType == null || collectionType.isBlank()) {
            collectionType = "MALE";
        }

        karixApiService.sendButtonMessage(
                phone,
                "Great choice! " + styleName + " watches are perfect for your celebration.\n\n"
                        + "Would you like to see recommendations by price?",
                List.of(
                        Map.of("title", "See by Price", "payload", "SEE_BY_PRICE"),
                        Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK"),
                        Map.of("title", "Catalogue", "payload", "DOWNLOAD_CATALOGUE")
                )
        );

        session.setCurrentStep("STYLE_SELECTED_" + collectionType + "_" + stylePayload);
        session.setLastActivity(LocalDateTime.now());
        botSessionRepository.save(session);
    }

    private void sendCollectionSelection(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "Celebrate your special day with a watch that matches your personality.\n\nChoose a collection:",
                List.of(
                        Map.of("title", "Men’s Collection", "payload", "MENS_COLLECTION"),
                        Map.of("title", "Women’s Collection", "payload", "WOMENS_COLLECTION"),
                        Map.of("title", "Couples Watches", "payload", "COUPLES_WATCHES")
                )
        );
    }


    private void sendBirthdayOffers(String phone) {
        karixApiService.sendButtonMessage(
                phone,
                "A special birthday deserves special rewards! 🎉\n\n"
                        + "Exclusive benefits include:\n"
                        + "• Birthday discounts\n"
                        + "• Early access to collections\n"
                        + "• Special in-store experience\n\n"
                        + "How would you like to continue?",
                List.of(
                        Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK"),
                        Map.of("title", "Book Store Visit", "payload", "BOOK_STORE_VISIT"),
                        Map.of("title", "Explore Website", "payload", "EXPLORE_WEBSITE")
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
                "Hi " + name + " 🎂\n\n"
                        + "This is your birthday month.\n"
                        + "Gift yourself time.\n\n"
                        + "We've put together a Titan edit - chosen for your style, for this occasion.\n"
                        + "Take a look whenever it feels right.",
                List.of(
                        Map.of("title", "Find watch", "payload", "FIND_BIRTHDAY_WATCH"),
                        Map.of("title", "Offers", "payload", "BIRTHDAY_OFFERS")
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
}