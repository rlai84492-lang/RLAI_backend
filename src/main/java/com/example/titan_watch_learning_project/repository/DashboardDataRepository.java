
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


@Slf4j
@Repository
@RequiredArgsConstructor
public class DashboardDataRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ════════════════════════════════════════════════════════════
    // ── SESSIONS ───────────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Saare flows ke sessions — date filter nahi (legacy/fallback). */
    public List<DashboardResponse.SessionDto> findSessions() {
        String sql =
                "SELECT " +
                        "    bs.id, bs.phone, bs.current_step, bs.is_active, " +
                        "    bs.last_activity, bs.selected_collection, bs.selected_brand, " +
                        "    c.name AS customer_name " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.id = bs.customer_id OR c.phone = bs.phone " +
                        "WHERE ( " +
                        "    bs.current_step LIKE 'BIRTHDAY_T10_%' " +
                        "    OR bs.current_step LIKE 'BIRTHDAY_TDAY_%' " +
                        "    OR bs.current_step LIKE 'ANNIVERSARY_T10_%' " +
                        "    OR bs.current_step LIKE 'ANNIVERSARY_TDAY_%' " +
                        ") " +
                        "ORDER BY bs.last_activity DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            try { return mapSession(rs); } catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    /** Flow-wise sessions — date filter nahi (legacy). */
    public List<DashboardResponse.SessionDto> findSessionsByFlow(String flow) {
        String likePattern = flowToPattern(flow);
        String sql =
                "SELECT " +
                        "    bs.id, bs.phone, bs.current_step, bs.is_active, " +
                        "    bs.last_activity, bs.selected_collection, bs.selected_brand, " +
                        "    c.name AS customer_name " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.id = bs.customer_id OR c.phone = bs.phone " +
                        "WHERE bs.current_step LIKE ? " +
                        "ORDER BY bs.last_activity DESC";

        return jdbcTemplate.query(sql, new Object[]{likePattern},
                (rs, rowNum) -> { try { return mapSession(rs); } catch (Exception e) { throw new RuntimeException(e); } });
    }

    /** Flow + date-range wise sessions — Dashboard ke liye main query. */
    public List<DashboardResponse.SessionDto> findSessionsByFlowAndDate(String flow, LocalDate from, LocalDate to) {
        String likePattern = flowToPattern(flow);
        String sql =
                "SELECT " +
                        "    bs.id, bs.phone, bs.current_step, bs.is_active, " +
                        "    bs.last_activity, bs.selected_collection, bs.selected_brand, " +
                        "    c.name AS customer_name " +
                        "FROM bot_sessions bs " +
                        "LEFT JOIN customers c ON c.id = bs.customer_id OR c.phone = bs.phone " +
                        "WHERE bs.current_step LIKE ? " +
                        "AND DATE(bs.last_activity) BETWEEN ? AND ? " +
                        "ORDER BY bs.last_activity DESC";

        return jdbcTemplate.query(sql, new Object[]{likePattern, from, to},
                (rs, rowNum) -> { try { return mapSession(rs); } catch (Exception e) { throw new RuntimeException(e); } });
    }

    // ════════════════════════════════════════════════════════════
    // ── LEADS ──────────────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Saare flows ke leads — date filter nahi (legacy). */
    public List<DashboardResponse.LeadDto> findLeads() {
        try {
            String sql =
                    "SELECT " +
                            "    l.id, " +
                            "    COALESCE(l.customer_name, c.name, 'WhatsApp User') AS customer_name, " +
                            "    COALESCE(l.phone, c.phone) AS phone, " +
                            "    l.lead_type, l.flow, l.step_name, " +
                            "    l.selected_collection, l.selected_brand, l.selected_style, " +
                            "    l.status, l.notes, l.created_at " +
                            "FROM leads l " +
                            "LEFT JOIN customers c ON c.id = l.customer_id OR c.phone = l.phone " +
                            "WHERE ( " +
                            "    l.flow IN ('bday_t10', 'bday_t0', 'anniv_t10', 'anniv_t0') " +
                            "    OR l.step_name LIKE 'BIRTHDAY_T10_%' " +
                            "    OR l.step_name LIKE 'BIRTHDAY_TDAY_%' " +
                            "    OR l.step_name LIKE 'ANNIVERSARY_T10_%' " +
                            "    OR l.step_name LIKE 'ANNIVERSARY_TDAY_%' " +
                            ") " +
                            "ORDER BY l.created_at DESC";

            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> { try { return mapLead(rs); } catch (Exception e) { throw new RuntimeException(e); } });
        } catch (Exception e) {
            log.warn("Could not read leads table. Returning empty.", e);
            return new ArrayList<>();
        }
    }

    /** Flow-wise leads — date filter nahi (legacy). */
    public List<DashboardResponse.LeadDto> findLeadsByFlow(String flow) {
        try {
            String likePattern = flowToPattern(flow);
            String sql =
                    "SELECT " +
                            "    l.id, " +
                            "    COALESCE(l.customer_name, c.name, 'WhatsApp User') AS customer_name, " +
                            "    COALESCE(l.phone, c.phone) AS phone, " +
                            "    l.lead_type, l.flow, l.step_name, " +
                            "    l.selected_collection, l.selected_brand, l.selected_style, " +
                            "    l.status, l.notes, l.created_at " +
                            "FROM leads l " +
                            "LEFT JOIN customers c ON c.id = l.customer_id OR c.phone = l.phone " +
                            "WHERE l.flow = ? OR l.step_name LIKE ? " +
                            "ORDER BY l.created_at DESC";

            return jdbcTemplate.query(sql, new Object[]{flow, likePattern},
                    (rs, rowNum) -> { try { return mapLead(rs); } catch (Exception e) { throw new RuntimeException(e); } });
        } catch (Exception e) {
            log.warn("Could not read leads by flow. Returning empty.", e);
            return new ArrayList<>();
        }
    }

    /** Flow + date-range wise leads — Dashboard ke liye main query. */
