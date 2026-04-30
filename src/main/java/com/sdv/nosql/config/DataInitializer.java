package com.sdv.nosql.config;

import com.sdv.nosql.model.Activity;
import com.sdv.nosql.model.Hotel;
import com.sdv.nosql.model.Leg;
import com.sdv.nosql.model.Offer;
import com.sdv.nosql.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;
    private final OfferRepository offerRepository;
    private final Neo4jClient neo4jClient;

    @Override
    public void run(String... args) {
        log.info("DataInitializer starting, database: {}", mongoTemplate.getDb().getName());

        mongoTemplate.indexOps(Offer.class)
                .ensureIndex(new Index()
                        .on("from", Sort.Direction.ASC)
                        .on("to", Sort.Direction.ASC)
                        .on("price", Sort.Direction.ASC));

        mongoTemplate.indexOps(Offer.class)
                .ensureIndex(new TextIndexDefinition.TextIndexDefinitionBuilder()
                        .onField("provider")
                        .onField("hotel.name")
                        .onField("activity.title")
                        .build());

        long count = offerRepository.count();
        log.info("Offers in DB: {}", count);

        if (count == 0) {
            offerRepository.save(buildSampleOffer());
            log.info("Sample offer inserted");
        }

        initNeo4j();
    }

    private static Offer buildSampleOffer() {
        Offer offer = new Offer();
        offer.setFrom("PAR");
        offer.setTo("TYO");
        offer.setDepartDate(LocalDate.of(2026, 6, 1));
        offer.setReturnDate(LocalDate.of(2026, 6, 15));
        offer.setProvider("AirZen");
        offer.setPrice(BigDecimal.valueOf(750));
        offer.setCurrency("EUR");
        offer.setLegs(List.of(new Leg("AZ123", "PAR", "TYO", "13h")));
        offer.setHotel(new Hotel("Tokyo Central Hotel", 14, BigDecimal.valueOf(900)));
        offer.setActivity(new Activity("Visite de Shibuya", BigDecimal.valueOf(40)));
        return offer;
    }

    private void initNeo4j() {
        neo4jClient.query("""
            MERGE (par:City {code:'PAR', name:'Paris', country:'FR'})
            MERGE (tyo:City {code:'TYO', name:'Tokyo', country:'JP'})
            MERGE (osa:City {code:'OSA', name:'Osaka', country:'JP'})
            MERGE (sel:City {code:'SEL', name:'Seoul', country:'KR'})
            MERGE (tyo)-[:NEAR {weight:0.9}]->(osa)
            MERGE (tyo)-[:NEAR {weight:0.7}]->(sel)
            MERGE (par)-[:NEAR {weight:0.8}]->(tyo)
            """).run();
        log.info("Neo4j city graph initialised");
    }
}
