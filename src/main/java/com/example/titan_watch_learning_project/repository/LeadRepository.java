package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    /**
     * ★ Paginated leads — specific flow + date range.
     *
     * Matches the same WHERE clause as DashboardDataRepository.getLeadMetrics():
     *   (flow = :flow OR stepName LIKE :stepPattern)
     *
     * This catches leads where the flow column was not populated but
     * step_name still identifies the campaign (e.g. "BIRTHDAY_T10_MENS_...").
     * Without the OR clause the count here differs from the dashboard metric.
     *
     * Uses indexes:
     *   idx_leads_flow_created(flow, created_at)
     *   idx_leads_step_created(step_name, created_at)
     */
    @Query("SELECT l FROM Lead l " +
            "WHERE (l.flow = :flow OR l.stepName LIKE :stepPattern) " +
            "  AND l.createdAt >= :from AND l.createdAt < :to " +
            "ORDER BY l.createdAt DESC")
    Page<Lead> findByFlowOrStepPatternAndDateRange(
            @Param("flow")        String flow,
            @Param("stepPattern") String stepPattern,
            @Param("from")        LocalDateTime from,
            @Param("to")          LocalDateTime to,
            Pageable pageable
    );

    /**
     * ★ Paginated leads — all flows, date range.
     * Uses index: idx_leads_created_at(created_at).
     */
    @Query("SELECT l FROM Lead l " +
            "WHERE l.createdAt >= :from AND l.createdAt < :to " +
            "ORDER BY l.createdAt DESC")
    Page<Lead> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    /** Legacy — no date filter, for export/settings pages. */
    List<Lead> findAllByOrderByCreatedAtDesc();

    List<Lead> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

}