package com.example.titan_watch_learning_project.repository;//package com.example.titan.repository;
//import com.example.titan.entity.Store;
import com.example.titan_watch_learning_project.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByStoreCode(String storeCode);
}