package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DashboardDataRepository — ONLY native SQL queries + basic column→field mapping.
 *
 * Rules enforced here:
 *  ✅  SQL queries via JdbcTemplate
 *  ✅  RowMapper: copies DB column values directly to DTO fields
 *  ✅  Dynamic WHERE clause builder (query-building, not business logic)
 *  ✅  JDBC utilities: safe(), toIso()
 *
 *  ❌  No flowToPattern()       — lives in DashboardServiceImpl
 *  ❌  No normalizeCollection() — lives in DashboardServiceImpl
 *  ❌  No extractCollection()   — lives in DashboardServiceImpl
 *  ❌  No detectFlow()          — lives in DashboardServiceImpl
 *  ❌  No activity icon/color   — lives in DashboardServiceImpl
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DashboardDataRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** MAX rows returned in a single list call — prevents 504 timeouts. */
    private static final int MAX_SESSION_ROWS = 500;
    private static final int MAX_LEAD_ROWS    = 500;

    // ════════════════════════════════════════════════════════════
    // ── RAW ACTIVITY ROW ───────────────────────────────────────
    // Raw holder for messages + customers columns.
    // ServiceImpl converts this to ActivityEventDto (icon/text/bg/color are business logic).
    // ════════════════════════════════════════════════════════════

    public record RawActivityRow(
            String phone,
            String customerName,
            String direction,
            String messageContent,
            String buttonPayload,
            String sentAt             // ISO-formatted timestamp string
    ) {}

    // ════════════════════════════════════════════════════════════
    // ── SESSIONS — LIST ────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /**
     * All flows, no date filter — legacy/fallback.
     * Capped at MAX_SESSION_ROWS. Returned DTOs are raw (not enriched).
     */
    public List<DashboardResponse.SessionDto> findSessions() {
        String sql =
                "SELECT bs.id, bs.phone, bs.current_step, bs.is_active, " +
                        "       bs.last_activity, bs.selected_collection, bs.selected_brand, " +
                        "       c.name AS customer_name " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.phone = bs.phone " +
                        "WHERE (bs.current_step LIKE 'BIRTHDAY_T10_%' " +
                        "    OR bs.current_step LIKE 'BIRTHDAY_TDAY_%' " +
                        "    OR bs.current_step LIKE 'ANNIVERSARY_T10_%' " +
                        "    OR bs.current_step LIKE 'ANNIVERSARY_TDAY_%') " +
                        "ORDER BY bs.last_activity DESC " +
                        "LIMIT " + MAX_SESSION_ROWS;

        return jdbcTemplate.query(sql,
                (rs, rn) -> { try { return mapSession(rs); } catch (Exception e) { throw new RuntimeException(e); }});
    }

    /**
     * Sessions filtered by flow pattern + date range.
     *
     * @param stepPattern  SQL LIKE pattern, e.g. "BIRTHDAY_T10_%"
     *                     (callers get this from DashboardServiceImpl.flowToPattern)
     * Uses index: idx_bs_step_activity(current_step, last_activity).
     * Key: >= and < instead of DATE() BETWEEN keeps the index hit.
     */
    public List<DashboardResponse.SessionDto> findSessionsByFlowAndDate(
            String stepPattern, LocalDateTime from, LocalDateTime to) {

        String sql =
                "SELECT bs.id, bs.phone, bs.current_step, bs.is_active, " +
                        "       bs.last_activity, bs.selected_collection, bs.selected_brand, " +
                        "       c.name AS customer_name " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.phone = bs.phone " +
                        "WHERE bs.current_step LIKE ? " +
                        "  AND bs.last_activity >= ? AND bs.last_activity < ? " +
                        "ORDER BY bs.last_activity DESC " +
                        "LIMIT " + MAX_SESSION_ROWS;

        return jdbcTemplate.query(sql, new Object[]{stepPattern, from, to},
                (rs, rn) -> { try { return mapSession(rs); } catch (Exception e) { throw new RuntimeException(e); }});
    }

    // ════════════════════════════════════════════════════════════
    // ── SESSIONS — PAGINATED (no hardcoded cap) ─────────────────
    // ════════════════════════════════════════════════════════════

    /**
     * COUNT of sessions matching the given filters.
     * Used by ServiceImpl to build pagination metadata (totalSessions, totalPages).
     *
     * @param stepPattern SQL LIKE pattern for current_step
     */
    public long countSessionsPaginated(
            String stepPattern,
            LocalDateTime from, LocalDateTime to,
            String collection, String brand, String step, String search) {

        WhereClause wc = buildSessionWhere(stepPattern, from, to, collection, brand, step, search);

        String sql = "SELECT COUNT(*) " +
                "FROM bot_sessions bs " +
                "LEFT JOIN customers c ON c.phone = bs.phone " +
                wc.sql();
        try {
            Long total = jdbcTemplate.queryForObject(sql, Long.class, wc.params().toArray());
            return total != null ? total : 0L;
        } catch (Exception e) {
            log.warn("countSessionsPaginated failed: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Paginated session data rows — no hardcoded cap.
     * Caller (ServiceImpl) controls size and page.
     *
     * sortField is whitelisted here against a fixed set of column names
     * to prevent SQL injection — this is query-building, not business logic.
     *
     * @param stepPattern SQL LIKE pattern for current_step
     * @param size        rows per page (already bounded by ServiceImpl)
     * @param page        0-based page index (already validated by ServiceImpl)
     */
    public List<DashboardResponse.SessionDto> fetchSessionsPaginated(
            String stepPattern,
            LocalDateTime from, LocalDateTime to,
            String collection, String brand, String step, String search,
            String sortField, String sortDir,
            int page, int size) {

        WhereClause wc = buildSessionWhere(stepPattern, from, to, collection, brand, step, search);

        // Whitelist sort columns to prevent SQL injection
        String orderCol = switch (sortField != null ? sortField : "") {
            case "customerName"       -> "c.name";
            case "phone"              -> "bs.phone";
            case "currentStep"        -> "bs.current_step";
            case "selectedCollection" -> "bs.selected_collection";
            case "selectedBrand"      -> "bs.selected_brand";
            default                   -> "bs.last_activity";
        };
        String dir = "asc".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        int offset = Math.max(page, 0) * size;

        List<Object> dataParams = new ArrayList<>(wc.params());
        dataParams.add(size);
        dataParams.add(offset);

        String sql =
                "SELECT bs.id, bs.phone, bs.current_step, bs.is_active, " +
                        "       bs.last_activity, bs.selected_collection, bs.selected_brand, " +
                        "       c.name AS customer_name " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.phone = bs.phone " +
                        wc.sql() +
                        " ORDER BY " + orderCol + " " + dir +
                        " LIMIT ? OFFSET ?";

        try {
            return jdbcTemplate.query(sql, dataParams.toArray(),
                    (rs, rn) -> { try { return mapSession(rs); } catch (Exception ex) { throw new RuntimeException(ex); }});
        } catch (Exception e) {
            log.warn("fetchSessionsPaginated data failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── SESSIONS — METRICS (aggregate COUNT queries, no rows) ──
    // ════════════════════════════════════════════════════════════

    /**
     * All session KPI metrics in ONE SQL query using SUM(CASE WHEN ...).
     * No rows are fetched — only aggregate numbers.
     *
     * @param stepPattern SQL LIKE pattern for current_step
     * @return map keys: total, active, replied, catalogue, completed
     */
    public Map<String, Long> getSessionMetrics(String stepPattern, LocalDateTime from, LocalDateTime to) {
        Map<String, Long> result = defaultSessionMetrics();
        try {
            String sql =
                    "SELECT " +
                            "  COUNT(*) AS total, " +
                            "  SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count, " +
                            "  SUM(CASE WHEN current_step NOT LIKE '%_CONFIRMATION_SENT' " +
                            "           AND current_step NOT LIKE '%_TEMPLATE_SENT' THEN 1 ELSE 0 END) AS replied_count, " +
                            "  SUM(CASE WHEN current_step LIKE '%CATALOGUE%' THEN 1 ELSE 0 END) AS catalogue_count, " +
                            "  SUM(CASE WHEN current_step LIKE '%COMPLETED%' THEN 1 ELSE 0 END) AS completed_count " +
                            "FROM bot_sessions " +
                            "WHERE current_step LIKE ? " +
                            "  AND last_activity >= ? AND last_activity < ?";

            jdbcTemplate.query(sql, new Object[]{stepPattern, from, to}, rs -> {
                result.put("total",     rs.getLong("total"));
                result.put("active",    rs.getLong("active_count"));
                result.put("replied",   rs.getLong("replied_count"));
                result.put("catalogue", rs.getLong("catalogue_count"));
                result.put("completed", rs.getLong("completed_count"));
            });
        } catch (Exception e) {
            log.warn("getSessionMetrics failed: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, Long> defaultSessionMetrics() {
        Map<String, Long> m = new HashMap<>();
        m.put("total", 0L); m.put("active", 0L); m.put("replied", 0L);
        m.put("catalogue", 0L); m.put("completed", 0L);
        return m;
    }

    /**
     * All lead KPI metrics in ONE SQL query.
     *
     * @param flow        the flow key (e.g. "bday_t10") for l.flow = ?
     * @param stepPattern the LIKE pattern for l.step_name LIKE ?
     * @return map keys: total, callback, store_visit, new_count, converted
     */
    public Map<String, Long> getLeadMetrics(
            String flow, String stepPattern, LocalDateTime from, LocalDateTime to) {

        Map<String, Long> result = defaultLeadMetrics();
        try {
            String sql =
                    "SELECT " +
                            "  COUNT(*) AS total, " +
                            "  SUM(CASE WHEN lead_type = 'CALLBACK'    THEN 1 ELSE 0 END) AS callback_count, " +
                            "  SUM(CASE WHEN lead_type = 'STORE_VISIT' THEN 1 ELSE 0 END) AS store_visit_count, " +
                            "  SUM(CASE WHEN status = 'NEW'            THEN 1 ELSE 0 END) AS new_count, " +
                            "  SUM(CASE WHEN status = 'CONVERTED'      THEN 1 ELSE 0 END) AS converted_count " +
                            "FROM leads " +
                            "WHERE (flow = ? OR step_name LIKE ?) " +
                            "  AND created_at >= ? AND created_at < ?";

            jdbcTemplate.query(sql, new Object[]{flow, stepPattern, from, to}, rs -> {
                result.put("total",       rs.getLong("total"));
                result.put("callback",    rs.getLong("callback_count"));
                result.put("store_visit", rs.getLong("store_visit_count"));
                result.put("new_count",   rs.getLong("new_count"));
                result.put("converted",   rs.getLong("converted_count"));
            });
        } catch (Exception e) {
            log.warn("getLeadMetrics failed: {}", e.getMessage());
        }
        return result;
    }

    private Map<String, Long> defaultLeadMetrics() {
        Map<String, Long> m = new HashMap<>();
        m.put("total", 0L); m.put("callback", 0L); m.put("store_visit", 0L);
        m.put("new_count", 0L); m.put("converted", 0L);
        return m;
    }

    // ════════════════════════════════════════════════════════════
    // ── LEADS — LIST ───────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** All flows, no date filter — legacy. Capped at MAX_LEAD_ROWS. */
    public List<DashboardResponse.LeadDto> findLeads() {
        try {
            String sql =
                    "SELECT l.id, COALESCE(l.customer_name, c.name, 'WhatsApp User') AS customer_name, " +
                            "       COALESCE(l.phone, c.phone) AS phone, " +
                            "       l.lead_type, l.flow, l.step_name, " +
                            "       l.selected_collection, l.selected_brand, l.selected_style, " +
                            "       l.status, l.notes, l.created_at " +
                            "FROM leads l " +
                            "LEFT JOIN customers c ON c.phone = l.phone " +
                            "WHERE (l.flow IN ('bday_t10','bday_t0','anniv_t10','anniv_t0') " +
                            "    OR l.step_name LIKE 'BIRTHDAY_T10_%' OR l.step_name LIKE 'BIRTHDAY_TDAY_%' " +
                            "    OR l.step_name LIKE 'ANNIVERSARY_T10_%' OR l.step_name LIKE 'ANNIVERSARY_TDAY_%') " +
                            "ORDER BY l.created_at DESC " +
                            "LIMIT " + MAX_LEAD_ROWS;

            return jdbcTemplate.query(sql,
                    (rs, rn) -> { try { return mapLead(rs); } catch (Exception e) { throw new RuntimeException(e); }});
        } catch (Exception e) {
            log.warn("findLeads failed", e);
            return new ArrayList<>();
        }
    }

    /**
     * Leads filtered by flow + date range.
     * Uses index: idx_leads_flow_created(flow, created_at).
     *
     * @param flow        the flow key for l.flow = ?
     * @param stepPattern LIKE pattern for l.step_name LIKE ?
     */
    public List<DashboardResponse.LeadDto> findLeadsByFlowAndDate(
            String flow, String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT l.id, COALESCE(l.customer_name, c.name, 'WhatsApp User') AS customer_name, " +
                            "       COALESCE(l.phone, c.phone) AS phone, " +
                            "       l.lead_type, l.flow, l.step_name, " +
                            "       l.selected_collection, l.selected_brand, l.selected_style, " +
                            "       l.status, l.notes, l.created_at " +
                            "FROM leads l " +
                            "LEFT JOIN customers c ON c.phone = l.phone " +
                            "WHERE (l.flow = ? OR l.step_name LIKE ?) " +
                            "  AND l.created_at >= ? AND l.created_at < ? " +
                            "ORDER BY l.created_at DESC " +
                            "LIMIT " + MAX_LEAD_ROWS;

            return jdbcTemplate.query(sql, new Object[]{flow, stepPattern, from, to},
                    (rs, rn) -> { try { return mapLead(rs); } catch (Exception e) { throw new RuntimeException(e); }});
        } catch (Exception e) {
            log.warn("findLeadsByFlowAndDate failed", e);
            return new ArrayList<>();
        }
    }

    /** Backward-compat overload (LocalDate → LocalDateTime). */
    public List<DashboardResponse.LeadDto> findLeadsByFlowAndDate(
            String flow, String stepPattern, LocalDate from, LocalDate to) {
        return findLeadsByFlowAndDate(flow, stepPattern,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    // ════════════════════════════════════════════════════════════
    // ── COLLECTION SPLIT — aggregate, no list fetch ────────────
    // ════════════════════════════════════════════════════════════

    /**
     * mens/womens/couples counts directly in SQL — faster than iterating a list in Java.
     * @param stepPattern SQL LIKE pattern for current_step
     */
    public DashboardResponse.CollectionSplitDto getCollectionSplit(
            String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT " +
                            "  SUM(CASE WHEN UPPER(COALESCE(selected_collection,'')) LIKE '%WOMEN%' " +
                            "            OR UPPER(COALESCE(selected_collection,'')) LIKE '%FEMALE%' THEN 1 ELSE 0 END) AS womens, " +
                            "  SUM(CASE WHEN (UPPER(COALESCE(selected_collection,'')) LIKE '%MEN%' " +
                            "            AND UPPER(COALESCE(selected_collection,'')) NOT LIKE '%WOMEN%') THEN 1 ELSE 0 END) AS mens, " +
                            "  SUM(CASE WHEN UPPER(COALESCE(selected_collection,'')) LIKE '%COUPLES%' THEN 1 ELSE 0 END) AS couples " +
                            "FROM bot_sessions " +
                            "WHERE current_step LIKE ? " +
                            "  AND last_activity >= ? AND last_activity < ?";

            DashboardResponse.CollectionSplitDto dto =
                    jdbcTemplate.queryForObject(sql, new Object[]{stepPattern, from, to},
                            (rs, rn) -> DashboardResponse.CollectionSplitDto.builder()
                                    .mens(rs.getLong("mens"))
                                    .womens(rs.getLong("womens"))
                                    .couples(rs.getLong("couples"))
                                    .build());

            return dto != null ? dto : DashboardResponse.CollectionSplitDto.builder().build();
        } catch (Exception e) {
            log.warn("getCollectionSplit failed: {}", e.getMessage());
            return DashboardResponse.CollectionSplitDto.builder().build();
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── COUNTS / LEFT SIDEBAR ──────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /**
     * Active session counts per flow — used by the left sidebar.
     * Single SUM(CASE WHEN …) query, uses idx_bs_active index.
     */
    public Map<String, Long> findFlowCounts() {
        String sql =
                "SELECT " +
                        "  SUM(CASE WHEN current_step LIKE 'BIRTHDAY_T10_%'     THEN 1 ELSE 0 END) AS bday_t10, " +
                        "  SUM(CASE WHEN current_step LIKE 'BIRTHDAY_TDAY_%'    THEN 1 ELSE 0 END) AS bday_t0, " +
                        "  SUM(CASE WHEN current_step LIKE 'ANNIVERSARY_T10_%'  THEN 1 ELSE 0 END) AS anniv_t10, " +
                        "  SUM(CASE WHEN current_step LIKE 'ANNIVERSARY_TDAY_%' THEN 1 ELSE 0 END) AS anniv_t0, " +
                        "  COUNT(*) AS total " +
                        "FROM bot_sessions WHERE is_active = 1";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rn) -> {
                Map<String, Long> m = new LinkedHashMap<>();
                m.put("bday_t10",  rs.getLong("bday_t10"));
                m.put("bday_t0",   rs.getLong("bday_t0"));
                m.put("anniv_t10", rs.getLong("anniv_t10"));
                m.put("anniv_t0",  rs.getLong("anniv_t0"));
                m.put("total",     rs.getLong("total"));
                return m;
            });
        } catch (Exception e) {
            log.warn("findFlowCounts failed", e);
            return new LinkedHashMap<>();
        }
    }

    /** Total active sessions across all flows. */
    public long countActiveSessions() {
        try {
            Long c = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bot_sessions WHERE is_active = 1", Long.class);
            return c != null ? c : 0L;
        } catch (Exception e) { return 0L; }
    }

    // ════════════════════════════════════════════════════════════
    // ── DELIVERY METRICS ───────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** COUNT of delivered outbound messages in the given flow + date window. */
    /** COUNT of delivered outbound messages in the given flow + date window.
     *  Includes both 'DELIVERED' and 'READ' status — a message must pass
     *  through DELIVERED before it can become READ, so both count as delivered. */
    public long countDeliveredMessages(String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM messages m " +
                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
                            "WHERE bs.current_step LIKE ? " +
                            "  AND m.status IN ('DELIVERED', 'READ') AND m.direction = 'OUTBOUND' " +
                            "  AND m.sent_at >= ? AND m.sent_at < ?";
            Long c = jdbcTemplate.queryForObject(sql, Long.class, stepPattern, from, to);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countDeliveredMessages failed: {}", e.getMessage());
            return 0L;
        }
    }

    /** COUNT of all outbound messages in the given flow + date window. */
    public long countSentMessages(String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM messages m " +
                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
                            "WHERE bs.current_step LIKE ? " +
                            "  AND m.direction = 'OUTBOUND' " +
                            "  AND m.sent_at >= ? AND m.sent_at < ?";
            Long c = jdbcTemplate.queryForObject(sql, Long.class, stepPattern, from, to);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countSentMessages failed: {}", e.getMessage());
            return 0L;
        }
    }



    /**
     * "Completed Flow" — user ne final action le liya:
     *   - CALLBACK_CONFIRMED (Request Callback maanga)
     *   - STORE_VISIT_SENT   (Store Visit maanga)
     *   - COMPLETED          (T-Day flows ka explicit "completed" step)
     *
     * Pehle sirf current_step LIKE '%COMPLETED%' check hota tha,
     * jo SIRF T-Day flows ke liye match karta tha — T-10 flows mein
     * koi step ka naam "COMPLETED" nahi hai, isliye T-10 ka
     * completionRate hamesha 0% aata tha (bug).
     */
    public long countCompletedFlows(String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM bot_sessions " +
                            "WHERE current_step LIKE ? " +
                            "  AND ( " +
                            "       current_step LIKE '%CALLBACK_CONFIRMED%' " +
                            "    OR current_step LIKE '%STORE_VISIT_SENT%' " +
                            "    OR current_step LIKE '%COMPLETED%' " +
                            "  ) " +
                            "  AND last_activity >= ? AND last_activity < ?";
            Long c = jdbcTemplate.queryForObject(sql, Long.class, stepPattern, from, to);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countCompletedFlows failed: {}", e.getMessage());
            return 0L;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── ACTIVITY FEED — returns RawActivityRow (no business logic)
    // ServiceImpl converts RawActivityRow → ActivityEventDto
    // ════════════════════════════════════════════════════════════

    /** Most recent 15 activity events across all flows. */
    public List<RawActivityRow> findRecentActivity() {
        return findRecentActivityByFlow(null);
    }

    /**
     * Most recent 15 activity events for the given flow (or all flows if stepPattern is null).
     * @param stepPattern SQL LIKE pattern, or null for all flows
     */
    public List<RawActivityRow> findRecentActivityByFlow(String stepPattern) {
        String stepFilter;
        Object[] params;

        if (stepPattern != null) {
            stepFilter = "m.phone IN (SELECT DISTINCT bs.phone FROM bot_sessions bs " +
                    "WHERE bs.current_step LIKE ?)";
            params = new Object[]{stepPattern};
        } else {
            stepFilter = "m.phone IN (SELECT DISTINCT bs.phone FROM bot_sessions bs " +
                    "WHERE bs.current_step LIKE 'BIRTHDAY_T10_%' " +
                    "   OR bs.current_step LIKE 'BIRTHDAY_TDAY_%' " +
                    "   OR bs.current_step LIKE 'ANNIVERSARY_T10_%' " +
                    "   OR bs.current_step LIKE 'ANNIVERSARY_TDAY_%')";
            params = new Object[]{};
        }

        String sql =
                "SELECT m.phone, m.direction, m.message_content, m.button_payload, m.sent_at, " +
                        "       c.name AS customer_name " +
                        "FROM messages m LEFT JOIN customers c ON c.phone = m.phone " +
                        "WHERE " + stepFilter + " ORDER BY m.sent_at DESC LIMIT 15";

        try {
            return jdbcTemplate.query(sql, params,
                    (rs, rn) -> { try { return mapRawActivity(rs); } catch (Exception e) { throw new RuntimeException(e); }});
        } catch (Exception e) {
            log.warn("findRecentActivityByFlow failed", e);
            return new ArrayList<>();
        }
    }

    /**
     * Most recent 15 activity events for the given flow + date range.
     * Uses index: idx_msg_phone_sent(phone, sent_at).
     *
     * @param stepPattern SQL LIKE pattern for current_step
     */
    public List<RawActivityRow> findRecentActivityByFlowAndDate(
            String stepPattern, LocalDateTime from, LocalDateTime to) {
        String sql =
                "SELECT m.phone, m.direction, m.message_content, m.button_payload, m.sent_at, " +
                        "       c.name AS customer_name " +
                        "FROM messages m LEFT JOIN customers c ON c.phone = m.phone " +
                        "WHERE m.phone IN (SELECT DISTINCT bs.phone FROM bot_sessions bs " +
                        "                  WHERE bs.current_step LIKE ?) " +
                        "  AND m.sent_at >= ? AND m.sent_at < ? " +
                        "ORDER BY m.sent_at DESC LIMIT 15";

        try {
            return jdbcTemplate.query(sql, new Object[]{stepPattern, from, to},
                    (rs, rn) -> { try { return mapRawActivity(rs); } catch (Exception e) { throw new RuntimeException(e); }});
        } catch (Exception e) {
            log.warn("findRecentActivityByFlowAndDate failed", e);
            return new ArrayList<>();
        }
    }

    /** Backward-compat overload (LocalDate). */
    public List<RawActivityRow> findRecentActivityByFlowAndDate(
            String stepPattern, LocalDate from, LocalDate to) {
        return findRecentActivityByFlowAndDate(stepPattern,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    // ════════════════════════════════════════════════════════════
    // ── HOURLY MESSAGES CHART ──────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Message counts grouped by hour + direction for today (all flows). */
    public Map<Integer, Map<String, Long>> findMessageCountsToday() {
        String sql =
                "SELECT HOUR(sent_at) AS hour_value, direction, COUNT(*) AS total " +
                        "FROM messages " +
                        "WHERE DATE(sent_at) = CURDATE() " +
                        "  AND phone IN (SELECT DISTINCT phone FROM bot_sessions " +
                        "                WHERE current_step LIKE 'BIRTHDAY_T10_%' " +
                        "                   OR current_step LIKE 'BIRTHDAY_TDAY_%' " +
                        "                   OR current_step LIKE 'ANNIVERSARY_T10_%' " +
                        "                   OR current_step LIKE 'ANNIVERSARY_TDAY_%') " +
                        "GROUP BY HOUR(sent_at), direction ORDER BY hour_value";

        Map<Integer, Map<String, Long>> result = new HashMap<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                int    h = rs.getInt("hour_value");
                String d = safe(rs, "direction").toUpperCase();
                long   t = rs.getLong("total");
                result.putIfAbsent(h, new HashMap<>());
                result.get(h).put(d, t);
            });
        } catch (Exception e) {
            log.warn("findMessageCountsToday failed: {}", e.getMessage());
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════
    // ── PRIVATE — ROW MAPPERS ──────────────────────────────────
    // Pure column → field. Zero business logic.
    // ════════════════════════════════════════════════════════════

    /**
     * Maps bot_sessions + customers columns directly to SessionDto.
     * flow, selectedStyle, and collection normalization are NOT done here —
     * ServiceImpl.enrichSession() handles those after the query.
     */
    private DashboardResponse.SessionDto mapSession(ResultSet rs) throws Exception {
        return DashboardResponse.SessionDto.builder()
                .id(rs.getLong("id"))
                .customerName(safe(rs, "customer_name"))
                .phone(safe(rs, "phone"))
                .currentStep(safe(rs, "current_step"))
                .rawStep(safe(rs, "current_step"))
                .selectedCollection(safe(rs, "selected_collection"))
                .selectedBrand(safe(rs, "selected_brand"))
                .isActive(rs.getBoolean("is_active"))
                .lastActivity(toIso(rs, "last_activity"))
                // flow + selectedStyle intentionally omitted — enriched by ServiceImpl
                .build();
    }

    /**
     * Maps leads + customers columns directly to LeadDto.
     * Flow detection when blank and collection normalization are NOT done here —
     * ServiceImpl.enrichLead() handles those after the query.
     */
    private DashboardResponse.LeadDto mapLead(ResultSet rs) throws Exception {
        return DashboardResponse.LeadDto.builder()
                .id(rs.getLong("id"))
                .customerName(safe(rs, "customer_name"))
                .phone(safe(rs, "phone"))
                .leadType(safe(rs, "lead_type"))
                .flow(safe(rs, "flow"))
                .stepName(safe(rs, "step_name"))
                .selectedCollection(safe(rs, "selected_collection"))
                .selectedBrand(safe(rs, "selected_brand"))
                .status(safe(rs, "status"))
                .notes(safe(rs, "notes"))
                .createdAt(rs.getTimestamp("created_at") == null
                        ? null : rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    /**
     * Maps messages + customers columns to a plain RawActivityRow holder.
     * icon, text, bg, color are computed by ServiceImpl.enrichActivity().
     */
    private RawActivityRow mapRawActivity(ResultSet rs) throws Exception {
        return new RawActivityRow(
                safe(rs, "phone"),
                safe(rs, "customer_name"),
                safe(rs, "direction"),
                safe(rs, "message_content"),
                safe(rs, "button_payload"),
                toIso(rs, "sent_at")
        );
    }

    // ════════════════════════════════════════════════════════════
    // ── PRIVATE — DYNAMIC WHERE CLAUSE BUILDER ─────────────────
    // Builds SQL WHERE string + params list for session pagination.
    // This is query construction, not business logic.
    // ════════════════════════════════════════════════════════════

    private WhereClause buildSessionWhere(
            String stepPattern, LocalDateTime from, LocalDateTime to,
            String collection, String brand, String step, String search) {

        List<Object> params = new ArrayList<>();
        params.add(stepPattern);
        params.add(from);
        params.add(to);

        StringBuilder where = new StringBuilder(
                "WHERE bs.current_step LIKE ? " +
                        "  AND bs.last_activity >= ? AND bs.last_activity < ?");

        if (collection != null && !collection.isBlank()) {
            where.append(" AND bs.selected_collection = ?");
            params.add(collection.trim().toUpperCase());
        }
        if (brand != null && !brand.isBlank()) {
            where.append(" AND bs.selected_brand = ?");
            params.add(brand.trim());
        }
        // Exact step match overrides the LIKE pattern from the outer filter
        if (step != null && !step.isBlank()) {
            where.append(" AND bs.current_step = ?");
            params.add(step.trim());
        }
        if (search != null && !search.isBlank()) {
            String s = "%" + search.trim() + "%";
            where.append(" AND (c.name LIKE ? OR bs.phone LIKE ?)");
            params.add(s);
            params.add(s);
        }

        return new WhereClause(where.toString(), params);
    }

    /** Simple value object carrying a SQL WHERE fragment and its bound parameters. */
    private record WhereClause(String sql, List<Object> params) {}

    // ════════════════════════════════════════════════════════════
    // ── PRIVATE — JDBC UTILITIES ────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Null-safe column read — returns empty string instead of null. */
    private String safe(ResultSet rs, String column) {
        try {
            String v = rs.getString(column);
            return v == null ? "" : v;
        } catch (Exception e) { return ""; }
    }

    /** Formats a TIMESTAMP column as an ISO datetime string. */
    private String toIso(ResultSet rs, String column) {
        try { return ISO.format(rs.getTimestamp(column).toLocalDateTime()); }
        catch (Exception e) { return ISO.format(LocalDateTime.now()); }
    }




    // ════════════════════════════════════════════════════════════
    // ── MESSAGES — OPEN / CLICK metrics (Karix webhook data) ────
    // ════════════════════════════════════════════════════════════

    /**
     * "Opens" — Karix webhook se status='READ' (code=102, "Read by the user").
     * Email marketing ka "Open" WhatsApp mein yahi blue-tick READ event hai.
     */
    public long countReadMessages(String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM messages m " +
                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
                            "WHERE bs.current_step LIKE ? " +
                            "  AND m.status = 'READ' AND m.direction = 'OUTBOUND' " +
                            "  AND m.sent_at >= ? AND m.sent_at < ?";
            Long c = jdbcTemplate.queryForObject(sql, Long.class, stepPattern, from, to);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countReadMessages failed: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * "Clicks" — INBOUND message jisme button_payload set hai (user ne koi
     * quick-reply button dabaya, jaise "Request Callback" / "Visit Store" / etc).
     */
    public long countButtonClicks(String stepPattern, LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM messages m " +
                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
                            "WHERE bs.current_step LIKE ? " +
                            "  AND m.direction = 'INBOUND' " +
                            "  AND m.button_payload IS NOT NULL AND m.button_payload != '' " +
                            "  AND m.sent_at >= ? AND m.sent_at < ?";
            Long c = jdbcTemplate.queryForObject(sql, Long.class, stepPattern, from, to);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countButtonClicks failed: {}", e.getMessage());
            return 0L;
        }
    }



    // ════════════════════════════════════════════════════════════
    // ── DOB / DATE REVISION REQUESTS ────────────────────────────
    // Users jinhone "No, that's not right" dabaya hai (Birthday
    // month confirm) ya Anniversary date confirm — aur abhi tak
    // apni sahi DOB/Date reply nahi ki hai (session "PENDING"
    // step pe ruka hua hai).
    // ════════════════════════════════════════════════════════════

    /**
     * Saare PENDING DOB/Date-revision requests.
     * Customer table se JOIN karke Name nikalta hai.
     */
    /**
     * Saare PENDING DOB/Date-revision requests.
     * Customer table se JOIN karke Name nikalta hai.
     */
    public List<Map<String, Object>> findDobRevisionRequests() {
        String sql =
                "SELECT " +
                        "    bs.id, " +
                        "    bs.phone, " +
                        "    COALESCE(c.name, 'WhatsApp User') AS customer_name, " +
                        "    bs.current_step, " +
                        "    bs.session_start, " +
                        "    bs.last_activity " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.id = bs.customer_id OR c.phone = bs.phone " +
                        "WHERE bs.current_step IN ( " +
                        "    'BIRTHDAY_T10_DOB_CORRECTION_PENDING', " +
                        "    'ANNIVERSARY_T10_DATE_CORRECTION_PENDING' " +
                        ") " +
                        "ORDER BY bs.last_activity DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            String step = safe(rs, "current_step");

            row.put("id",           rs.getLong("id"));
            row.put("customerName", safe(rs, "customer_name"));
            row.put("phone",        safe(rs, "phone"));
            row.put("flow",         step.startsWith("BIRTHDAY") ? "Birthday" : "Anniversary");
            row.put("revisionType", step.contains("DOB") ? "Date of Birth" : "Anniversary Date");
            row.put("requestedAt",  toIso(rs, "session_start"));
            row.put("lastActivity", toIso(rs, "last_activity"));
            return row;
        });
    }

    /** Sirf count — box ke liye. */
    public long countDobRevisionRequests() {
        try {
            String sql =
                    "SELECT COUNT(*) FROM bot_sessions " +
                            "WHERE current_step IN ( " +
                            "    'BIRTHDAY_T10_DOB_CORRECTION_PENDING', " +
                            "    'ANNIVERSARY_T10_DATE_CORRECTION_PENDING' " +
                            ")";
            Long c = jdbcTemplate.queryForObject(sql, Long.class);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countDobRevisionRequests failed: {}", e.getMessage());
            return 0L;
        }
    }





    /**
     * COUNT per current_step — for Bot Flow Funnel chart.
     * No row cap — real counts directly from DB.
     * Returns Map<stepName, count>
     */
    public Map<String, Long> getStepCounts(String stepPattern, LocalDateTime from, LocalDateTime to) {
        Map<String, Long> result = new LinkedHashMap<>();
        try {
            String sql =
                    "SELECT current_step, COUNT(*) AS cnt " +
                            "FROM bot_sessions " +
                            "WHERE current_step LIKE ? " +
                            "  AND last_activity >= ? AND last_activity < ? " +
                            "GROUP BY current_step";

            jdbcTemplate.query(sql, new Object[]{stepPattern, from, to}, rs -> {
                result.put(rs.getString("current_step"), rs.getLong("cnt"));
            });
        } catch (Exception e) {
            log.warn("getStepCounts failed: {}", e.getMessage());
        }
        return result;
    }





}