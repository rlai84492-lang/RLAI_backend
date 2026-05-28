//package com.example.titan_watch_learning_project.repository;
//
//import com.example.titan_watch_learning_project.entity.WatchProduct;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface WatchProductRepository extends JpaRepository<WatchProduct, Long> {
//
//    List<WatchProduct> findTop6ByCollectionTypeAndPriceBucketAndActiveTrueOrderByIdAsc(
//            String collectionType,
//            String priceBucket
//    );
//
//    List<WatchProduct> findTop6ByCollectionTypeAndActiveTrueOrderByIdAsc(
//            String collectionType
//    );
//
//
//    List<WatchProduct> findTop6ByCollectionTypeAndPriceBucketAndStyleAndActiveTrueOrderByIdAsc(
//            String collectionType,
//            String priceBucket,
//            String style
//    );
//}



package com.example.titan_watch_learning_project.repository;

import com.example.titan_watch_learning_project.entity.WatchProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchProductRepository extends JpaRepository<WatchProduct, Long> {

    List<WatchProduct> findTop6ByCollectionTypeAndPriceBucketAndActiveTrueOrderByIdAsc(
            String collectionType,
            String priceBucket
    );

    List<WatchProduct> findTop6ByCollectionTypeAndPriceBucketInAndActiveTrueOrderByIdAsc(
            String collectionType,
            List<String> priceBuckets
    );

    List<WatchProduct> findTop6ByCollectionTypeAndPriceBucketAndStyleAndActiveTrueOrderByIdAsc(
            String collectionType,
            String priceBucket,
            String style
    );

    List<WatchProduct> findTop6ByCollectionTypeAndActiveTrueOrderByIdAsc(
            String collectionType
    );
}