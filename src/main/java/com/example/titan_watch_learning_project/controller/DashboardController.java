package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/bot-sessions")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public List<DashboardResponse.SessionDto> getSessions() {
        return dashboardService.getSessions();
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public List<DashboardResponse.LeadDto> getLeads() {
        return dashboardService.getLeads();
    }
}