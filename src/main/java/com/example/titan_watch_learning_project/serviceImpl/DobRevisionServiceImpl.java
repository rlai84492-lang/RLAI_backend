package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.repository.DobRevisionDataRepository;
import com.example.titan_watch_learning_project.service.DobRevisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DobRevisionServiceImpl — ALL business logic lives here.
 *
 *  ✅  Date range resolution (IST-aware)
 *  ✅  Knowledge of which step-names mean "pending DOB/date correction"
 *  ✅  Response shaping (pending + filled + counts in one envelope)
 *
 *  ❌  No SQL — all data access goes through DobRevisionDataRepository
 *  ❌  No HTTP concerns — controller handles those
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DobRevisionServiceImpl implements DobRevisionService {

    private final DobRevisionDataRepository repo;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /** Step names that mean "user said No and hasn't replied with correct date yet". */
    private static final List<String> PENDING_STEPS = List.of(
            "BIRTHDAY_T10_DOB_CORRECTION_PENDING",
            "ANNIVERSARY_T10_DATE_CORRECTION_PENDING"
    );

    @Override
    public Map<String, Object> getDobRevisionData(String range, String startDate, String endDate) {

        LocalDateTime[] dt = resolveDateTimeRange(range, startDate, endDate);
        LocalDateTime from = dt[0];
        LocalDateTime to   = dt[1];

        log.info("DOB Revision request → range={} from={} to={}", range, from, to);

        // ── Pending (sessions stuck on correction step) ──────────────
        List<Map<String, Object>> pendingRaw = repo.findPendingByStepsAndDate(PENDING_STEPS, from, to);
        List<Map<String, Object>> pending = pendingRaw.stream()
                .map(this::enrichPendingRow)
                .toList();
        long pendingCount = repo.countPendingByStepsAndDate(PENDING_STEPS, from, to);

        // ── Filled (customers who already have DOB / anniversary date) ──
        List<Map<String, Object>> filled = repo.findFilledCustomersByDate(from, to);
        long filledCount = repo.countFilledCustomersByDate(from, to);

        // ── Breakdown for the count boxes ────────────────────────────
        long pendingDob  = pending.stream().filter(r -> "Birthday".equals(r.get("flow"))).count();
        long pendingAnniv = pending.stream().filter(r -> "Anniversary".equals(r.get("flow"))).count();
        long filledDob    = filled.stream().filter(r -> r.get("dateOfBirth") != null).count();
        long filledAnniv  = filled.stream().filter(r -> r.get("anniversaryDate") != null).count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pending",          pending);
        response.put("pendingCount",     pendingCount);
        response.put("pendingDobCount",  pendingDob);
        response.put("pendingAnnivCount",pendingAnniv);
        response.put("filled",           filled);
        response.put("filledCount",      filledCount);
        response.put("filledDobCount",   filledDob);
        response.put("filledAnnivCount", filledAnniv);
        return response;
    }

    /** Adds "flow" and "revisionType" labels based on the raw step name. */
    private Map<String, Object> enrichPendingRow(Map<String, Object> raw) {
        String step = String.valueOf(raw.get("currentStep"));
        Map<String, Object> row = new LinkedHashMap<>(raw);
        row.put("flow",         step.startsWith("BIRTHDAY") ? "Birthday" : "Anniversary");
        row.put("revisionType", step.contains("DOB") ? "Date of Birth" : "Anniversary Date");
        row.remove("currentStep"); // internal detail, not needed in API response
        return row;
    }

    // ════════════════════════════════════════════════════════════
    // ── DATE RANGE RESOLVER (same pattern as DashboardServiceImpl) ─
    // ════════════════════════════════════════════════════════════

    private LocalDateTime[] resolveDateTimeRange(String range, String startDate, String endDate) {
        LocalDate today = LocalDate.now(IST);

        LocalDate fromDate;
        LocalDate toDate;

        switch (range == null ? "today" : range) {
            case "7days"   -> { fromDate = today.minusDays(6);  toDate = today; }
            case "30days"  -> { fromDate = today.minusDays(29); toDate = today; }
            case "alltime" -> { fromDate = LocalDate.of(2020, 1, 1); toDate = today; }
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
            default -> { fromDate = today; toDate = today; }
        }

        return new LocalDateTime[]{
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        };
    }
}