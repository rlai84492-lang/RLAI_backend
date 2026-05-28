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
        // STEP 1: Find Birthday Watch
        if ("FIND_BIRTHDAY_WATCH".equalsIgnoreCase(cleanPayload)
                || "FIND_WATCH".equalsIgnoreCase(cleanPayload)
                || "FIND_MY_WATCH".equalsIgnoreCase(cleanPayload)
                || "FIND_MY_BIRTHDAY_WATCH".equalsIgnoreCase(cleanPayload)
                || "FIND_BIRTHDAY_WATCH".equalsIgnoreCase(normalizePayload(cleanPayload))) {

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
                || "MENS".equalsIgnoreCase(cleanPayload)
                || "MEN’S_COLLECTION".equalsIgnoreCase(cleanPayload)) {

            sendPriceSelection(phone);

            session.setCurrentStep("PRICE_SELECTION_MALE");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // STEP 2: Women's Collection
        if ("WOMENS_COLLECTION".equalsIgnoreCase(cleanPayload)
                || "WOMEN_COLLECTION".equalsIgnoreCase(cleanPayload)
                || "WOMENS".equalsIgnoreCase(cleanPayload)
                || "WOMEN’S_COLLECTION".equalsIgnoreCase(cleanPayload)) {

            sendPriceSelection(phone);

            session.setCurrentStep("PRICE_SELECTION_FEMALE");
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
        if ("PRICE_BELOW_10K".equalsIgnoreCase(cleanPayload)
                || "PRICE_2K_5K".equalsIgnoreCase(cleanPayload)
                || "PRICE_5K_10K".equalsIgnoreCase(cleanPayload)
                || "PRICE_10K_25K".equalsIgnoreCase(cleanPayload)
                || "PRICE_25K_PLUS".equalsIgnoreCase(cleanPayload)) {

            handlePriceSelected(phone, session, cleanPayload);
            return;
        }

        // Callback
        if ("REQUEST_CALLBACK".equalsIgnoreCase(cleanPayload)
                || "INTERESTED".equalsIgnoreCase(cleanPayload)
                || "SPEAK_WITH_EXPERT".equalsIgnoreCase(cleanPayload)) {

            karixApiService.sendButtonMessage(
                    phone,
                    "✅ *Done. Our team will be in touch.*\n\n"
                            + "One of our Titan experts will call you to help you with the one that caught your eye.\n\n"
                            + "In the meantime, locate the nearest store if you'd like to walk in.",
                    List.of(
                            Map.of("title", "Nearby Store", "payload", "BOOK_STORE_VISIT"),
                            Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
                    )
            );

            session.setCurrentStep("CALLBACK_REQUESTED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }        // Book store visit
        if ("BOOK_STORE_VISIT".equalsIgnoreCase(cleanPayload)
                || "NEARBY_STORE".equalsIgnoreCase(cleanPayload)
                || "PICK_UP_STORE".equalsIgnoreCase(cleanPayload)
                || "STORE_VISIT".equalsIgnoreCase(cleanPayload)
                || "LOCATE_STORE".equalsIgnoreCase(cleanPayload)
                || "FIND_STORE_NEAR_ME".equalsIgnoreCase(cleanPayload)
                || "VISIT_STORE_TODAY".equalsIgnoreCase(cleanPayload)) {

            karixApiService.sendButtonMessage(
                    phone,
                    "🏪 *Find your nearest Titan store here:*\n\n"
                            + "https://www.titan.co.in/store-locator\n\n"
                            + "Hope you found your nearest store. If you need help, our expert can call you and book an appointment.",
                    List.of(
                            Map.of("title", "Speak Expert", "payload", "REQUEST_CALLBACK"),
                            Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
                    )
            );

            session.setCurrentStep("STORE_VISIT_REQUESTED");
            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }

        // Download catalogue
        if ("DOWNLOAD_CATALOGUE".equalsIgnoreCase(cleanPayload)) {
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

            if (collectionType == null || collectionType.isBlank()) {
                sendCollectionSelection(phone);
                session.setCurrentStep("COLLECTION_SELECTION");
            } else {
                sendPriceSelection(phone);
                session.setCurrentStep("PRICE_SELECTION_" + collectionType);
            }

            session.setLastActivity(LocalDateTime.now());
            botSessionRepository.save(session);
            return;
        }
        log.info("No matching payload found. Starting welcome flow for phone={}", phone);
        handleWelcome(phone, customerName, session);
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

        for (WatchProduct product : products) {
            String brand = (product.getBrand() == null || product.getBrand().isBlank())
                    ? "Titan Watch"
                    : product.getBrand();

            String productUrl = (product.getProductUrl() == null)
                    ? ""
                    : product.getProductUrl();

            String imageUrl = product.getImageUrl();

            String priceText = product.getPrice() == null
                    ? ""
                    : "💰 *Price:* ₹" + String.format("%.0f", product.getPrice()) + "\n";

            String styleText = product.getStyle() == null || product.getStyle().isBlank()
                    ? ""
                    : "✨ *Style:* " + product.getStyle() + "\n";

            String caption =
                    "⌚ *" + brand.toUpperCase() + "*\n"
                            + "━━━━━━━━━━━━━━\n"
                            + "🎂 _Birthday Special Pick_\n\n"
                            + priceText
                            + styleText
                            + "\n"
                            + "Crafted for those who wear time with pride.\n\n"
                            + "🔗 " + productUrl;

            log.info("Sending product id={} brand={} imageUrl={}",
                    product.getId(), brand, imageUrl);

            if (imageUrl != null && !imageUrl.isBlank()) {
                boolean sent = karixApiService.sendImageButtonMessage(
                        phone,
                        imageUrl,
                        caption,
                        List.of(
                                Map.of("title", "Interested?", "payload", "REQUEST_CALLBACK"),
                                Map.of("title", "Nearby Store", "payload", "BOOK_STORE_VISIT")
                        )
                );

                if (!sent) {
                    log.warn("Image card failed for product id={}. Sending text fallback.", product.getId());
                    karixApiService.sendTextMessage(phone, caption);
                }
            } else {
                log.warn("No imageUrl for product id={}. Sending text.", product.getId());
                karixApiService.sendTextMessage(phone, caption);
            }
        }

        karixApiService.sendButtonMessage(
                phone,
                "✨ *Here are the best picks in your range.*\n\n"
                        + "Found one? Let's get you in store.",
                List.of(
                        Map.of("title", "Interested? We'll get in touch", "payload", "REQUEST_CALLBACK"),
                        Map.of("title", "Pick Up from a Nearby Store ", "payload", "BOOK_STORE_VISIT")
//                        Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
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


//    private void handlePriceSelected(String phone, BotSession session, String pricePayload) {
//        String collectionType = getCollectionFromSession(session);
//        String priceBucket = mapPricePayloadToBucket(pricePayload);
//        String stylePayload = getStyleFromSession(session);
//        String dbStyle = mapStylePayloadToDbStyle(stylePayload);
//
//        if (collectionType == null || collectionType.isBlank()) {
//            collectionType = "MALE";
//        }
//
//        log.info("Fetching products collectionType={} priceBucket={} stylePayload={} dbStyle={}",
//                collectionType, priceBucket, stylePayload, dbStyle);
//
//        List<WatchProduct> products;
//
//        if ("COUPLES".equalsIgnoreCase(collectionType)) {
//            products = productCatalogService.getProductsByCollectionAndPrice(
//                    collectionType,
//                    priceBucket
//            );
//        } else if (dbStyle != null && !dbStyle.isBlank()) {
//            products = productCatalogService.getProductsByCollectionPriceAndStyle(
//                    collectionType,
//                    priceBucket,
//                    dbStyle
//            );
//        } else {
//            products = productCatalogService.getProductsByCollectionAndPrice(
//                    collectionType,
//                    priceBucket
//            );
//        }
//
//        if (products == null || products.isEmpty()) {
//            karixApiService.sendTextMessage(
//                    phone,
//                    "Sorry, we could not find watches in this style and price range right now. Our team can help you personally."
//            );
//
//            karixApiService.sendButtonMessage(
//                    phone,
//                    "Would you like a callback?",
//                    List.of(
//                            Map.of("title", "Request Callback", "payload", "REQUEST_CALLBACK"),
//                            Map.of("title", "Browse Again", "payload", "BROWSE_AGAIN")
//                    )
//            );
//
//            session.setCurrentStep("NO_PRODUCTS_FOUND");
//            session.setLastActivity(LocalDateTime.now());
//            botSessionRepository.save(session);
//            return;
//        }
//
//        sendProductRecommendations(phone, products, collectionType, priceBucket);
//
//        session.setCurrentStep("PRODUCTS_SENT_" + collectionType + "_" + priceBucket + "_" + stylePayload);
//        session.setLastActivity(LocalDateTime.now());
//        botSessionRepository.save(session);
//    }





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

//    private void sendPriceSelection(String phone) {
//        karixApiService.sendListMessage(
//                phone,
//                "💎 *Every budget deserves a masterpiece.*\n\n"
//                        + "Select your preferred range and we'll curate the finest Titan timepieces for you:",
//                List.of(
//                        Map.of("title", "₹2k - ₹5k", "payload", "PRICE_2K_5K"),
//                        Map.of("title", "₹5k - ₹10k", "payload", "PRICE_5K_10K"),
//                        Map.of("title", "₹10k - ₹25k", "payload", "PRICE_10K_25K"),
//                        Map.of("title", "₹25k+", "payload", "PRICE_25K_PLUS")
//                )
//        );
//    }




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



//    private void handleWelcome(String phone, String customerName, BotSession session) {
//        String name = customerName == null || customerName.isBlank()
//                ? "there"
//                : customerName;
//
//        karixApiService.sendImageButtonMessage(
//                phone,
//                "https://www.titan.co.in/dw/image/v2/BKDD_PRD/on/demandware.static/-/Sites-titan-master-catalog/default/dw6279dccd/images/Titan/Catalog/1688KM06_4.jpg?sw=600&sh=600",
//                "🎂 *Happy Birthday Month, " + name + "!*\n\n"
//                        + "At Titan, we believe every moment deserves to be remembered.\n\n"
//                        + "✨ Your birthday is the perfect occasion to gift yourself time — timeless, elegant, yours.\n\n"
//                        + "_A curated collection awaits you._",
//                List.of(
//                        Map.of("title", "🎁 Find My Watch", "payload", "FIND_BIRTHDAY_WATCH"),
//                        Map.of("title", "🎉 Birthday Offers", "payload", "BIRTHDAY_OFFERS")
//                )
//        );
//
//        session.setCurrentStep("WELCOME_SENT");
//        session.setLastActivity(LocalDateTime.now());
//        botSessionRepository.save(session);
//    }



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
}


//WEBHOOK_FORWARD_URL = https://quack-freestyle-slashed.ngrok-free.dev/webhook