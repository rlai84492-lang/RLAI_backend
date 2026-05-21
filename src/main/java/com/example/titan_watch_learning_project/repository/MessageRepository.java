package com.example.titan_watch_learning_project.repository;//package com.example.titan.repository;
//import com.example.titan.entity.Message;
import com.example.titan_watch_learning_project.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByMid(String mid);
    List<Message> findByPhoneOrderBySentAtDesc(String phone);
}