package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.serviceImpl.KarixApiServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
//@RequiredArgsConstructor
public class TestController {

    private final KarixApiServiceImpl karixApiService;

    public TestController(KarixApiServiceImpl karixApiService) {
        this.karixApiService = karixApiService;
    }

    // Simple flat request — String values only
    @PostMapping("/send-document")
    public ResponseEntity<?> testDocument(
            @RequestParam String phone,
            @RequestParam String url,
            @RequestParam(defaultValue = "Test.pdf") String filename,
            @RequestParam(defaultValue = "Here is your catalogue") String caption
    ) {
        boolean result = karixApiService.sendDocumentMessage(phone, url, caption, filename);
        return ResponseEntity.ok(Map.of(
                "success", result,
                "phone", phone
        ));
    }
}