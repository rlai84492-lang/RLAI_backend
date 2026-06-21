////
////package com.example.titan_watch_learning_project.service;
////
////import com.example.titan_watch_learning_project.dto.DashboardResponse;
////import com.example.titan_watch_learning_project.entity.Lead;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.Pageable;
////
////import java.util.List;
////
/////**
//// * Dashboard business operations contract.
//// * Implementation: DashboardServiceImpl.java
//// */
////public interface DashboardService {
////
////    /** Legacy — all flows, no date filter. */
////    DashboardResponse getDashboardData();
////
////    /** All sessions (legacy). */
////    List<DashboardResponse.SessionDto> getSessions();
////
////    /** All leads (legacy). */
////    List<DashboardResponse.LeadDto> getLeads();
////
////
////    DashboardResponse getDashboardDataByFlow(
////            String flow, String range, String startDate, String endDate);
////
////    /** Backward-compat overload — defaults to range="today". */
////    DashboardResponse getDashboardDataByFlow(String flow);
////
////    /** Paginated leads — flow + date range. */
////    Page<Lead> getLeadsPage(
////            String flow, String range, String startDate, String endDate, Pageable pageable);
////
////    /** Convert Lead entity to LeadDto for API response. */
////    DashboardResponse.LeadDto toLeadDto(Lead lead);
////}
//package com.example.titan_watch_learning_project.service;
//
//import com.example.titan_watch_learning_project.dto.DashboardResponse;
//import com.example.titan_watch_learning_project.entity.Lead;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Dashboard business operations contract.
// * Implementation: {@link com.example.titan_watch_learning_project.serviceImpl.DashboardServiceImpl}
// *
// * Controller talks ONLY to this interface — never to the repository directly.
// */
//public interface DashboardService {
//
//    // ── Main dashboard ──────────────────────────────────────────────
//
//    /**
//     * Legacy — all flows, "today" date range.
//     * Prefer getDashboardDataByFlow(flow, range, start, end).
//     */
//    DashboardResponse getDashboardData();
//
//    /**
//     * Main dashboard API.
//     * range: "today" | "7days" | "30days" | "custom"
//     * startDate / endDate: yyyy-MM-dd (required only when range="custom")
//     */
//    DashboardResponse getDashboardDataByFlow(
//            String flow, String range, String startDate, String endDate);
//
//    /** Backward-compat overload — defaults to range="today". */
//    DashboardResponse getDashboardDataByFlow(String flow);
//
//    // ── Sessions ────────────────────────────────────────────────────
//
//    /** Legacy — all flows, no date filter, capped list. */
//    List<DashboardResponse.SessionDto> getSessions();
//
//    /**
//     * Paginated sessions with server-side filters.
//     * All business logic (date resolution, flow→pattern, enrichment) is in ServiceImpl.
//     *
//     * @param flow       bday_t10 | bday_t0 | anniv_t10 | anniv_t0
//     * @param range      today | 7days | 30days | custom
//     * @param collection MENS | WOMENS | COUPLES (nullable)
//     * @param brand      exact brand value (nullable)
//     * @param step       exact current_step value (nullable)
//     * @param search     name or phone substring (nullable)
//     * @param sortField  customerName | phone | currentStep | selectedCollection |
//     *                   selectedBrand | lastActivity
//     * @param sortDir    asc | desc
//     * @param page       0-based page index
//     * @param size       rows per page (capped at 500 in ServiceImpl)
//     * @return Map: { sessions, totalSessions, totalPages, currentPage, pageSize }
//     */
//    Map<String, Object> getSessionsPaginated(
//            String flow, String range, String startDate, String endDate,
//            String collection, String brand, String step, String search,
//            String sortField, String sortDir,
//            int page, int size);
//
//    // ── Counts (left sidebar) ───────────────────────────────────────
//
//    /**
//     * Active session counts per flow + total leads count.
//     * Lightweight — called every 30s by the sidebar.
//     *
//     * @return Map keys: bday_t10, bday_t0, anniv_t10, anniv_t0, total, conversations, leads
//     */
//    Map<String, Long> getFlowCounts();
//
//    // ── Leads ───────────────────────────────────────────────────────
//
//    /** Legacy — all flows, no date filter, capped list. */
//    List<DashboardResponse.LeadDto> getLeads();
//
//    /**
//     * Paginated leads for GET /api/leads.
//     * Page size is enforced to max 200 in the controller.
//     */
//    Page<Lead> getLeadsPage(
//            String flow, String range, String startDate, String endDate, Pageable pageable);
//
//    /** Convert Lead JPA entity to LeadDto for API response. */
//    DashboardResponse.LeadDto toLeadDto(Lead lead);
//}


