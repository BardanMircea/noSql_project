package com.sdv.nosql.dto;

import com.sdv.nosql.model.Offer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferDetailsResponse {

    private Offer offer;
    private List<String> relatedOffers;
}
