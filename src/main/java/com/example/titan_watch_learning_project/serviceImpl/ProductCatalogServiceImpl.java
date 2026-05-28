package com.example.titan_watch_learning_project.serviceImpl;

import com.example.titan_watch_learning_project.entity.WatchProduct;
import com.example.titan_watch_learning_project.repository.WatchProductRepository;
import com.example.titan_watch_learning_project.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private final WatchProductRepository watchProductRepository;

    @Override
    public List<WatchProduct> getProductsByCollectionAndPrice(
            String collectionType,
            String priceBucket
    ) {
        return watchProductRepository
                .findTop6ByCollectionTypeAndPriceBucketAndActiveTrueOrderByIdAsc(
                        collectionType,
                        priceBucket
                );
    }

    @Override
    public List<WatchProduct> getProductsByCollectionAndPriceBuckets(
            String collectionType,
            List<String> priceBuckets
    ) {
        return watchProductRepository
                .findTop6ByCollectionTypeAndPriceBucketInAndActiveTrueOrderByIdAsc(
                        collectionType,
                        priceBuckets
                );
    }

    @Override
    public List<WatchProduct> getProductsByCollectionPriceAndStyle(
            String collectionType,
            String priceBucket,
            String style
    ) {
        return watchProductRepository
                .findTop6ByCollectionTypeAndPriceBucketAndStyleAndActiveTrueOrderByIdAsc(
                        collectionType,
                        priceBucket,
                        style
                );
    }

    @Override
    public List<WatchProduct> getProductsByCollection(String collectionType) {
        return watchProductRepository
                .findTop6ByCollectionTypeAndActiveTrueOrderByIdAsc(collectionType);
    }
}