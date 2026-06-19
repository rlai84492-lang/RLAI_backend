////package com.example.titan_watch_learning_project.repository;
////
////import com.example.titan_watch_learning_project.entity.Lead;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.Pageable;
////import org.springframework.data.jpa.repository.JpaRepository;
////import org.springframework.stereotype.Repository;
////
////import java.util.List;
////
////@Repository
////public interface LeadRepository extends JpaRepository<Lead, Long> {
////    List<Lead> findByStatusOrderByCreatedAtDesc(Lead.LeadStatus status);
////    List<Lead> findAllByOrderByCreatedAtDesc();
////    Long countByStatus(Lead.LeadStatus status);
////
////    // Paginated
////    Page<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);
////
////    // Flow filtered + paginated
////    Page<Lead> findByFlowOrderByCreatedAtDesc(String flow, Pageable pageable);
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
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface LeadRepository extends JpaRepository<Lead, Long> {
//
//    // ── Existing methods (same rakho) ───────────────────────────────
//    List<Lead> findAllByOrderByCreatedAtDesc();
//
//    Page<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);
//
//    Page<Lead> findByFlowOrderByCreatedAtDesc(String flow, Pageable pageable);
//
//    // ── NAYE — Date-range wale methods ──────────────────────────────
//
//    // Flow + Date range dono ke saath
//    @Query("SELECT l FROM Lead l WHERE l.flow = :flow " +
//            "AND l.createdAt BETWEEN :from AND :to " +
//            "ORDER BY l.createdAt DESC")
//    Page<Lead> findByFlowAndDateRange(
//            @Param("flow") String flow,
//            @Param("from") LocalDateTime from,
//            @Param("to")   LocalDateTime to,
//            Pageable pageable
//    );
//
//    // Sirf Date range (flow = null/all)
//    @Query("SELECT l FROM Lead l WHERE l.createdAt BETWEEN :from AND :to " +
//            "ORDER BY l.createdAt DESC")
//    Page<Lead> findByDateRange(
//            @Param("from") LocalDateTime from,
//            @Param("to")   LocalDateTime to,
//            Pageable pageable
//    );
//}

package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Leads table ka data access layer — saari custom queries native SQL mein.
 * Spring Data JPA ke derived method names (findBy...) ke liye Hibernate khud
 * query banata hai (no native SQL needed) — wo as-is rakhe hain.
 * Jahan custom WHERE/JOIN chahiye, wahan @Query(nativeQuery = true) use kiya hai.
 */
public interface LeadRepository extends JpaRepository<Lead, Long> {

    // ── Derived methods — Spring khud query banata hai, native SQL ki zaroorat nahi ──
    List<Lead> findAllByOrderByCreatedAtDesc();

    Page<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Lead> findByFlowOrderByCreatedAtDesc(String flow, Pageable pageable);

    // ════════════════════════════════════════════════════════════
    // ── NATIVE SQL QUERIES ──────────────────────────────────────
    // ════════════════════════════════════════════════════════════

    /**
     * Flow + Date range dono ke saath — native SQL.
     * countQuery zaroori hai warna Page ka total count galat aayega
     * (Hibernate apne aap native query se count nahi nikaal pata).
     */
    @Query(
            value = "SELECT * FROM leads l " +
                    "WHERE l.flow = :flow " +
                    "AND l.created_at BETWEEN :from AND :to " +
                    "ORDER BY l.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM leads l " +
                    "WHERE l.flow = :flow " +
                    "AND l.created_at BETWEEN :from AND :to",
            nativeQuery = true
    )
    Page<Lead> findByFlowAndDateRange(
            @Param("flow") String flow,
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    /**
     * Sirf Date range (flow = null/all) — native SQL.
     */
    @Query(
            value = "SELECT * FROM leads l " +
                    "WHERE l.created_at BETWEEN :from AND :to " +
                    "ORDER BY l.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM leads l " +
                    "WHERE l.created_at BETWEEN :from AND :to",
            nativeQuery = true
    )
    Page<Lead> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable
    );

    /**
     * Single lead by mid (agar Lead entity mein mid column hai) — example native query.
     * Agar tumhare Lead entity mein "mid" column nahi hai, ye method hata sakte ho.
     */
    // @Query(value = "SELECT * FROM leads WHERE mid = :mid LIMIT 1", nativeQuery = true)
    // Lead findByMidNative(@Param("mid") String mid);
}