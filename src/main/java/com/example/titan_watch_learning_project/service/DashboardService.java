//
//package com.example.titan_watch_learning_project.service;
//
//import com.example.titan_watch_learning_project.dto.DashboardResponse;
//import com.example.titan_watch_learning_project.entity.Lead;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//
//public interface DashboardService {
//
//    DashboardResponse getDashboardData();
//
//    List<DashboardResponse.SessionDto> getSessions();
//
//    List<DashboardResponse.LeadDto> getLeads();
//
//    // ── Flow + Date-range wise dashboard ────────────────────────────
//    DashboardResponse getDashboardDataByFlow(String flow, String range, String startDate, String endDate);
//
//    // Backward-compatible overload
//    DashboardResponse getDashboardDataByFlow(String flow);
//
//    // ── Leads — flow + date range wise paginated ────────────────────
//    Page<Lead> getLeadsPage(String flow, String range, String startDate, String endDate, Pageable pageable);
//
//    DashboardResponse.LeadDto toLeadDto(Lead lead);
//}

package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Dashboard ke saare business operations ka contract.
 * Implementation: DashboardServiceImpl.java
 */
public interface DashboardService {

    DashboardResponse getDashboardData();

    List<DashboardResponse.SessionDto> getSessions();

    List<DashboardResponse.LeadDto> getLeads();

    // ── Flow + Date-range wise dashboard ────────────────────────────
    DashboardResponse getDashboardDataByFlow(String flow, String range, String startDate, String endDate);

    // Backward-compatible overload
    DashboardResponse getDashboardDataByFlow(String flow);

    // ── Leads — flow + date range wise paginated ────────────────────
    Page<Lead> getLeadsPage(String flow, String range, String startDate, String endDate, Pageable pageable);

    DashboardResponse.LeadDto toLeadDto(Lead lead);
}