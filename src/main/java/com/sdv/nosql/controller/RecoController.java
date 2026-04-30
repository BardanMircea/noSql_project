package com.sdv.nosql.controller;

import com.sdv.nosql.dto.RecommendationResponse;
import com.sdv.nosql.service.RecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reco")
public class RecoController {

    private final RecoService recoService;

    @GetMapping
    public List<RecommendationResponse> reco(
            @RequestParam String city,
            @RequestParam(defaultValue = "3") Integer k
    ) {
        return recoService.recommend(city, k);
    }
}
