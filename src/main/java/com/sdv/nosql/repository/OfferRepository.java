package com.sdv.nosql.repository;

import com.sdv.nosql.model.Offer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OfferRepository extends MongoRepository<Offer, String> {

    List<Offer> findByFromAndToOrderByPriceAsc(String from, String to, Pageable pageable);

    @Query("{ '$text': { '$search': ?0 }, 'from': ?1, 'to': ?2 }")
    List<Offer> searchByTextAndRoute(String q, String from, String to, Pageable pageable);
}
