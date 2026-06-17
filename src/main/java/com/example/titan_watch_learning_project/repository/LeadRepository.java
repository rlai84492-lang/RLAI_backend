package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByStatusOrderByCreatedAtDesc(Lead.LeadStatus status);
    List<Lead> findAllByOrderByCreatedAtDesc();
    Long countByStatus(Lead.LeadStatus status);

    // Paginated
    Page<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Flow filtered + paginated
    Page<Lead> findByFlowOrderByCreatedAtDesc(String flow, Pageable pageable);
}