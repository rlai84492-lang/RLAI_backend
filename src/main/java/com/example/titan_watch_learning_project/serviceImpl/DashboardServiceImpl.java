    package com.example.titan_watch_learning_project.serviceImpl;

    import com.example.titan_watch_learning_project.dto.DashboardResponse;
    import com.example.titan_watch_learning_project.entity.Lead;
    import com.example.titan_watch_learning_project.repository.DashboardDataRepository;
    import com.example.titan_watch_learning_project.repository.LeadRepository;
    import com.example.titan_watch_learning_project.service.DashboardService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.ZoneId;
    import java.util.*;

    /**
     * DashboardServiceImpl — ALL business logic lives here.
     *
     * Responsibilities:
     *  ✅  Date range resolution (IST-aware)
     *  ✅  flow key → SQL LIKE pattern (flowToPattern)
     *  ✅  Post-query enrichment of sessions, leads, activity events
     *  ✅  Collection / style / flow detection helpers
     *  ✅  Activity icon / text / bg / color computation
     *  ✅  Metrics calculation from raw COUNT maps
     *  ✅  Hourly chart construction
     *  ✅  Pagination result envelope building (getSessionsPaginated)
     *  ✅  Sidebar counts aggregation (getFlowCounts)
     *
     *  ❌  No SQL queries — all data access goes through DashboardDataRepository
     *  ❌  No HTTP concerns — controller handles those
     */
    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class DashboardServiceImpl implements DashboardService {

        private final DashboardDataRepository repo;
        private final LeadRepository          leadRepository;

        /**
         * IST timezone — CRITICAL.
         * Server runs in UTC. LocalDate.now() without a zone returns the UTC date.
         * At 12:01 AM IST = 6:31 PM UTC the previous day — so "today" in UTC
         * is yesterday in India, and the filter misses all new records.
         * Fix: always use LocalDate.now(IST).
         */
        private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

        // ════════════════════════════════════════════════════════════
        // ── DASHBOARD ──────────────────────────────────────────────
        // ════════════════════════════════════════════════════════════

        @Override
        public DashboardResponse getDashboardData() {
            return getDashboardDataByFlow("bday_t10", "today", null, null);
        }

        /**
         * Main dashboard data method — called by GET /api/dashboard.
         *
         * Flow:
         *  1. Resolve date range → LocalDateTime pair (IST-aware)
         *  2. Convert flow key → SQL LIKE pattern
         *  3. Fetch aggregate metrics (two fast COUNT queries — no row fetch)
         *  4. Fetch capped display lists and enrich each item
         *  5. Build and return DashboardResponse
         */
        @Override
        public DashboardResponse getDashboardDataByFlow(
                String flow, String range, String startDate, String endDate) {

            LocalDateTime[] dt      = resolveDateTimeRange(range, startDate, endDate);
            LocalDateTime   from    = dt[0];
            LocalDateTime   to      = dt[1];
            String          pattern = flowToPattern(flow);

            log.info("Dashboard → flow={} range={} from={} to={}", flow, range, from, to);

            // ── Aggregate metrics (fast COUNT queries — no row fetching) ──
            Map<String, Long> sm = repo.getSessionMetrics(pattern, from, to);
            Map<String, Long> lm = repo.getLeadMetrics(flow, pattern, from, to);

            long delivered = repo.countDeliveredMessages(pattern, from, to);
            long sent      = repo.countSentMessages(pattern, from, to);

            // ── Display lists (capped at MAX_SESSION/LEAD_ROWS) + enrichment ──
            List<DashboardResponse.SessionDto> sessions =
                    repo.findSessionsByFlowAndDate(pattern, from, to)
                            .stream().map(this::enrichSession).toList();

            List<DashboardResponse.LeadDto> leads =
                    repo.findLeadsByFlowAndDate(flow, pattern, from, to)
                            .stream().map(this::enrichLead).toList();

            List<DashboardResponse.ActivityEventDto> timeline =
                    repo.findRecentActivityByFlowAndDate(pattern, from, to)
                            .stream().map(this::enrichActivity).toList();


            // Existing code ke saath yahan add karo:
            Map<String, Long> stepCounts = repo.getStepCounts(pattern, from, to);

            log.info("stepCounts size={} data={}", stepCounts.size(), stepCounts);  // ← ADD


            return DashboardResponse.builder()
                    .sessions(sessions)
                    .leads(leads)
                    .totalSessions(sm.getOrDefault("total", (long) sessions.size()))
                    .totalLeads(lm.getOrDefault("total",    (long) leads.size()))
//                    .metrics(buildMetrics(sm, lm, delivered, sent))
                        .metrics(buildMetrics(sm, lm, delivered, sent, pattern, from, to))

                    .hourly(buildHourlyMessages())
                    .collData(repo.getCollectionSplit(pattern, from, to))
                    .timeline(timeline)
                    .stepCounts(stepCounts)     // ← NAYA
                    .build();
        }

        @Override
        public DashboardResponse getDashboardDataByFlow(String flow) {
            return getDashboardDataByFlow(flow, "today", null, null);
        }

        @Override
        public List<DashboardResponse.SessionDto> getSessions() {
            return repo.findSessions().stream().map(this::enrichSession).toList();
        }

        // ════════════════════════════════════════════════════════════
        // ── SESSIONS — PAGINATED ───────────────────────────────────
        // ════════════════════════════════════════════════════════════

        /**
         * Paginated sessions with all server-side filters applied.
         * Called by GET /api/bot-sessions.
         *
         * All business decisions (date resolution, flow→pattern, enrichment,
         * result envelope construction) happen here.
         * Repository only executes the SQL and maps columns.
         */
        @Override
        public Map<String, Object> getSessionsPaginated(
                String flow, String range, String startDate, String endDate,
                String collection, String brand, String step, String search,
                String sortField, String sortDir,
                int page, int size) {

            LocalDateTime[] dt      = resolveDateTimeRange(range, startDate, endDate);
            String          pattern = flowToPattern(flow);
            int             safeSize = Math.min(Math.max(size, 1), 500);
            int             safePage = Math.max(page, 0);

            // COUNT first — cheap, used for pagination metadata
            long total = repo.countSessionsPaginated(
                    pattern, dt[0], dt[1], collection, brand, step, search);

            // Fetch page + enrich
            List<DashboardResponse.SessionDto> sessions =
                    repo.fetchSessionsPaginated(
                                    pattern, dt[0], dt[1], collection, brand, step, search,
                                    sortField, sortDir, safePage, safeSize)
                            .stream().map(this::enrichSession).toList();

            // Build result envelope (business decision — lives in service, not controller)
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("sessions",      sessions);
            result.put("totalSessions", total);
            result.put("totalPages",    (int) Math.ceil((double) total / safeSize));
            result.put("currentPage",   safePage);
            result.put("pageSize",      safeSize);
            return result;
        }

        // ════════════════════════════════════════════════════════════
        // ── COUNTS / LEFT SIDEBAR ──────────────────────────────────
        // ════════════════════════════════════════════════════════════

        /**
         * Aggregates active session counts per flow + total leads count.
         * Called by GET /api/dashboard/counts every 30s from the sidebar.
         *
         * Business rule: "conversations" = all active bot sessions.
         */
        @Override
        public Map<String, Long> getFlowCounts() {
            Map<String, Long> counts = new LinkedHashMap<>(repo.findFlowCounts());
            counts.put("conversations", repo.countActiveSessions());
            counts.put("leads",         leadRepository.count());
            return counts;
        }

        // ════════════════════════════════════════════════════════════
        // ── LEADS ──────────────────────────────────────────────────
        // ════════════════════════════════════════════════════════════

        @Override
        public Page<Lead> getLeadsPage(
                String flow, String range, String startDate, String endDate, Pageable pageable) {

            LocalDateTime[] dt = resolveDateTimeRange(range, startDate, endDate);

            if (flow != null && !flow.isBlank()) {
                // ★ FIX: Use (flow = ? OR stepName LIKbuildE ?) — same WHERE as getLeadMetrics SQL.
                // The old query only used "flow = ?" so leads with a null/empty flow column
                // but a matching step_name were excluded → mismatch vs dashboard callback count.
                String pattern = flowToPattern(flow);
                return leadRepository.findByFlowOrStepPatternAndDateRange(
                        flow, pattern, dt[0], dt[1], pageable);
            }
            return leadRepository.findByDateRange(dt[0], dt[1], pageable);
        }

        @Override
        public Map<String, Long> getLeadMetrics(
                String flow, String range, String startDate, String endDate) {

            LocalDateTime[] dt = resolveDateTimeRange(range, startDate, endDate);
            // Delegates to the native SQL SUM(CASE WHEN ...) query — fast, no row fetch.
            // Same (flow = ? OR step_name LIKE ?) clause as getLeadsPage so counts match.
            return repo.getLeadMetrics(flow, flowToPattern(flow), dt[0], dt[1]);
        }

        @Override
        public List<DashboardResponse.LeadDto> getLeads() {
            return leadRepository.findAllByOrderByCreatedAtDesc()
                    .stream().map(this::toLeadDto).toList();
        }

        @Override
        public DashboardResponse.LeadDto toLeadDto(Lead lead) {
            return DashboardResponse.LeadDto.builder()
                    .id(lead.getId())
                    .customerName(lead.getCustomerName())
                    .phone(lead.getPhone())
                    .leadType(lead.getLeadType()  == null ? null : lead.getLeadType().name())
                    .flow(lead.getFlow())
                    .selectedCollection(lead.getSelectedCollection())
                    .selectedBrand(lead.getSelectedBrand())
                    .stepName(lead.getStepName())
                    .status(lead.getStatus() == null ? null : lead.getStatus().name())
                    .notes(lead.getNotes())
                    .createdAt(lead.getCreatedAt())
                    .build();
        }

        // ════════════════════════════════════════════════════════════
        // ── DATE RANGE RESOLVER ────────────────────────────────────
        // ════════════════════════════════════════════════════════════

        /**
         * Converts a range string to [from (inclusive), to (exclusive)] as LocalDateTime.
         *
         * All SQL queries use:  col >= from  AND  col < to
         * This is index-friendly and timezone-correct for IST.
         *
         * Example for "today" on 2026-06-20 IST:
         *   from = 2026-06-20T00:00:00
         *   to   = 2026-06-21T00:00:00
         * → selects everything in the full 24h window of June 20th IST.
         *
         * Previously duplicated in DashboardController as resolveSessionDates() — removed from there.
         */
        private LocalDateTime[] resolveDateTimeRange(String range, String startDate, String endDate) {
            LocalDate today = LocalDate.now(IST);   // ← IST, not UTC

            LocalDate fromDate;
            LocalDate toDate;

            switch (range == null ? "today" : range) {
                case "7days"  -> { fromDate = today.minusDays(6);  toDate = today; }
                case "30days" -> { fromDate = today.minusDays(29); toDate = today; }
                case "custom" -> {
                    try {
                        fromDate = (startDate != null && !startDate.isBlank())
                                ? LocalDate.parse(startDate) : today;
                        toDate   = (endDate != null && !endDate.isBlank())
                                ? LocalDate.parse(endDate)   : today;
                    } catch (Exception ex) {
                        log.warn("Invalid custom date range startDate={} endDate={}, falling back to today",
                                startDate, endDate);
                        fromDate = today;
                        toDate   = today;
                    }
                }
                default -> { fromDate = today; toDate = today; }   // "today"
            }

            return new LocalDateTime[]{
                    fromDate.atStartOfDay(),            // 00:00:00 — inclusive
                    toDate.plusDays(1).atStartOfDay()   // 00:00:00 next day — exclusive
            };
        }

        // ════════════════════════════════════════════════════════════
        // ── FLOW → SQL PATTERN  (moved here from repository) ───────
        // ════════════════════════════════════════════════════════════

        /**
         * Converts a frontend flow key to the SQL LIKE pattern used in current_step queries.
         * Business knowledge about what each flow key means belongs in the service layer.
         */
        private String flowToPattern(String flow) {
            if (flow == null) return "BIRTHDAY_T10_%";
            return switch (flow) {
                case "bday_t10"  -> "BIRTHDAY_T10_%";
                case "bday_t0"   -> "BIRTHDAY_TDAY_%";
                case "anniv_t10" -> "ANNIVERSARY_T10_%";
                case "anniv_t0"  -> "ANNIVERSARY_TDAY_%";
                default          -> "BIRTHDAY_T10_%";
            };
        }

        // ════════════════════════════════════════════════════════════
        // ── ENRICHMENT METHODS (post-query business logic) ──────────
        // These methods take raw DTO from the repo and apply the business rules.
        // ════════════════════════════════════════════════════════════

        /**
         * Applies all session business rules to a raw SessionDto from the repository.
         * - Defaults customerName to "WhatsApp User" if blank
         * - Derives flow from current_step
         * - Normalizes selectedCollection, falls back to step-embedded collection
         * - Extracts selectedStyle from step name
         */
        private DashboardResponse.SessionDto enrichSession(DashboardResponse.SessionDto raw) {
            String rawStep    = raw.getCurrentStep();
            String collection = normalizeCollection(raw.getSelectedCollection());
            if (collection == null || collection.isBlank()) {
                collection = extractCollection(rawStep);
            }
            return DashboardResponse.SessionDto.builder()
                    .id(raw.getId())
                    .customerName(defaultIfBlank(raw.getCustomerName(), "WhatsApp User"))
                    .phone(raw.getPhone())
                    .currentStep(rawStep)
                    .rawStep(rawStep)
                    .flow(detectFlow(rawStep))
                    .selectedCollection(collection)
                    .selectedBrand(defaultIfBlank(raw.getSelectedBrand(), null))
                    .selectedStyle(extractStyle(rawStep))
                    .isActive(raw.getIsActive())
                    .lastActivity(raw.getLastActivity())
                    .build();
        }

        /**
         * Applies all lead business rules to a raw LeadDto from the repository.
         * - Defaults customerName to "WhatsApp User" if blank
         * - Defaults leadType to "CALLBACK" if blank
         * - Detects flow from step_name when l.flow column is empty
         * - Normalizes selectedCollection
         * - Defaults status to "NEW" if blank
         */
        private DashboardResponse.LeadDto enrichLead(DashboardResponse.LeadDto raw) {
            String flow     = raw.getFlow();
            String stepName = raw.getStepName();
            return DashboardResponse.LeadDto.builder()
                    .id(raw.getId())
                    .customerName(defaultIfBlank(raw.getCustomerName(), "WhatsApp User"))
                    .phone(raw.getPhone())
                    .leadType(defaultIfBlank(raw.getLeadType(), "CALLBACK"))
                    .flow(flow == null || flow.isBlank() ? detectFlow(stepName) : flow)
                    .stepName(stepName)
                    .selectedCollection(normalizeCollection(raw.getSelectedCollection()))
                    .selectedBrand(raw.getSelectedBrand())
                    .status(defaultIfBlank(raw.getStatus(), "NEW"))
                    .notes(raw.getNotes())
                    .createdAt(raw.getCreatedAt())
                    .build();
        }

        /**
         * Converts a raw RawActivityRow from the repository to an ActivityEventDto.
         * All icon, text, bg, color rules live here — never in the repository.
         */
        private DashboardResponse.ActivityEventDto enrichActivity(
                DashboardDataRepository.RawActivityRow row) {
            String name      = defaultIfBlank(row.customerName(), "WhatsApp User");
            String direction = row.direction();
            String payload   = row.buttonPayload();
            String content   = row.messageContent();
            return DashboardResponse.ActivityEventDto.builder()
                    .icon(activityIcon(payload, direction))
                    .text(buildActivityText(name, direction, payload, content))
                    .time(row.sentAt())
                    .bg(activityBg(payload, direction))
                    .color(activityColor(payload, direction))
                    .build();
        }

        // ════════════════════════════════════════════════════════════
        // ── FLOW / COLLECTION / STYLE HELPERS (moved from repository)
        // ════════════════════════════════════════════════════════════

        /** Derives the frontend flow key from a raw current_step value. */
        private String detectFlow(String rawStep) {
            if (rawStep == null || rawStep.isBlank()) return "bday_t10";
            String s = rawStep.toUpperCase();
            if (s.startsWith("BIRTHDAY_T10_"))     return "bday_t10";
            if (s.startsWith("BIRTHDAY_TDAY_"))    return "bday_t0";
            if (s.startsWith("ANNIVERSARY_T10_"))  return "anniv_t10";
            if (s.startsWith("ANNIVERSARY_TDAY_")) return "anniv_t0";
            return "bday_t10";
        }

        /**
         * Normalizes a raw selected_collection value to one of: MENS, WOMENS, COUPLES, or null.
         * Handles mixed-case values and legacy spellings (MALE/FEMALE).
         */
        private String normalizeCollection(String value) {
            if (value == null || value.isBlank()) return null;
            String s = value.toUpperCase();
            if (s.contains("FEMALE") || s.contains("WOMEN")) return "WOMENS";
            if (s.contains("MALE")   || s.contains("MEN"))   return "MENS";
            if (s.contains("COUPLES"))                        return "COUPLES";
            return s;
        }

        /**
         * Extracts collection from the current_step step name when selected_collection is empty.
         * e.g. "BIRTHDAY_T10_MENS_STYLE_BOLD_EDGY" → "MENS"
         */
        private String extractCollection(String rawStep) {
            if (rawStep == null) return null;
            String s = rawStep.toUpperCase();
            if (s.contains("FEMALE") || s.contains("WOMEN")) return "WOMENS";
            if (s.contains("MALE")   || s.contains("MEN"))   return "MENS";
            if (s.contains("COUPLES"))                        return "COUPLES";
            return null;
        }

        /**
         * Extracts the selected style from the current_step name.
         * e.g. "BIRTHDAY_T10_MENS_STYLE_BOLD_EDGY" → "BOLD_EDGY"
         */
        private String extractStyle(String rawStep) {
            if (rawStep == null) return null;
            String s = rawStep.toUpperCase();
            if (s.contains("STYLE_MINIMAL_CHIC"))       return "MINIMAL_CHIC";
            if (s.contains("STYLE_BOLD_EDGY"))          return "BOLD_EDGY";
            if (s.contains("STYLE_LUXE_CLASSY"))        return "LUXE_CLASSY";
            if (s.contains("STYLE_SPORTY_ADVENTUROUS")) return "SPORTY_ADVENTUROUS";
            return null;
        }

        // ════════════════════════════════════════════════════════════
        // ── ACTIVITY EVENT HELPERS (moved from repository) ──────────
        // ════════════════════════════════════════════════════════════

        /** Builds a human-readable description for an activity event. */
        private String buildActivityText(String name, String direction, String payload, String content) {
            String p = payload == null ? "" : payload.toUpperCase();
            if (p.contains("MENS_COLLECTION"))         return name + " selected Men's Collection";
            if (p.contains("WOMENS_COLLECTION"))        return name + " selected Women's Collection";
            if (p.contains("STYLE_BOLD_EDGY"))          return name + " selected Bold & Edgy";
            if (p.contains("STYLE_MINIMAL_CHIC"))       return name + " selected Minimal & Chic";
            if (p.contains("STYLE_LUXE_CLASSY"))        return name + " selected Luxe & Classy";
            if (p.contains("STYLE_SPORTY_ADVENTUROUS")) return name + " selected Sporty & Adventurous";
            if (p.contains("PRICE_2K_5K"))              return name + " chose Rs.2k-5k range";
            if (p.contains("PRICE_5K_10K"))             return name + " chose Rs.5k-10k range";
            if (p.contains("PRICE_10K_25K"))            return name + " chose Rs.10k-25k range";
            if (p.contains("PRICE_25K_PLUS"))           return name + " chose Rs.25k+ range";
            if (p.contains("REQUEST_CALLBACK"))         return name + " requested a callback";
            if (p.contains("BOOK_STORE_VISIT"))         return name + " booked a store visit";
            if ("INBOUND".equalsIgnoreCase(direction))  return name + " sent a message";
            return "Bot replied to " + name;
        }

        /** Returns an icon code for the activity event type. */
        private String activityIcon(String payload, String direction) {
            String p = payload == null ? "" : payload.toUpperCase();
            if (p.contains("REQUEST_CALLBACK")) return "CALLBACK";
            if (p.contains("BOOK_STORE_VISIT")) return "STORE";
            if (p.contains("PRICE_"))           return "PRICE";
            if (p.contains("STYLE_"))           return "STYLE";
            if (p.contains("COLLECTION"))       return "COLLECTION";
            if ("OUTBOUND".equalsIgnoreCase(direction)) return "BOT";
            return "MSG";
        }

        /** Returns a background color hex for the activity event. */
        private String activityBg(String payload, String direction) {
            String p = payload == null ? "" : payload.toUpperCase();
            if (p.contains("REQUEST_CALLBACK")) return "#E1F5EE";
            if (p.contains("PRICE_"))           return "#FEF3CD";
            if (p.contains("STYLE_"))           return "#EEEDFE";
            if ("OUTBOUND".equalsIgnoreCase(direction)) return "#FEF0EB";
            return "#EBF4FD";
        }

        /** Returns a text/icon color hex for the activity event. */
        private String activityColor(String payload, String direction) {
            String p = payload == null ? "" : payload.toUpperCase();
            if (p.contains("REQUEST_CALLBACK")) return "#1D9E75";
            if (p.contains("PRICE_"))           return "#BA7517";
            if (p.contains("STYLE_"))           return "#7F77DD";
            if ("OUTBOUND".equalsIgnoreCase(direction)) return "#E85A2B";
            return "#378ADD";
        }

        // ════════════════════════════════════════════════════════════
        // ── METRICS — built from COUNT maps, not list iteration ─────
        // ════════════════════════════════════════════════════════════

        /**
         * Builds all 8 primary metric tiles + legacy compat fields.
         * Numbers come from pre-computed COUNT maps — no sessions/leads lists needed.
         */
//        private DashboardResponse.MetricsDto buildMetrics(
//                Map<String, Long> sm, Map<String, Long> lm,
//                long delivered, long sent) {
//
//            long messagesSent   = sm.getOrDefault("total",     0L);
//            long activeSessions = sm.getOrDefault("active",    0L);
//            long replied        = sm.getOrDefault("replied",   0L);
//            long catalogueViews = sm.getOrDefault("catalogue", 0L);
//            long completedFlows = sm.getOrDefault("completed", 0L);
//
//
//            long callbackLeads  = lm.getOrDefault("callback",    0L);
//            long storeVisits    = lm.getOrDefault("store_visit", 0L);
//            long newLeads       = lm.getOrDefault("new_count",   0L);
//            long converted      = lm.getOrDefault("converted",   0L);
//
//            int deliveryRate   = pct(delivered, sent);          // ← sent = DISTINCT phones now ✅
//            int openRate       = pct(replied, delivered);        // ★ FIX: replied/delivered (not /messagesSent)
//            int clickRate      = pct(callbackLeads + storeVisits, delivered); // ★ FIX: /delivered
//            int completionRate = pct(completedFlows, delivered); // ★ FIX: /delivered
//            int conversionRate = pct(converted, delivered);      // ★ FIX: /delivered
//
//
//            System.out.println(deliveryRate + " " + openRate + " " + clickRate + " " + completionRate + " " + conversionRate + "tested" );
//
//            return DashboardResponse.MetricsDto.builder()
//                    // 8 primary KPI tiles
//                    .messagesSent(messagesSent)
//                    .deliveryRate(deliveryRate)
//                    .openRate(openRate)
//                    .clickRate(clickRate)
//                    .callbackRequests(callbackLeads)
//                    .storeVisitRequests(storeVisits)
//                    .catalogueViews(catalogueViews)
//                    .completionRate(completionRate)
//                    // Legacy backward-compat fields
//                    .totalReached(messagesSent)
//                    .activeSessions(activeSessions)
//                    .callbackLeads(callbackLeads)
//                    .storeVisits(storeVisits)
//                    .completedFlows(completedFlows)
//                    .newLeads(newLeads)
//                    .converted(converted)
//                    .conversionRate(conversionRate)
//                    .build();
//        }



//        private DashboardResponse.MetricsDto buildMetrics(
//                Map<String, Long> sm, Map<String, Long> lm,
//                long delivered, long sent,
//                String pattern, LocalDateTime from, LocalDateTime to) {
//
//            long messagesSent   = sm.getOrDefault("total",     0L);
//            long activeSessions = sm.getOrDefault("active",    0L);
//            long catalogueViews = sm.getOrDefault("catalogue", 0L);
//            long completedFlows = sm.getOrDefault("completed", 0L);
//
//            long callbackLeads  = lm.getOrDefault("callback",    0L);
//            long storeVisits    = lm.getOrDefault("store_visit", 0L);
//            long newLeads       = lm.getOrDefault("new_count",   0L);
//            long converted      = lm.getOrDefault("converted",   0L);
//
//            // ── NAYA — "Opens" aur "Clicks" genuinely messages table se ──
//            // (session-based "replied" ki jagah, taaki concept sahi match kare)
//            long readMessages = repo.countReadMessages(pattern, from, to);
//            long buttonClicks = repo.countButtonClicks(pattern, from, to);
//
//            int deliveryRate   = pct(delivered, sent);                          // Delivered / Sent
//            int openRate       = pct(readMessages, delivered);                  // Opens / Delivered
//            int clickRate      = pct(buttonClicks, delivered);                  // Clicks / Delivered
//            int completionRate = pct(completedFlows, delivered);                // Completed / Delivered
//            int conversionRate = pct(converted, delivered);                     // Converted / Delivered
//
//
//            System.out.println(delivered + deliveryRate +  " messages sent" + openRate + clickRate + completionRate + conversionRate);
//
//
//            return DashboardResponse.MetricsDto.builder()
//                    // 8 primary KPI tiles
//                    .messagesSent(messagesSent)
//                    .deliveryRate(deliveryRate)
//                    .openRate(openRate)
//                    .clickRate(clickRate)
//                    .callbackRequests(callbackLeads)
//                    .storeVisitRequests(storeVisits)
//                    .catalogueViews(catalogueViews)
//                    .completionRate(completionRate)
//                    // Legacy backward-compat fields
//                    .totalReached(messagesSent)
//                    .activeSessions(activeSessions)
//                    .callbackLeads(callbackLeads)
//                    .storeVisits(storeVisits)
//                    .completedFlows(completedFlows)
//                    .newLeads(newLeads)
//                    .converted(converted)
//                    .conversionRate(conversionRate)
//                    .build();
//        }





        private DashboardResponse.MetricsDto buildMetrics(
                Map<String, Long> sm, Map<String, Long> lm,
                long delivered, long sent,
                String pattern, LocalDateTime from, LocalDateTime to) {

            System.out.println(delivered + "delivered" + sent + "sent");

            long activeSessions = sm.getOrDefault("active",    0L);
            long catalogueViews = sm.getOrDefault("catalogue", 0L);
//            long completedFlows = sm.getOrDefault("completed", 0L);
            long completedFlows = repo.countCompletedFlows(pattern, from, to);
            int completionRate = pct(completedFlows, delivered);


            System.out.println("completedFlows: " + completedFlows);


            long callbackLeads  = lm.getOrDefault("callback",    0L);
            long storeVisits    = lm.getOrDefault("store_visit", 0L);

            System.out.println("callbackLeads: " + callbackLeads + ";storeVisits: " + storeVisits);

            long newLeads       = lm.getOrDefault("new_count",   0L);
//            long converted      = lm.getOrDefault("converted",   0L);
            long totalLeads     = lm.getOrDefault("total",        0L);

            System.out.println(totalLeads  + "Total Leads");

            double conversionRate = pctFloat(totalLeads, delivered);


            // ── "Opens" aur "Clicks" genuinely messages table se ──
            // (session-based "replied" ki jagah, taaki concept sahi match kare)
            long readMessages = repo.countReadMessages(pattern, from, to);
            long buttonClicks = repo.countButtonClicks(pattern, from, to);

            int deliveryRate   = pct(delivered, sent);                          // Delivered / Sent
            int openRate       = pct(readMessages, delivered);                  // Opens / Delivered
            int clickRate      = pct(buttonClicks, delivered);                  // Clicks / Delivered
//            int completionRate = pct(completedFlows, delivered);                // Completed / Delivered
//            int conversionRate = pct(converted, delivered);                     // Converted / Delivered

            return DashboardResponse.MetricsDto.builder()
                    // 8 primary KPI tiles
                    .messagesSent(sent)                  // ★ FIX: "sent" (messages table) — pehle "sm.total" (bot_sessions count) tha
                    .deliveryRate(deliveryRate)
                    .openRate(openRate)
                    .clickRate(clickRate)
                    .callbackRequests(callbackLeads)
                    .storeVisitRequests(storeVisits)
                    .catalogueViews(catalogueViews)
                    .completionRate(completionRate)
                    // Legacy backward-compat fields
                    .totalReached(sent)                  // ★ FIX: same yahan bhi — "sent" use karo
                    .activeSessions(activeSessions)
                    .callbackLeads(callbackLeads)
                    .storeVisits(storeVisits)
                    .completedFlows(completedFlows)
                    .newLeads(newLeads)
//                    .converted(converted)
                    .conversionRate(conversionRate)
                    .build();
        }



        /** Safe percentage — avoids divide-by-zero, caps at 100. */
        private int pct(long num, long den) {
            if (den == 0) return 0;
            return (int) Math.min(100, Math.round((num * 100.0) / den));
        }

        /** Floating-point percentage for conversionRate — 2 decimal places, no cap. */
        private double pctFloat(long num, long den) {
            if (den == 0) return 0.0;
            return Math.round((num * 10000.0) / den) / 100.0;
        }

        // ════════════════════════════════════════════════════════════
        // ── HOURLY MESSAGES CHART ──────────────────────────────────
        // ════════════════════════════════════════════════════════════

        /** Builds the hourly inbound/outbound chart for today (08:00–20:00). */
        private DashboardResponse.HourlyMessagesDto buildHourlyMessages() {
            Map<Integer, Map<String, Long>> dbCounts = repo.findMessageCountsToday();

            List<String> labels   = new ArrayList<>();
            List<Long>   inbound  = new ArrayList<>();
            List<Long>   outbound = new ArrayList<>();

            for (int hour = 8; hour <= 20; hour++) {
                labels.add(String.format("%02d:00", hour));
                Map<String, Long> c = dbCounts.getOrDefault(hour, new HashMap<>());
                inbound.add(c.getOrDefault("INBOUND",  0L));
                outbound.add(c.getOrDefault("OUTBOUND", 0L));
            }

            return DashboardResponse.HourlyMessagesDto.builder()
                    .labels(labels).inbound(inbound).outbound(outbound).build();
        }

        // ════════════════════════════════════════════════════════════
        // ── SHARED UTILITY ─────────────────────────────────────────
        // ════════════════════════════════════════════════════════════

        /** Returns fallback when value is null or blank. */
        private String defaultIfBlank(String value, String fallback) {
            return (value == null || value.isBlank()) ? fallback : value;
        }
    }

