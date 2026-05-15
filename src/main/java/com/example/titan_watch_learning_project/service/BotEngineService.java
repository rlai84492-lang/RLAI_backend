package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.entity.BotSession;

public interface BotEngineService {
    void processIncomingMessage(String phone, String messageText, String buttonPayload,
                                Long customerId, String customerName);
    void sendWelcomeMessage(String phone, String customerName, BotSession session);
    void sendBirthdayDayMessage(String phone, String customerName, BotSession session);

}