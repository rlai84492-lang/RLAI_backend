////package com.example.titan_watch_learning_project.controller;
////
////import com.example.titan_watch_learning_project.dto.DashboardResponse;
////import com.example.titan_watch_learning_project.entity.Lead;
////import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
////import com.example.titan_watch_learning_project.repository.LeadRepository;
////import com.example.titan_watch_learning_project.serviceImpl.DashboardServiceImpl;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.PageRequest;
////import org.springframework.data.domain.Pageable;
////import org.springframework.data.domain.Sort;
////import org.springframework.http.CacheControl;
////import org.springframework.http.ResponseEntity;
////import org.springframework.security.access.prepost.PreAuthorize;
////import org.springframework.web.bind.annotation.*;
////
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////import java.time.ZoneId;
////import java.util.LinkedHashMap;
////import java.util.List;
////import java.util.Map;
////import java.util.concurrent.TimeUnit;
////
////@Slf4j
////@RestController
////@RequestMapping("/api")
////@RequiredArgsConstructor
////public class DashboardController {
////
////    private final DashboardServiceImpl    dashboardService;
////    private final LeadRepository          leadRepository;
////    private final DashboardDataRepository dashboardDataRepository;
////
////    // ── Main Dashboard ─────────────────────────────────────────────────
////    /**
////     * GET /api/dashboard?flow=bday_t10&range=today
////     *
////     * range: today | 7days | 30days | custom
////     * startDate / endDate: yyyy-MM-dd (only when range=custom)
////     *
////     * Response includes:
////     *  - sessions[]       limited list (max 500) for the table
////     *  - leads[]          limited list (max 500) for the table
////     *  - totalSessions    real COUNT from DB (may be > 500)
////     *  - totalLeads       real COUNT from DB
////     *  - metrics          all 8 KPI tiles
////     *  - hourly, collData, timeline
////     */
////    @GetMapping("/dashboard")
////    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
////    public ResponseEntity<DashboardResponse> getDashboard(
////            @RequestParam(defaultValue = "bday_t10") String flow,
////            @RequestParam(defaultValue = "today")    String range,
////            @RequestParam(required = false)          String startDate,
////            @RequestParam(required = false)          String endDate
////    ) {
////
////        System.out.println(flow + " testing" + range + " " + startDate + " " + endDate );
////        DashboardResponse response = dashboardService.getDashboardDataByFlow(
////                flow, range, startDate, endDate);
////
////        // Allow CDN / browser to cache for 30s — reduces repeated API hits
////        return ResponseEntity.ok()
////                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).mustRevalidate())
////                .body(response);
////    }
////
////    // ── Left menu flow counts ─────────────────────────────────────────
////    /**
////     * GET /api/dashboard/counts
////     * Lightweight — only returns sidebar counts, no session/lead data.
////     * Called every 30s by sidebar auto-refresh.
////     */
////    @GetMapping("/dashboard/counts")
////    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
////    public ResponseEntity<Map<String, Long>> getCounts() {
////        Map<String, Long> counts = new LinkedHashMap<>(
////                dashboardDataRepository.findFlowCounts());
////        counts.put("conversations", dashboardDataRepository.countActiveSessions());
////        counts.put("leads",         leadRepository.count());
////
////        return ResponseEntity.ok()
////                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
////                .body(counts);
////    }
////
////    // ── Sessions — Paginated with server-side filters ────────────────
////    /**
////     * GET /api/bot-sessions
////     *
////     * ★ UPDATED — now fully paginated. No 500-row cap.
////     * All filters are applied in SQL (index-friendly), so even 10k+ sessions
////     * are handled efficiently.
////     *
////     * Params:
////     *   flow        bday_t10 | bday_t0 | anniv_t10 | anniv_t0
////     *   range       today | 7days | 30days | custom
////     *   startDate   yyyy-MM-dd (only when range=custom)
////     *   endDate     yyyy-MM-dd (only when range=custom)
////     *   collection  MENS | WOMENS | COUPLES (optional)
////     *   brand       exact brand value (optional)
////     *   step        exact current_step value (optional)
////     *   search      name or phone substring (optional)
////     *   sortField   customerName | phone | currentStep | selectedCollection |
////     *               selectedBrand | lastActivity  (default: lastActivity)
////     *   sortDir     asc | desc  (default: desc)
////     *   page        0-based page index  (default: 0)
////     *   size        rows per page, max 500  (default: 100)
////     *
////     * Response: { sessions[], totalSessions, totalPages, currentPage, pageSize }
////     */
////    @GetMapping("/bot-sessions")
////    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
////    public ResponseEntity<Map<String, Object>> getSessions(
////            @RequestParam(defaultValue = "bday_t10")    String flow,
////            @RequestParam(defaultValue = "today")       String range,
////            @RequestParam(required = false)             String startDate,
////            @RequestParam(required = false)             String endDate,
////            @RequestParam(required = false)             String collection,
////            @RequestParam(required = false)             String brand,
////            @RequestParam(required = false)             String step,
////            @RequestParam(required = false)             String search,
////            @RequestParam(defaultValue = "lastActivity") String sortField,
////            @RequestParam(defaultValue = "desc")        String sortDir,
////            @RequestParam(defaultValue = "0")           int    page,
////            @RequestParam(defaultValue = "100")         int    size
////    ) {
////
////
////        System.out.println(flow + " testing session" + range + " " + startDate + " " + endDate + " " + collection + " " + brand + " " + step + " " + search + " " + sortField + " " + sortDir + " " + page + " " + size);
////        LocalDateTime[] dt = resolveSessionDates(range, startDate, endDate);
////        int safeSize = Math.min(Math.max(size, 1), 500);
////
////        Map<String, Object> result = dashboardDataRepository.findSessionsPaginated(
////                flow, dt[0], dt[1],
////                collection, brand, step, search,
////                sortField, sortDir,
////                Math.max(page, 0), safeSize
////        );
////
////        // Sessions list is live data — do not cache
////        return ResponseEntity.ok()
////                .cacheControl(CacheControl.noStore())
////                .body(result);
////    }
////
////    /**
////     * GET /api/bot-sessions/flow/{flow}
////     * Legacy endpoint — still supported. Returns first-page sessions only.
////     */
////    @GetMapping("/bot-sessions/flow/{flow}")
////    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
////    public List<DashboardResponse.SessionDto> getSessionsByFlow(@PathVariable String flow) {
////        return dashboardService.getSessions()
////                .stream()
////                .filter(s -> flow.equalsIgnoreCase(s.getFlow()))
////                .toList();
////    }
////
////    // ── Private: IST-aware date resolver (mirrors DashboardServiceImpl) ──
////    private static final ZoneId CONTROLLER_IST = ZoneId.of("Asia/Kolkata");
////
////    private LocalDateTime[] resolveSessionDates(String range, String startDate, String endDate) {
////        LocalDate today = LocalDate.now(CONTROLLER_IST);
////        LocalDate fromDate;
////        LocalDate toDate;
////        switch (range == null ? "today" : range) {
////            case "7days"  -> { fromDate = today.minusDays(6);  toDate = today; }
////            case "30days" -> { fromDate = today.minusDays(29); toDate = today; }
////            case "custom" -> {
////                try {
////                    fromDate = (startDate != null && !startDate.isBlank())
////                            ? LocalDate.parse(startDate) : today;
////                    toDate   = (endDate != null && !endDate.isBlank())
////                            ? LocalDate.parse(endDate) : today;
////                } catch (Exception ex) {
////                    log.warn("Invalid custom dates: startDate={} endDate={}", startDate, endDate);
////                    fromDate = today; toDate = today;
////                }
////            }
////            default -> { fromDate = today; toDate = today; }
////        }
////        return new LocalDateTime[]{
////                fromDate.atStartOfDay(),
////                toDate.plusDays(1).atStartOfDay()
////        };
////    }
////
////    // ── Leads — paginated ─────────────────────────────────────────────
////    /**
////     * GET /api/leads?page=0&size=100&flow=bday_t10&range=today
////     *
////     * ★ Paginated. Max page size enforced at 200 to prevent timeout.
////     * Frontend should request size=100 per page and show pagination UI.
////     *
////     * Response:
////     * {
////     *   leads:       [...],   // current page items
////     *   totalLeads:  1234,    // total matching records
////     *   totalPages:  13,
////     *   currentPage: 0,
////     *   pageSize:    100
////     * }
////     */
////    @GetMapping("/leads")
////    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
////    public ResponseEntity<Map<String, Object>> getLeads(
////            @RequestParam(defaultValue = "0")       int    page,
////            @RequestParam(defaultValue = "100")     int    size,
////            @RequestParam(required = false)         String flow,
////            @RequestParam(defaultValue = "today")   String range,
////            @RequestParam(required = false)         String startDate,
////            @RequestParam(required = false)         String endDate
////    ) {
////        // Hard-cap to 200 — prevents someone passing size=999999 and causing OOM
////        int safeSize = Math.min(Math.max(size, 1), 200);
////
////        Pageable pageable = PageRequest.of(page, safeSize,
////                Sort.by("createdAt").descending());
////
////        Page<Lead> leadsPage = dashboardService.getLeadsPage(
////                flow, range, startDate, endDate, pageable);
////
////        Map<String, Object> body = new LinkedHashMap<>();
////        body.put("leads",       leadsPage.getContent().stream()
////                .map(dashboardService::toLeadDto).toList());
////        body.put("totalLeads",  leadsPage.getTotalElements());
////        body.put("totalPages",  leadsPage.getTotalPages());
////        body.put("currentPage", page);
////        body.put("pageSize",    safeSize);
////
////        return ResponseEntity.ok(body);
////    }
////}
//
//package com.example.titan_watch_learning_project.controller;
//
//import com.example.titan_watch_learning_project.dto.DashboardResponse;
//import com.example.titan_watch_learning_project.entity.Lead;
//import com.example.titan_watch_learning_project.service.DashboardService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.CacheControl;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * DashboardController — HTTP layer ONLY.
// *
// * Responsibilities:
// *  ✅  Read @RequestParam / @PathVariable from the HTTP request
// *  ✅  Apply HTTP-level guards (page-size cap, cache headers, security)
// *  ✅  Call dashboardService — the single point of contact for all business operations
// *  ✅  Return ResponseEntity with the service result
// *
// *  ❌  No date resolution     — lives in DashboardServiceImpl
// *  ❌  No flow→pattern logic  — lives in DashboardServiceImpl
// *  ❌  No direct repository calls — always goes through DashboardService
// */
//@Slf4j
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class DashboardController {
//
//    /** Single service dependency — no repository injected here. */
//    private final DashboardService dashboardService;
//
//    // ── Main Dashboard ─────────────────────────────────────────────────
//    /**
//     * GET /api/dashboard?flow=bday_t10&range=today
//     *
//     * range: today | 7days | 30days | custom
//     * startDate / endDate: yyyy-MM-dd (only when range=custom)
//     *
//     * Response:
//     *  sessions[]       capped display list
//     *  leads[]          capped display list
//     *  totalSessions    real COUNT from DB
//     *  totalLeads       real COUNT from DB
//     *  metrics          all 8 KPI tiles
//     *  hourly, collData, timeline
//     */
//    @GetMapping("/dashboard")
//    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
//    public ResponseEntity<DashboardResponse> getDashboard(
//            @RequestParam(defaultValue = "bday_t10") String flow,
//            @RequestParam(defaultValue = "today")    String range,
//            @RequestParam(required = false)          String startDate,
//            @RequestParam(required = false)          String endDate
//    ) {
//        DashboardResponse response = dashboardService.getDashboardDataByFlow(
//                flow, range, startDate, endDate);
//
//        // Allow CDN / browser to cache for 30s — reduces repeated API hits
//        return ResponseEntity.ok()
//                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).mustRevalidate())
//                .body(response);
//    }
//
//    // ── Left Sidebar Flow Counts ───────────────────────────────────────
//    /**
//     * GET /api/dashboard/counts
//     *
//     * Lightweight endpoint — sidebar calls this every 30s.
//     * Returns only per-flow counts, conversations total, and leads total.
//     * All aggregation is done in DashboardServiceImpl.getFlowCounts().
//     */
//    @GetMapping("/dashboard/counts")
//    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
//    public ResponseEntity<Map<String, Long>> getCounts() {
//        return ResponseEntity.ok()
//                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
//                .body(dashboardService.getFlowCounts());
//    }
//
//    // ── Sessions — Paginated with Server-Side Filters ─────────────────
//    /**
//     * GET /api/bot-sessions
//     *
//     * Fully server-side paginated. No 500-row cap.
//     * All filters applied in SQL (index-friendly) — handles 10k+ sessions efficiently.
//     *
//     * Params:
//     *   flow        bday_t10 | bday_t0 | anniv_t10 | anniv_t0
//     *   range       today | 7days | 30days | custom
//     *   startDate   yyyy-MM-dd (only when range=custom)
//     *   endDate     yyyy-MM-dd (only when range=custom)
//     *   collection  MENS | WOMENS | COUPLES (optional)
//     *   brand       exact brand value (optional)
//     *   step        exact current_step value (optional)
//     *   search      name or phone substring (optional)
//     *   sortField   customerName | phone | currentStep | selectedCollection |
//     *               selectedBrand | lastActivity  (default: lastActivity)
//     *   sortDir     asc | desc  (default: desc)
//     *   page        0-based page index  (default: 0)
//     *   size        rows per page, max 500  (default: 100)
//     *
//     * Response: { sessions[], totalSessions, totalPages, currentPage, pageSize }
//     */
//    @GetMapping("/bot-sessions")
//    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
//    public ResponseEntity<Map<String, Object>> getSessions(
//            @RequestParam(defaultValue = "bday_t10")     String flow,
//            @RequestParam(defaultValue = "today")        String range,
//            @RequestParam(required = false)              String startDate,
//            @RequestParam(required = false)              String endDate,
//            @RequestParam(required = false)              String collection,
//            @RequestParam(required = false)              String brand,
//            @RequestParam(required = false)              String step,
//            @RequestParam(required = false)              String search,
//            @RequestParam(defaultValue = "lastActivity") String sortField,
//            @RequestParam(defaultValue = "desc")         String sortDir,
//            @RequestParam(defaultValue = "0")            int    page,
//            @RequestParam(defaultValue = "100")          int    size
//    ) {
//        // HTTP-level cap: prevent someone passing size=999999 and causing timeouts.
//        // ServiceImpl also enforces its own cap of 500 — belt and suspenders.
//        int safeSize = Math.min(Math.max(size, 1), 500);
//
//        Map<String, Object> result = dashboardService.getSessionsPaginated(
//                flow, range, startDate, endDate,
//                collection, brand, step, search,
//                sortField, sortDir,
//                Math.max(page, 0), safeSize);
//
//        // Sessions are live data — do not cache
//        return ResponseEntity.ok()
//                .cacheControl(CacheControl.noStore())
//                .body(result);
//    }
//
//    /**
//     * GET /api/bot-sessions/flow/{flow}
//     * Legacy endpoint — still supported. Returns enriched session list filtered by flow.
//     */
//    @GetMapping("/bot-sessions/flow/{flow}")
//    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
//    public List<DashboardResponse.SessionDto> getSessionsByFlow(@PathVariable String flow) {
//        return dashboardService.getSessions()
//                .stream()
//                .filter(s -> flow.equalsIgnoreCase(s.getFlow()))
//                .toList();
//    }
//
//    // ── Leads — Paginated ─────────────────────────────────────────────
//    /**
//     * GET /api/leads?page=0&size=100&flow=bday_t10&range=today
//     *
//     * Paginated. Max page size enforced at 200 to prevent timeout.
//     *
//     * Response:
//     * {
//     *   leads:       [...],    // current page items
//     *   totalLeads:  1234,     // total matching records
//     *   totalPages:  13,
//     *   currentPage: 0,
//     *   pageSize:    100
//     * }
//     */
//    @GetMapping("/leads")
//    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
//    public ResponseEntity<Map<String, Object>> getLeads(
//            @RequestParam(defaultValue = "0")       int    page,
//            @RequestParam(defaultValue = "100")     int    size,
//            @RequestParam(required = false)         String flow,
//            @RequestParam(defaultValue = "today")   String range,
//            @RequestParam(required = false)         String startDate,
//            @RequestParam(required = false)         String endDate
//    ) {
//        // Hard-cap to 200 — prevents OOM from large page requests
//        int safeSize = Math.min(Math.max(size, 1), 200);
//
//        Pageable pageable = PageRequest.of(page, safeSize,
//                Sort.by("createdAt").descending());
//
//        Page<Lead> leadsPage = dashboardService.getLeadsPage(
//                flow, range, startDate, endDate, pageable);
//
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("leads",       leadsPage.getContent().stream()
//                .map(dashboardService::toLeadDto).toList());
//        body.put("totalLeads",  leadsPage.getTotalElements());
//        body.put("totalPages",  leadsPage.getTotalPages());
//        body.put("currentPage", page);
//        body.put("pageSize",    safeSize);
//
//        return ResponseEntity.ok(body);
//    }
//}

package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
import com.example.titan_watch_learning_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DashboardController — HTTP layer ONLY.
 *
 * Responsibilities:
 *  ✅  Read @RequestParam / @PathVariable from the HTTP request
 *  ✅  Apply HTTP-level guards (page-size cap, cache headers, security)
 *  ✅  Call dashboardService — the single point of contact for all business operations
 *  ✅  Return ResponseEntity with the service result
 *
 *  ❌  No date resolution     — lives in DashboardServiceImpl
 *  ❌  No flow→pattern logic  — lives in DashboardServiceImpl
 *  ❌  No direct repository calls — always goes through DashboardService
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    /** Single service dependency — no repository injected here. */
    private final DashboardService dashboardService;

    private final DashboardDataRepository repo;


    // ── Main Dashboard ─────────────────────────────────────────────────
    /**
     * GET /api/dashboard?flow=bday_t10&range=today
     *
     * range: today | 7days | 30days | custom
     * startDate / endDate: yyyy-MM-dd (only when range=custom)
     *
     * Response:
     *  sessions[]       capped display list
     *  leads[]          capped display list
     *  totalSessions    real COUNT from DB
     *  totalLeads       real COUNT from DB
     *  metrics          all 8 KPI tiles
     *  hourly, collData, timeline
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(defaultValue = "bday_t10") String flow,
            @RequestParam(defaultValue = "today")    String range,
            @RequestParam(required = false)          String startDate,
            @RequestParam(required = false)          String endDate
    ) {
        DashboardResponse response = dashboardService.getDashboardDataByFlow(
                flow, range, startDate, endDate);

        // Allow CDN / browser to cache for 30s — reduces repeated API hits
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).mustRevalidate())
                .body(response);
    }

    // ── Left Sidebar Flow Counts ───────────────────────────────────────
    /**
     * GET /api/dashboard/counts
     *
     * Lightweight endpoint — sidebar calls this every 30s.
     * Returns only per-flow counts, conversations total, and leads total.
     * All aggregation is done in DashboardServiceImpl.getFlowCounts().
     */
    @GetMapping("/dashboard/counts")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public ResponseEntity<Map<String, Long>> getCounts() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(dashboardService.getFlowCounts());
    }

    // ── Sessions — Paginated with Server-Side Filters ─────────────────
    /**
     * GET /api/bot-sessions
     *
     * Fully server-side paginated. No 500-row cap.
     * All filters applied in SQL (index-friendly) — handles 10k+ sessions efficiently.
     *
     * Params:
     *   flow        bday_t10 | bday_t0 | anniv_t10 | anniv_t0
     *   range       today | 7days | 30days | custom
     *   startDate   yyyy-MM-dd (only when range=custom)
     *   endDate     yyyy-MM-dd (only when range=custom)
     *   collection  MENS | WOMENS | COUPLES (optional)
     *   brand       exact brand value (optional)
     *   step        exact current_step value (optional)
     *   search      name or phone substring (optional)
     *   sortField   customerName | phone | currentStep | selectedCollection |
     *               selectedBrand | lastActivity  (default: lastActivity)
     *   sortDir     asc | desc  (default: desc)
     *   page        0-based page index  (default: 0)
     *   size        rows per page, max 500  (default: 100)
     *
     * Response: { sessions[], totalSessions, totalPages, currentPage, pageSize }
     */
    @GetMapping("/bot-sessions")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getSessions(
            @RequestParam(defaultValue = "bday_t10")     String flow,
            @RequestParam(defaultValue = "today")        String range,
            @RequestParam(required = false)              String startDate,
            @RequestParam(required = false)              String endDate,
            @RequestParam(required = false)              String collection,
            @RequestParam(required = false)              String brand,
            @RequestParam(required = false)              String step,
            @RequestParam(required = false)              String search,
            @RequestParam(defaultValue = "lastActivity") String sortField,
            @RequestParam(defaultValue = "desc")         String sortDir,
            @RequestParam(defaultValue = "0")            int    page,
            @RequestParam(defaultValue = "100")          int    size
    ) {
        // HTTP-level cap: prevent someone passing size=999999 and causing timeouts.
        // ServiceImpl also enforces its own cap of 500 — belt and suspenders.
        int safeSize = Math.min(Math.max(size, 1), 500);

        Map<String, Object> result = dashboardService.getSessionsPaginated(
                flow, range, startDate, endDate,
                collection, brand, step, search,
                sortField, sortDir,
                Math.max(page, 0), safeSize);

        // Sessions are live data — do not cache
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(result);
    }

    /**
     * GET /api/bot-sessions/flow/{flow}
     * Legacy endpoint — still supported. Returns enriched session list filtered by flow.
     */
    @GetMapping("/bot-sessions/flow/{flow}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public List<DashboardResponse.SessionDto> getSessionsByFlow(@PathVariable String flow) {
        return dashboardService.getSessions()
                .stream()
                .filter(s -> flow.equalsIgnoreCase(s.getFlow()))
                .toList();
    }

    // ── Leads — Paginated ─────────────────────────────────────────────
    /**
     * GET /api/leads?page=0&size=100&flow=bday_t10&range=today
     *
     * Paginated. Max page size enforced at 200 to prevent timeout.
     *
     * Response:
     * {
     *   leads:       [...],    // current page items
     *   totalLeads:  1234,     // total matching records
     *   totalPages:  13,
     *   currentPage: 0,
     *   pageSize:    100
     * }
     */
    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public ResponseEntity<Map<String, Object>> getLeads(
            @RequestParam(defaultValue = "0")       int    page,
            @RequestParam(defaultValue = "100")     int    size,
            @RequestParam(required = false)         String flow,
            @RequestParam(defaultValue = "today")   String range,
            @RequestParam(required = false)         String startDate,
            @RequestParam(required = false)         String endDate
    ) {
        // Hard-cap to 200 — prevents OOM from large page requests
        int safeSize = Math.min(Math.max(size, 1), 200);

        Pageable pageable = PageRequest.of(page, safeSize,
                Sort.by("createdAt").descending());

        Page<Lead> leadsPage = dashboardService.getLeadsPage(
                flow, range, startDate, endDate, pageable);

        // ★ FIX: Aggregate metrics from server SQL — same WHERE clause as getLeadsPage.
        //   These replace the client-side leads.filter(...) counts in LeadsPage.jsx
        //   which only counted the current page (≤100 rows) instead of the full DB result.
        //   Keys: total, callback, store_visit, new_count, converted
        Map<String, Long> leadMetrics = dashboardService.getLeadMetrics(
                flow, range, startDate, endDate);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("leads",       leadsPage.getContent().stream()
                .map(dashboardService::toLeadDto).toList());
        body.put("totalLeads",  leadsPage.getTotalElements());
        body.put("totalPages",  leadsPage.getTotalPages());
        body.put("currentPage", page);
        body.put("pageSize",    safeSize);
        body.put("leadMetrics", leadMetrics);

        return ResponseEntity.ok(body);
    }





}