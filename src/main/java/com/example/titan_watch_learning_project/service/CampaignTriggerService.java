package com.example.titan_watch_learning_project.service;

public interface CampaignTriggerService {

    // Prepares DB session before Karix sends Birthday T-10 template
    void prepareBirthdayT10Session(String phone, Long customerId);

    // Prepares DB session before Karix sends Birthday T-Day template
    void prepareBirthdayTDaySession(String phone, Long customerId);

    // Prepares DB session before Karix sends Anniversary T-10 template
    void prepareAnniversaryT10Session(String phone, Long customerId);

    // Prepares DB session before Karix sends Anniversary T-Day template
    void prepareAnniversaryTDaySession(String phone, Long customerId);
}