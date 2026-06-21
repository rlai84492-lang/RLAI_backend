////
////
////package com.example.titan_watch_learning_project.repository;
////
////import com.example.titan_watch_learning_project.entity.Lead;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.Pageable;
////import org.springframework.data.jpa.repository.JpaRepository;
////import org.springframework.data.jpa.repository.Query;
////import org.springframework.data.repository.query.Param;
////
////import java.time.LocalDateTime;
////import java.util.List;
////
/////**
//// * Leads table ka data access layer — saari custom queries native SQL mein.
//// * Spring Data JPA ke derived method names (findBy...) ke liye Hibernate khud
//// * query banata hai (no native SQL needed) — wo as-is rakhe hain.
//// * Jahan custom WHERE/JOIN chahiye, wahan @Query(nativeQuery = true) use kiya hai.
//// */
////public interface LeadRepository extends JpaRepository<Lead, Long> {
////
////    // ── Derived methods — Spring khud query banata hai, native SQL ki zaroorat nahi ──
////    List<Lead> findAllByOrderByCreatedAtDesc();
////
////    Page<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);
////
////    Page<Lead> findByFlowOrderByCreatedAtDesc(String flow, Pageable pageable);
////
////    // ════════════════════════════════════════════════════════════
////    // ── NATIVE SQL QUERIES ──────────────────────────────────────
////    // ════════════════════════════════════════════════════════════
////
////    /**
////     * Flow + Date range dono ke saath — native SQL.
////     * countQuery zaroori hai warna Page ka total count galat aayega
////     * (Hibernate apne aap native query se count nahi nikaal pata).
////     */
////    @Query(
////            value = "SELECT * FROM leads l " +
////                    "WHERE l.flow = :flow " +
////                    "AND l.created_at BETWEEN :from AND :to " +
////                    "ORDER BY l.created_at DESC",
////            countQuery = "SELECT COUNT(*) FROM leads l " +
////                    "WHERE l.flow = :flow " +
////                    "AND l.created_at BETWEEN :from AND :to",
////            nativeQuery = true
////    )
////    Page<Lead> findByFlowAndDateRange(
////            @Param("flow") String flow,
////            @Param("from") LocalDateTime from,
////            @Param("to") LocalDateTime to,
////            Pageable pageable
////    );
////
////    /**
////     * Sirf Date range (flow = null/all) — native SQL.
////     */
////    @Query(
////            value = "SELECT * FROM leads l " +
////                    "WHERE l.created_at BETWEEN :from AND :to " +
////                    "ORDER BY l.created_at DESC",
////            countQuery = "SELECT COUNT(*) FROM leads l " +
////                    "WHERE l.created_at BETWEEN :from AND :to",
////            nativeQuery = true
////    )
////    Page<Lead> findByDateRange(
////            @Param("from") LocalDateTime from,
////            @Param("to") LocalDateTime to,
////            Pageable pageable
////    );
////
////    List<Lead> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
////
////}
//
//
//package com.example.titan_watch_learning_project.repository;
//
//import com.example.titan_watch_learning_project.entity.Lead;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface LeadRepository extends JpaRepository<Lead, Long> {
//
//    /**
//     * ★ Paginated leads — specific flow + date range.
//     * Uses index: idx_leads_flow_created(flow, created_at).
//     * Key fix: createdAt >= from AND createdAt < to (not DATE() BETWEEN).
//     */
//    @Query("SELECT l FROM Lead l " +
//            "WHERE l.flow = :flow " +
//            "  AND l.createdAt >= :from AND l.createdAt < :to " +
//            "ORDER BY l.createdAt DESC")
//    Page<Lead> findByFlowAndDateRange(
//            @Param("flow") String flow,
//            @Param("from") LocalDateTime from,
//            @Param("to")   LocalDateTime to,
//            Pageable pageable
//    );
//
//    /**
//     * ★ Paginated leads — all flows, date range.
//     * Uses index: idx_leads_created_at(created_at).
//     */
//    @Query("SELECT l FROM Lead l " +
//            "WHERE l.createdAt >= :from AND l.createdAt < :to " +
//            "ORDER BY l.createdAt DESC")
//    Page<Lead> findByDateRange(
//            @Param("from") LocalDateTime from,
//            @Param("to")   LocalDateTime to,
//            Pageable pageable
//    );
//
//    /** Legacy — no date filter, for export/settings pages. */
//    List<Lead> findAllByOrderByCreatedAtDesc();
//
//    List<Lead> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
//
//}

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