package com.example.titan_watch_learning_project.service;//package com.example.titan.service;

import java.util.List;
import java.util.Map;

public interface KarixApiService {
    String sendTextMessage(String toPhone, String text);
    String sendButtonMessage(String toPhone, String bodyText, List<Map<String, String>> buttons);
    String sendCarouselMessage(String toPhone, String headerText, List<Map<String, Object>> products);
}