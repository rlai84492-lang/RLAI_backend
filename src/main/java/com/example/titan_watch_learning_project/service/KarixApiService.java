package com.example.titan_watch_learning_project.service;//package com.example.titan.service;
//import com.example.titan_watch_learning_project.entity.WatchProduct;

import java.util.List;
import java.util.Map;

public interface KarixApiService {
    String sendTextMessage(String toPhone, String text);
    String sendButtonMessage(String toPhone, String bodyText, List<Map<String, String>> buttons);
    String sendImageButtonMessage(String toPhone, String imageUrl, String bodyText, List<Map<String, String>> buttons);
    String sendCarouselCards(String toPhone, String bodyText, List<Map<String, Object>> cards);
    String sendListMessage(String toPhone, String bodyText, List<Map<String, Object>> options);

//    boolean sendCarouselMessage(String phone, List<WatchProduct> products);


    boolean sendBrandCarouselMessage(
            String phone,
            String firstName,
            String gender,
            List<Map<String, String>> brands
    );

    boolean sendDocumentMessage(
            String toPhone,
            String documentUrl,
            String caption,
            String fileName
    );

    String getCatalogueUrl(String gender, String brandKey);
}