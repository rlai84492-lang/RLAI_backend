

package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.dto.DashboardResponse;
import com.example.titan_watch_learning_project.entity.Lead;
import com.example.titan_watch_learning_project.repository.LeadRepository;
import com.example.titan_watch_learning_project.service.DashboardService;
import com.example.titan_watch_learning_project.serviceImpl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImpl dashboardService;
    private final LeadRepository leadRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/bot-sessions")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public List<DashboardResponse.SessionDto> getSessions() {
        return dashboardService.getSessions();
    }

    @GetMapping("/bot-sessions/flow/{flow}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public List<DashboardResponse.SessionDto> getSessionsByFlow(
            @PathVariable String flow
    ) {
        return dashboardService.getSessions()
                .stream()
                .filter(s -> flow.equalsIgnoreCase(s.getFlow()))
                .collect(Collectors.toList());
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public ResponseEntity<?> getLeads(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false)    String flow
    ) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Lead> leadsPage = (flow != null && !flow.isBlank())
                ? leadRepository.findByFlowOrderByCreatedAtDesc(flow, pageable)
                : leadRepository.findAllByOrderByCreatedAtDesc(pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("leads",       leadsPage.getContent().stream()
                .map(dashboardService::toLeadDto)
                .toList());
        response.put("totalPages",  leadsPage.getTotalPages());
        response.put("totalLeads",  leadsPage.getTotalElements());
        response.put("currentPage", page);
        response.put("pageSize",    size);

        return ResponseEntity.ok(response);
    }
}