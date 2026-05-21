package com.example.titan_watch_learning_project.repository;//package com.example.titan.repository;
//import com.example.titan.entity.Watch;
import com.example.titan_watch_learning_project.entity.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WatchRepository extends JpaRepository<Watch, Long> {
    List<Watch> findByCollectionAndStyleAndIsActiveTrue(Watch.Collection collection, Watch.Style style);
    List<Watch> findByCollectionAndStyleAndPriceBetweenAndIsActiveTrue(
            Watch.Collection c, Watch.Style s, BigDecimal min, BigDecimal max);
}