//    public List<DashboardResponse.LeadDto> findLeadsByFlowAndDate(String flow, LocalDate from, LocalDate to) {
//        try {
//            String likePattern = flowToPattern(flow);
//            String sql =
//                    "SELECT " +
//                            "    l.id, " +
//                            "    COALESCE(l.customer_name, c.name, 'WhatsApp User') AS customer_name, " +
//                            "    COALESCE(l.phone, c.phone) AS phone, " +
//                            "    l.lead_type, l.flow, l.step_name, " +
//                            "    l.selected_collection, l.selected_brand, l.selected_style, " +
//                            "    l.status, l.notes, l.created_at " +
//                            "FROM leads l " +
//                            "LEFT JOIN customers c ON c.id = l.customer_id OR c.phone = l.phone " +
//                            "WHERE (l.flow = ? OR l.step_name LIKE ?) " +
//                            "AND DATE(l.created_at) BETWEEN ? AND ? " +
//                            "ORDER BY l.created_at DESC";
//
//            return jdbcTemplate.query(sql, new Object[]{flow, likePattern, from, to},
//                    (rs, rowNum) -> { try { return mapLead(rs); } catch (Exception e) { throw new RuntimeException(e); } });
//        } catch (Exception e) {
//            log.warn("Could not read leads by flow+date. Returning empty.", e);
//            return new ArrayList<>();
//        }
//    }





    public List<DashboardResponse.LeadDto> findLeadsByFlowAndDate(String flow, LocalDate from, LocalDate to) {
        try {
            String likePattern = flowToPattern(flow);
            String sql =
                    "SELECT " +
                            "    l.id, " +
                            "    COALESCE(l.customer_name, c.name, 'WhatsApp User') AS customer_name, " +
                            "    COALESCE(l.phone, c.phone) AS phone, " +
                            "    l.lead_type, l.flow, l.step_name, " +
                            "    l.selected_collection, l.selected_brand, l.selected_style, " +
                            "    l.status, l.notes, l.created_at " +
                            "FROM leads l " +
                            "LEFT JOIN customers c ON c.id = l.customer_id OR c.phone = l.phone " +
                            "WHERE (l.flow = ? OR l.step_name LIKE ?) " +
                            "AND l.created_at >= ? AND l.created_at < ? " +  // ← CHANGED
                            "ORDER BY l.created_at DESC";

            // to+1 day so that the full "to" day is included
            return jdbcTemplate.query(sql,
                    new Object[]{flow, likePattern, from.atStartOfDay(), to.plusDays(1).atStartOfDay()},
                    (rs, rowNum) -> { try { return mapLead(rs); } catch (Exception e) { throw new RuntimeException(e); } });
        } catch (Exception e) {
            log.warn("Could not read leads by flow+date. Returning empty.", e);
            return new ArrayList<>();
        }
    }


    // ════════════════════════════════════════════════════════════
    // ── COUNTS / METRICS (Repository ka kaam — Service ko sirf result chahiye) ──
    // ════════════════════════════════════════════════════════════

    /** Left menu ke 4 flow ke counts (active sessions). */
    public Map<String, Long> findFlowCounts() {
        String sql =
                "SELECT " +
                        "    SUM(CASE WHEN current_step LIKE 'BIRTHDAY_T10_%'     THEN 1 ELSE 0 END) AS bday_t10, " +
                        "    SUM(CASE WHEN current_step LIKE 'BIRTHDAY_TDAY_%'    THEN 1 ELSE 0 END) AS bday_t0, " +
                        "    SUM(CASE WHEN current_step LIKE 'ANNIVERSARY_T10_%'  THEN 1 ELSE 0 END) AS anniv_t10, " +
                        "    SUM(CASE WHEN current_step LIKE 'ANNIVERSARY_TDAY_%' THEN 1 ELSE 0 END) AS anniv_t0, " +
                        "    COUNT(*) AS total " +
                        "FROM bot_sessions " +
                        "WHERE is_active = 1";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, Long> counts = new LinkedHashMap<>();
                counts.put("bday_t10",  rs.getLong("bday_t10"));
                counts.put("bday_t0",   rs.getLong("bday_t0"));
                counts.put("anniv_t10", rs.getLong("anniv_t10"));
                counts.put("anniv_t0",  rs.getLong("anniv_t0"));
                counts.put("total",     rs.getLong("total"));
                return counts;
            });
        } catch (Exception e) {
            log.warn("Could not get flow counts.", e);
            return new LinkedHashMap<>();
        }
    }

    /** Active sessions ka total count. */
    public long countActiveSessions() {
        try {
            Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM bot_sessions WHERE is_active = 1",
                    Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Delivery Rate ke liye — flow + date range ke OUTBOUND messages
     * mein se kitne DELIVERED hue.
     */
    public long countDeliveredMessages(String flow, LocalDate from, LocalDate to) {
        try {
            String pattern = flowToPattern(flow);
            String sql =
                    "SELECT COUNT(*) FROM messages m " +
                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
                            "WHERE bs.current_step LIKE ? " +
                            "AND m.status = 'DELIVERED' " +
                            "AND m.direction = 'OUTBOUND' " +
                            "AND DATE(m.sent_at) BETWEEN ? AND ?";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, pattern, from, to);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("countDeliveredMessages failed: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Delivery Rate ke denominator ke liye — flow + date range ke
     * kitne OUTBOUND messages bheje gaye (status kuch bhi ho).
     */
    public long countSentMessages(String flow, LocalDate from, LocalDate to) {
        try {
            String pattern = flowToPattern(flow);
            String sql =
                    "SELECT COUNT(*) FROM messages m " +
                            "JOIN bot_sessions bs ON bs.phone = m.phone " +
                            "WHERE bs.current_step LIKE ? " +
                            "AND m.direction = 'OUTBOUND' " +
                            "AND DATE(m.sent_at) BETWEEN ? AND ?";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, pattern, from, to);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("countSentMessages failed: {}", e.getMessage());
            return 0L;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── ACTIVITY FEED ──────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Saare flows ki activity — date filter nahi (legacy). */
    public List<DashboardResponse.ActivityEventDto> findRecentActivity() {
        return findRecentActivityByFlow(null);
    }

    /** Flow-wise activity — date filter nahi (legacy). */
    public List<DashboardResponse.ActivityEventDto> findRecentActivityByFlow(String flow) {
        String stepFilter;
        Object[] params;

        if (flow != null) {
            stepFilter =
                    "m.phone IN (" +
                            "    SELECT DISTINCT bs.phone FROM bot_sessions bs " +
                            "    WHERE bs.current_step LIKE ?" +
                            ")";
            params = new Object[]{flowToPattern(flow)};
        } else {
            stepFilter =
                    "m.phone IN (" +
                            "    SELECT DISTINCT bs.phone FROM bot_sessions bs " +
                            "    WHERE bs.current_step LIKE 'BIRTHDAY_T10_%' " +
                            "       OR bs.current_step LIKE 'BIRTHDAY_TDAY_%' " +
                            "       OR bs.current_step LIKE 'ANNIVERSARY_T10_%' " +
                            "       OR bs.current_step LIKE 'ANNIVERSARY_TDAY_%'" +
                            ")";
            params = new Object[]{};
        }

        String sql =
                "SELECT " +
                        "    m.phone, m.direction, m.message_content, " +
                        "    m.button_payload, m.sent_at, " +
                        "    c.name AS customer_name " +
                        "FROM messages m " +
                        "LEFT JOIN customers c ON c.phone = m.phone " +
                        "WHERE " + stepFilter + " " +
                        "ORDER BY m.sent_at DESC " +
                        "LIMIT 15";

        try {
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                try {
                    return mapActivityEvent(rs);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("Could not read activity.", e);
            return new ArrayList<>();
        }
    }

    /** Flow + date-range wise activity — Dashboard ke liye main query. */
    public List<DashboardResponse.ActivityEventDto> findRecentActivityByFlowAndDate(String flow, LocalDate from, LocalDate to) {
        String pattern = flowToPattern(flow);
        String sql =
                "SELECT " +
                        "    m.phone, m.direction, m.message_content, " +
                        "    m.button_payload, m.sent_at, " +
                        "    c.name AS customer_name " +
                        "FROM messages m " +
                        "LEFT JOIN customers c ON c.phone = m.phone " +
                        "WHERE m.phone IN ( " +
                        "    SELECT DISTINCT bs.phone FROM bot_sessions bs " +
                        "    WHERE bs.current_step LIKE ? " +
                        ") " +
                        "AND DATE(m.sent_at) BETWEEN ? AND ? " +
                        "ORDER BY m.sent_at DESC " +
                        "LIMIT 15";

        try {
            return jdbcTemplate.query(sql, new Object[]{pattern, from, to},
                    (rs, rowNum) -> {
                        try {
                            return mapActivityEvent(rs);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            log.warn("Could not read activity by date.", e);
            return new ArrayList<>();
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── HOURLY MESSAGES (chart) ────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /** Aaj ke har ghante ka inbound/outbound message count. */
    public Map<Integer, Map<String, Long>> findMessageCountsToday() {
        String sql =
                "SELECT HOUR(sent_at) AS hour_value, direction, COUNT(*) AS total " +
                        "FROM messages " +
                        "WHERE DATE(sent_at) = CURDATE() " +
                        "AND phone IN ( " +
                        "    SELECT DISTINCT phone FROM bot_sessions " +
                        "    WHERE current_step LIKE 'BIRTHDAY_T10_%' " +
                        "       OR current_step LIKE 'BIRTHDAY_TDAY_%' " +
                        "       OR current_step LIKE 'ANNIVERSARY_T10_%' " +
                        "       OR current_step LIKE 'ANNIVERSARY_TDAY_%' " +
                        ") " +
                        "GROUP BY HOUR(sent_at), direction " +
                        "ORDER BY hour_value";

        Map<Integer, Map<String, Long>> result = new HashMap<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                int hour    = rs.getInt("hour_value");
                String dir  = safe(rs, "direction").toUpperCase();
                long total  = rs.getLong("total");
                result.putIfAbsent(hour, new HashMap<>());
                result.get(hour).put(dir, total);
            });
        } catch (Exception e) {
            log.warn("Could not read hourly counts.", e);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════
    // ── PRIVATE HELPERS — ResultSet → DTO mapping ──────────────
    // ════════════════════════════════════════════════════════════

    /** Flow key (frontend se aane wala) → DB ke current_step LIKE pattern. */
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

    private DashboardResponse.SessionDto mapSession(ResultSet rs) throws Exception {
        String rawStep    = safe(rs, "current_step");
        String collection = normalizeCollection(safe(rs, "selected_collection"));
        String brand      = safe(rs, "selected_brand");
        if (collection == null || collection.isBlank()) {
            collection = extractCollection(rawStep);
        }
        return DashboardResponse.SessionDto.builder()
                .id(rs.getLong("id"))
                .customerName(defaultIfBlank(safe(rs, "customer_name"), "WhatsApp User"))
                .phone(safe(rs, "phone"))
                .currentStep(rawStep)
                .rawStep(rawStep)
                .flow(detectFlow(rawStep))
                .selectedCollection(collection)
                .selectedBrand(defaultIfBlank(brand, null))
                .selectedStyle(extractStyle(rawStep))
                .isActive(rs.getBoolean("is_active"))
                .lastActivity(toIso(rs, "last_activity"))
                .build();
    }

    private DashboardResponse.LeadDto mapLead(ResultSet rs) throws Exception {
        String flow     = safe(rs, "flow");
        String stepName = safe(rs, "step_name");
        String resolvedFlow = flow.isBlank() ? detectFlow(stepName) : flow;
        return DashboardResponse.LeadDto.builder()
                .id(rs.getLong("id"))
                .customerName(defaultIfBlank(safe(rs, "customer_name"), "WhatsApp User"))
                .phone(safe(rs, "phone"))
                .leadType(defaultIfBlank(safe(rs, "lead_type"), "CALLBACK"))
                .flow(resolvedFlow)
                .stepName(stepName)
                .selectedCollection(normalizeCollection(safe(rs, "selected_collection")))
                .selectedBrand(safe(rs, "selected_brand"))
                .status(defaultIfBlank(safe(rs, "status"), "NEW"))
                .notes(safe(rs, "notes"))
                .createdAt(rs.getTimestamp("created_at") == null
                        ? null : rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    private DashboardResponse.ActivityEventDto mapActivityEvent(ResultSet rs) throws Exception {
        String name      = defaultIfBlank(safe(rs, "customer_name"), "WhatsApp User");
        String direction = safe(rs, "direction");
        String payload   = safe(rs, "button_payload");
        String content   = safe(rs, "message_content");
        return DashboardResponse.ActivityEventDto.builder()
                .icon(activityIcon(payload, direction))
                .text(buildActivityText(name, direction, payload, content))
                .time(toIso(rs, "sent_at"))
                .bg(activityBg(payload, direction))
                .color(activityColor(payload, direction))
                .build();
    }

    /** Raw step (DB se) → konsa flow (bday_t10 / bday_t0 / anniv_t10 / anniv_t0). */
    private String detectFlow(String rawStep) {
        if (rawStep == null || rawStep.isBlank()) return "bday_t10";
        String s = rawStep.toUpperCase();
        if (s.startsWith("BIRTHDAY_T10_"))     return "bday_t10";
        if (s.startsWith("BIRTHDAY_TDAY_"))    return "bday_t0";
        if (s.startsWith("ANNIVERSARY_T10_"))  return "anniv_t10";
        if (s.startsWith("ANNIVERSARY_TDAY_")) return "anniv_t0";
        return "bday_t10";
    }

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
        if ("OUTBOUND".equalsIgnoreCase(direction)) return "Bot replied to " + name;
        return defaultIfBlank(content, name + " had activity");
    }

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

    private String activityBg(String payload, String direction) {
        String p = payload == null ? "" : payload.toUpperCase();
        if (p.contains("REQUEST_CALLBACK")) return "#E1F5EE";
        if (p.contains("PRICE_"))           return "#FEF3CD";
        if (p.contains("STYLE_"))           return "#EEEDFE";
        if ("OUTBOUND".equalsIgnoreCase(direction)) return "#FEF0EB";
        return "#EBF4FD";
    }

    private String activityColor(String payload, String direction) {
        String p = payload == null ? "" : payload.toUpperCase();
        if (p.contains("REQUEST_CALLBACK")) return "#1D9E75";
        if (p.contains("PRICE_"))           return "#BA7517";
        if (p.contains("STYLE_"))           return "#7F77DD";
        if ("OUTBOUND".equalsIgnoreCase(direction)) return "#E85A2B";
        return "#378ADD";
    }

    private String extractCollection(String rawStep) {
        if (rawStep == null) return null;
        String s = rawStep.toUpperCase();
        if (s.contains("FEMALE") || s.contains("WOMEN")) return "WOMENS";
        if (s.contains("MALE")   || s.contains("MEN"))   return "MENS";
        if (s.contains("COUPLES"))                        return "COUPLES";
        return null;
    }

    private String extractStyle(String rawStep) {
        if (rawStep == null) return null;
        String s = rawStep.toUpperCase();
        if (s.contains("STYLE_MINIMAL_CHIC"))       return "MINIMAL_CHIC";
        if (s.contains("STYLE_BOLD_EDGY"))          return "BOLD_EDGY";
        if (s.contains("STYLE_LUXE_CLASSY"))        return "LUXE_CLASSY";
        if (s.contains("STYLE_SPORTY_ADVENTUROUS")) return "SPORTY_ADVENTUROUS";
        return null;
    }

    private String normalizeCollection(String value) {
        if (value == null || value.isBlank()) return null;
        String s = value.toUpperCase();
        if (s.contains("FEMALE") || s.contains("WOMEN")) return "WOMENS";
        if (s.contains("MALE")   || s.contains("MEN"))   return "MENS";
        if (s.contains("COUPLES"))                        return "COUPLES";
        return s;
    }

    private String safe(ResultSet rs, String column) {
        try {
            String val = rs.getString(column);
            return val == null ? "" : val;
        } catch (Exception e) {
            return "";
        }
    }

    private String toIso(ResultSet rs, String column) {
        try {
            return ISO.format(rs.getTimestamp(column).toLocalDateTime());
        } catch (Exception e) {
            return ISO.format(LocalDateTime.now());
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}