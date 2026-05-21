package com.example.titan_watch_learning_project.repository;//package com.example.titan.repository;
//import com.example.titan.entity.CampaignLog;
import com.example.titan_watch_learning_project.entity.CampaignLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface CampaignLogRepository extends JpaRepository<CampaignLog, Long> {
    boolean existsByCustomerIdAndCampaignTypeAndSentAtDate(Long customerId, String campaignType, LocalDate date);
}