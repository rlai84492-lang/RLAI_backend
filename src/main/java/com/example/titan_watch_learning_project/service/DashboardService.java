package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.dto.DashboardResponse;

import java.util.List;

public interface DashboardService {

    DashboardResponse getDashboardData();

    List<DashboardResponse.SessionDto> getSessions();

    List<DashboardResponse.LeadDto> getLeads();
}