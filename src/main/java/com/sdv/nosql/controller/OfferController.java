package com.sdv.nosql.controller;

import com.sdv.nosql.dto.CreateOfferRequest;
import com.sdv.nosql.dto.OfferDetailsResponse;
import com.sdv.nosql.dto.OfferResponse;
import com.sdv.nosql.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public List<OfferResponse> searchOffers(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String q
    ) {
        return offerService.searchOffers(from, to, limit, q);
    }

    @GetMapping("/{id}")
    public OfferDetailsResponse getOfferById(@PathVariable String id) {
        return offerService.getOfferById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse createOffer(@Valid @RequestBody CreateOfferRequest request) {
        return offerService.createOffer(request);
    }
}
