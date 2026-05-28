package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/bot-sessions")
    public List<DashboardResponse.SessionDto> getSessions() {
        return dashboardService.getSessions();
    }

    @GetMapping("/leads")
    public List<DashboardResponse.LeadDto> getLeads() {
        return dashboardService.getLeads();
    }
}