package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Dashboard business operations contract.
 * Implementation: {@link com.example.titan_watch_learning_project.serviceImpl.DashboardServiceImpl}
 *
 * Controller talks ONLY to this interface — never to the repository directly.
 */
public interface DashboardService {

    // ── Main dashboard ──────────────────────────────────────────────

    /**
     * Legacy — all flows, "today" date range.
     * Prefer getDashboardDataByFlow(flow, range, start, end).
     */
    DashboardResponse getDashboardData();

    /**
     * Main dashboard API.
     * range: "today" | "7days" | "30days" | "custom"
     * startDate / endDate: yyyy-MM-dd (required only when range="custom")
     */
    DashboardResponse getDashboardDataByFlow(
            String flow, String range, String startDate, String endDate);

    /** Backward-compat overload — defaults to range="today". */
    DashboardResponse getDashboardDataByFlow(String flow);

    // ── Sessions ────────────────────────────────────────────────────

    /** Legacy — all flows, no date filter, capped list. */
    List<DashboardResponse.SessionDto> getSessions();

    /**
     * Paginated sessions with server-side filters.
     * All business logic (date resolution, flow→pattern, enrichment) is in ServiceImpl.
     *
     * @param flow       bday_t10 | bday_t0 | anniv_t10 | anniv_t0
     * @param range      today | 7days | 30days | custom
     * @param collection MENS | WOMENS | COUPLES (nullable)
     * @param brand      exact brand value (nullable)
     * @param step       exact current_step value (nullable)
     * @param search     name or phone substring (nullable)
     * @param sortField  customerName | phone | currentStep | selectedCollection |
     *                   selectedBrand | lastActivity
     * @param sortDir    asc | desc
     * @param page       0-based page index
     * @param size       rows per page (capped at 500 in ServiceImpl)
     * @return Map: { sessions, totalSessions, totalPages, currentPage, pageSize }
     */
    Map<String, Object> getSessionsPaginated(
            String flow, String range, String startDate, String endDate,
            String collection, String brand, String step, String search,
            String sortField, String sortDir,
            int page, int size);

    // ── Counts (left sidebar) ───────────────────────────────────────

    /**
     * Active session counts per flow + total leads count.
     * Lightweight — called every 30s by the sidebar.
     *
     * @return Map keys: bday_t10, bday_t0, anniv_t10, anniv_t0, total, conversations, leads
     */
    Map<String, Long> getFlowCounts();

    // ── Leads ───────────────────────────────────────────────────────

    /** Legacy — all flows, no date filter, capped list. */
    List<DashboardResponse.LeadDto> getLeads();

    /**
     * Paginated leads for GET /api/leads.
     * Page size is enforced to max 200 in the controller.
     */
    Page<Lead> getLeadsPage(
            String flow, String range, String startDate, String endDate, Pageable pageable);

    /**
     * Aggregate lead metrics for the given flow + date range.
     * Uses the same WHERE clause as getLeadsPage so the counts always match.
     *
     * @return Map keys: total, callback, store_visit, new_count, converted
     */
    Map<String, Long> getLeadMetrics(
            String flow, String range, String startDate, String endDate);

    /** Convert Lead JPA entity to LeadDto for API response. */
    DashboardResponse.LeadDto toLeadDto(Lead lead);
}