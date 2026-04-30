package com.sdv.nosql.service;

import com.sdv.nosql.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoService {

    private final Neo4jClient neo4jClient;

    public List<RecommendationResponse> recommend(String city, Integer k) {
        return neo4jClient.query("""
                MATCH (c:City {code:$city})-[r:NEAR]->(n:City)
                RETURN n.code AS city, r.weight AS score
                ORDER BY r.weight DESC
                LIMIT $k
                """)
                .bind(city).to("city")
                .bind(k).to("k")
                .fetchAs(RecommendationResponse.class)
                .mappedBy((typeSystem, record) ->
                        new RecommendationResponse(
                                record.get("city").asString(),
                                record.get("score").asDouble()
                        )
                )
                .all()
                .stream()
                .toList();
    }
}