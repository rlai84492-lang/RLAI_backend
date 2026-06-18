package com.example.titan_watch_learning_project.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

//import java.net.http.HttpHeaders;

// TestForwardController.java — VM pe add karo
@RestController
@RequestMapping("/dev")
public class TestForwardController {

    @Value("${dev.forward.url:}")
    private String devForwardUrl;

    @Value("${dev.forward.enabled:false}")
    private boolean devForwardEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/forward")
    public ResponseEntity<String> forwardToLocal(@RequestBody String payload) {
        if (!devForwardEnabled || devForwardUrl.isBlank()) {
            return ResponseEntity.ok("Dev forward disabled");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(devForwardUrl, request, String.class);
            return ResponseEntity.ok("Forwarded to dev");
        } catch (Exception e) {
            return ResponseEntity.ok("Forward failed: " + e.getMessage());
        }
    }
}