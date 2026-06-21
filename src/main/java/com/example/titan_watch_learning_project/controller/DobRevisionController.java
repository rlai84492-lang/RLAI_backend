package com.example.titan_watch_learning_project.controller;

import com.example.titan_watch_learning_project.service.DobRevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * DOB / Anniversary Date revision tracking — HTTP layer only.
 * range: today | 7days | 30days | alltime | custom
 * startDate / endDate: yyyy-MM-dd (sirf range=custom ke liye)
 */
@RestController
@RequestMapping("/api/dob-revisions")
@RequiredArgsConstructor
public class DobRevisionController {

    private final DobRevisionService dobRevisionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE_MANAGER')")
    public Map<String, Object> getDobRevisions(
            @RequestParam(required = false, defaultValue = "today") String range,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return dobRevisionService.getDobRevisionData(range, startDate, endDate);
    }
}