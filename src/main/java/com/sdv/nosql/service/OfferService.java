package com.sdv.nosql.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdv.nosql.dto.OfferDetailsResponse;
import com.sdv.nosql.exception.NotFoundException;
import com.sdv.nosql.model.Offer;
import com.sdv.nosql.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Neo4jClient neo4jClient;
    private final MetricsService metricsService;

    public List<Offer> searchOffers(String from, String to, Integer limit, String q) {

        long start = System.currentTimeMillis();

        try {
            String cacheKey = "offers:" + from + ":" + to;

            String cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                metricsService.recordCacheHit();
                return objectMapper.readValue(
                        cached,
                        new TypeReference<List<Offer>>() {}
                );
            }

            metricsService.recordCacheMiss();

            Pageable pageable = PageRequest.of(0, limit);

            List<Offer> offers;

            if (q != null && !q.isBlank()) {
                offers = offerRepository.searchByTextAndRoute(q, from, to, pageable);
            } else {
                offers = offerRepository.findByFromAndToOrderByPriceAsc(from, to, pageable);
            }

            String json = objectMapper.writeValueAsString(offers);

            redisTemplate.opsForValue().set(
                    cacheKey,
                    json,
                    Duration.ofSeconds(60)
            );

            return offers;

        } catch (Exception e) {
            throw new RuntimeException("Erreur searchOffers", e);
        } finally {
            metricsService.recordOffersTime(System.currentTimeMillis() - start);
        }
    }

    public OfferDetailsResponse getOfferById(String id) {
        String cacheKey = "offers:" + id;

        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, OfferDetailsResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lecture cache Redis");
            }
        }

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));

        List<String> relatedOffers = getRelatedOffers(offer);

        OfferDetailsResponse response = new OfferDetailsResponse(offer, relatedOffers);

        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(response),
                    Duration.ofSeconds(300)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur écriture cache Redis");
        }

        return response;
    }

    private List<String> getRelatedOffers(Offer offer) {
        return neo4jClient.query("""
            MATCH (c:City {code:$to})-[r:NEAR]->(n:City)
            RETURN n.code AS city
            ORDER BY r.weight DESC
            LIMIT 3
            """)
                .bind(offer.getTo()).to("to")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("city").asString())
                .all()
                .stream()
                .toList();
    }

    public Offer createOffer(Offer offer) {
        Offer saved = offerRepository.save(offer);

        Map<String, Object> message = Map.of(
                "offerId", saved.getId(),
                "from", saved.getFrom(),
                "to", saved.getTo()
        );

        try {
            redisTemplate.convertAndSend(
                    "offers:new",
                    objectMapper.writeValueAsString(message)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur publication Redis Pub/Sub");
        }

        return saved;
    }
}
