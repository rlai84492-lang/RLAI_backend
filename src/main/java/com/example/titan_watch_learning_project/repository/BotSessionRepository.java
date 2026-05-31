package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BotSessionRepository extends JpaRepository<BotSession, Long> {

    List<BotSession> findByPhoneAndIsActiveTrue(String phone);

    Optional<BotSession> findTopByPhoneAndIsActiveTrueOrderByLastActivityDesc(String phone);

    void deleteByPhone(String phone);

    Optional<BotSession> findTopByPhoneOrderByLastActivityDesc(String phone);
}