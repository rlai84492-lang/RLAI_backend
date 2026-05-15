package com.example.titan_watch_learning_project.service;//package com.example.titan.service;

public interface WebhookService {
    void handleWebhookEvent(String rawPayload);
}