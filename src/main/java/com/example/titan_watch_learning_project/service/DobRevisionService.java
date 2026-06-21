package com.example.titan_watch_learning_project.service;

import java.util.Map;

/**
 * DOB / Anniversary Date revision tracking — business contract.
 * Implementation: DobRevisionServiceImpl.java
 */
public interface DobRevisionService {

    /**
     * Returns both PENDING (users who said "No" and haven't replied yet)
     * and FILLED (customers who already have a DOB/anniversary date on file)
     * for the given date range.
     *
     * range: today | 7days | 30days | custom
     */
    Map<String, Object> getDobRevisionData(String range, String startDate, String endDate);
}