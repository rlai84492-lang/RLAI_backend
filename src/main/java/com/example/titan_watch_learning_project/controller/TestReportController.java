package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.report.DailyLeadReportScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestReportController {
    private final DailyLeadReportScheduler scheduler;

    @GetMapping("/trigger-daily-report")
    public String trigger() {
        scheduler.sendDailyLeadReport();
        return "Report triggered — check email and logs";
    }
}

