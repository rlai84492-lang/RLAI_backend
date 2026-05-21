package com.example.titan_watch_learning_project.repository;//package com.example.titan.repository;
//import com.example.titan.entity.Campaign;
import com.example.titan_watch_learning_project.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByCampaignType(Campaign.CampaignType type);
}