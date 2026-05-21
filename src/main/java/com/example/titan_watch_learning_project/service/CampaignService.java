package com.example.titan_watch_learning_project.service;

//package com.example.titan.service;
public interface CampaignService {
    void triggerT10Campaign();
    void triggerTDayCampaign();
    void manualTrigger(String phone, String customerName, String campaignType);
}