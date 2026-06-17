package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.BrandCarouselCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandCarouselCardRepository extends JpaRepository<BrandCarouselCard, Long> {

    List<BrandCarouselCard> findByGenderIgnoreCaseAndActiveTrueOrderByDisplayOrderAscIdAsc(
            String gender
    );
}