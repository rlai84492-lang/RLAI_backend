package com.example.titan_watch_learning_project.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DobRevisionDataRepository — ONLY native SQL queries.
 * Koi business logic yahan nahi — date-range resolution,
 * step-name knowledge, response shaping sab Service layer mein hai.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DobRevisionDataRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ════════════════════════════════════════════════════════════
    // ── PENDING — bot_sessions abhi PENDING step pe ─────────────
    // ════════════════════════════════════════════════════════════

    /**
     * Sessions jinka current_step diye gaye list mein se koi ek hai,
     * aur last_activity diye gaye date-range ke andar hai.
     */
    public List<Map<String, Object>> findPendingByStepsAndDate(
            List<String> steps, LocalDateTime from, LocalDateTime to
    ) {
        String placeholders = String.join(",", steps.stream().map(s -> "?").toList());

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
                        "WHERE bs.current_step IN (" + placeholders + ") " +
                        "AND bs.last_activity >= ? AND bs.last_activity < ? " +
                        "ORDER BY bs.last_activity DESC";

        List<Object> params = new ArrayList<>(steps);
        params.add(from);
        params.add(to);

        try {
            return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
                try {
                    return mapPendingRow(rs);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("findPendingByStepsAndDate failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Sirf COUNT — box ke liye, rows fetch nahi karta. */
    public long countPendingByStepsAndDate(
            List<String> steps, LocalDateTime from, LocalDateTime to
    ) {
        String placeholders = String.join(",", steps.stream().map(s -> "?").toList());

        String sql =
                "SELECT COUNT(*) FROM bot_sessions bs " +
                        "WHERE bs.current_step IN (" + placeholders + ") " +
                        "AND bs.last_activity >= ? AND bs.last_activity < ?";

        List<Object> params = new ArrayList<>(steps);
        params.add(from);
        params.add(to);

        try {
            Long c = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countPendingByStepsAndDate failed: {}", e.getMessage());
            return 0L;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── FILLED — customers table mein date already set hai ──────
    // ════════════════════════════════════════════════════════════

    /**
     * Customers jinka date_of_birth ya anniversary_date already
     * set hai (diye gaye date-range ke andar created/updated).
     */
    public List<Map<String, Object>> findFilledCustomersByDate(LocalDateTime from, LocalDateTime to) {
        String sql =
                "SELECT " +
                        "    c.id, " +
                        "    c.name, " +
                        "    c.phone, " +
                        "    c.date_of_birth, " +
                        "    c.anniversary_date, " +
                        "    c.created_at " +
                        "FROM customers c " +
                        "WHERE (c.date_of_birth IS NOT NULL OR c.anniversary_date IS NOT NULL) " +
                        "AND c.created_at >= ? AND c.created_at < ? " +
                        "ORDER BY c.created_at DESC";

        try {
            return jdbcTemplate.query(sql, new Object[]{from, to}, (rs, rowNum) -> {
                try {
                    return mapFilledRow(rs);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.warn("findFilledCustomersByDate failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Sirf COUNT — box ke liye. */
    public long countFilledCustomersByDate(LocalDateTime from, LocalDateTime to) {
        try {
            String sql =
                    "SELECT COUNT(*) FROM customers c " +
                            "WHERE (c.date_of_birth IS NOT NULL OR c.anniversary_date IS NOT NULL) " +
                            "AND c.created_at >= ? AND c.created_at < ?";
            Long c = jdbcTemplate.queryForObject(sql, Long.class, from, to);
            return c != null ? c : 0L;
        } catch (Exception e) {
            log.warn("countFilledCustomersByDate failed: {}", e.getMessage());
            return 0L;
        }
    }

    // ════════════════════════════════════════════════════════════
    // ── ROW MAPPERS — pure column → field ────────────────────────
    // ════════════════════════════════════════════════════════════

    private Map<String, Object> mapPendingRow(ResultSet rs) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id",           rs.getLong("id"));
        row.put("customerName", safe(rs, "customer_name"));
        row.put("phone",        safe(rs, "phone"));
        row.put("currentStep",  safe(rs, "current_step"));
        row.put("requestedAt",  toIso(rs, "session_start"));
        row.put("lastActivity", toIso(rs, "last_activity"));
        return row;
    }

    private Map<String, Object> mapFilledRow(ResultSet rs) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id",              rs.getLong("id"));
        row.put("customerName",    safe(rs, "name"));
        row.put("phone",           safe(rs, "phone"));
        row.put("dateOfBirth",     rs.getDate("date_of_birth")    != null ? rs.getDate("date_of_birth").toString()    : null);
        row.put("anniversaryDate", rs.getDate("anniversary_date") != null ? rs.getDate("anniversary_date").toString() : null);
        row.put("createdAt",       toIso(rs, "created_at"));
        return row;
    }

    // ════════════════════════════════════════════════════════════
    // ── JDBC UTILITIES ─────────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    private String safe(ResultSet rs, String column) {
        try {
            String v = rs.getString(column);
            return v == null ? "" : v;
        } catch (Exception e) { return ""; }
    }

    private String toIso(ResultSet rs, String column) {
        try {
            return rs.getTimestamp(column) == null ? null : ISO.format(rs.getTimestamp(column).toLocalDateTime());
        } catch (Exception e) { return null; }
    }
}