package com.sdv.nosql.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();
    private final AtomicLong totalOffersTimeMs = new AtomicLong();
    private final AtomicLong offersRequestCount = new AtomicLong();

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public void recordOffersTime(long durationMs) {
        totalOffersTimeMs.addAndGet(durationMs);
        offersRequestCount.incrementAndGet();
    }

    public Map<String, Object> getMetrics() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        long count = offersRequestCount.get();

        double hitRate = total == 0 ? 0 : (double) hits / total;
        double avgTime = count == 0 ? 0 : (double) totalOffersTimeMs.get() / count;

        return Map.of(
                "averageOffersTimeMs", avgTime,
                "cacheHitRate", hitRate,
                "cacheHits", hits,
                "cacheMisses", misses
        );
    }
}
