package com.sdv.nosql.controller;

import com.sdv.nosql.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return metricsService.getMetrics();
    }
}
