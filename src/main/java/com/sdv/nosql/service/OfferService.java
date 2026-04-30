package com.sdv.nosql.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdv.nosql.dto.CreateOfferRequest;
import com.sdv.nosql.dto.OfferDetailsResponse;
import com.sdv.nosql.dto.OfferResponse;
import com.sdv.nosql.exception.NotFoundException;
import com.sdv.nosql.model.Offer;
import com.sdv.nosql.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Neo4jClient neo4jClient;
    private final MetricsService metricsService;

    public List<OfferResponse> searchOffers(String from, String to, Integer limit, String q) {
        long start = System.currentTimeMillis();
        try {
            // Cache key includes all query params to avoid returning stale/wrong results
            String cacheKey = "offers:search:" + from + ":" + to + ":" + limit + ":" + (q == null ? "" : q);
            String cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                try {
                    List<OfferResponse> result = objectMapper.readValue(cached, new TypeReference<>() {});
                    metricsService.recordCacheHit();
                    return result;
                } catch (JsonProcessingException e) {
                    // Corrupted cache entry — fall through to DB fetch
                    log.warn("Cache deserialization failed for key {}, fetching from DB", cacheKey, e);
                }
            }

            metricsService.recordCacheMiss();

            Pageable pageable = PageRequest.of(0, limit);
            List<Offer> offers;

            if (q != null && !q.isBlank()) {
                offers = offerRepository.searchByTextAndRoute(q, from, to, pageable);
            } else {
                offers = offerRepository.findByFromAndToOrderByPriceAsc(from, to, pageable);
            }

            List<OfferResponse> results = offers.stream().map(OfferResponse::from).toList();

            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(results), Duration.ofSeconds(60));
            } catch (JsonProcessingException e) {
                log.warn("Failed to cache search results for key {}", cacheKey, e);
            }

            return results;
        } finally {
            metricsService.recordOffersTime(System.currentTimeMillis() - start);
        }
    }

    public OfferDetailsResponse getOfferById(String id) {
        String cacheKey = "offers:detail:" + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, OfferDetailsResponse.class);
            } catch (JsonProcessingException e) {
                // Corrupted cache entry — fall through to DB fetch
                log.warn("Cache deserialization failed for offer {}, fetching from DB", id, e);
            }
        }

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + id));

        List<String> relatedOffers = getRelatedOffers(offer);
        OfferDetailsResponse response = new OfferDetailsResponse(offer, relatedOffers);

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), Duration.ofSeconds(300));
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache offer details for {}", id, e);
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

    public OfferResponse createOffer(CreateOfferRequest request) {
        Offer offer = new Offer();
        offer.setFrom(request.getFrom());
        offer.setTo(request.getTo());
        offer.setDepartDate(request.getDepartDate());
        offer.setReturnDate(request.getReturnDate());
        offer.setProvider(request.getProvider());
        offer.setPrice(request.getPrice());
        offer.setCurrency(request.getCurrency());
        offer.setLegs(request.getLegs());
        offer.setHotel(request.getHotel());
        offer.setActivity(request.getActivity());

        Offer saved = offerRepository.save(offer);

        // Evict all cached searches for this route so the new offer appears immediately.
        // Note: redisTemplate.keys() is O(N) — acceptable for this project's scale.
        Set<String> keysToEvict = redisTemplate.keys(
                "offers:search:" + saved.getFrom() + ":" + saved.getTo() + ":*");
        if (keysToEvict != null && !keysToEvict.isEmpty()) {
            redisTemplate.delete(keysToEvict);
        }

        try {
            redisTemplate.convertAndSend("offers:new", objectMapper.writeValueAsString(Map.of(
                    "offerId", saved.getId(),
                    "from", saved.getFrom(),
                    "to", saved.getTo()
            )));
        } catch (JsonProcessingException e) {
            log.warn("Failed to publish offer creation event for {}", saved.getId(), e);
        }

        return OfferResponse.from(saved);
    }
}
