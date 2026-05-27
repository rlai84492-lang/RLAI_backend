package com.example.titan_watch_learning_project.service;

import com.example.titan_watch_learning_project.entity.WatchProduct;

import java.util.List;

public interface ProductCatalogService {

    List<WatchProduct> getProductsByCollectionAndPrice(
            String collectionType,
            String priceBucket
    );

    List<WatchProduct> getProductsByCollection(String collectionType);


    List<WatchProduct> getProductsByCollectionPriceAndStyle(
            String collectionType,
            String priceBucket,
            String style
    );
}