package com.sdv.nosql.dto;

import com.sdv.nosql.model.Offer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferResponse {

    private String id;
    private String from;
    private String to;
    private LocalDate departDate;
    private LocalDate returnDate;
    private String provider;
    private BigDecimal price;
    private String currency;

    public static OfferResponse from(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getFrom(),
                offer.getTo(),
                offer.getDepartDate(),
                offer.getReturnDate(),
                offer.getProvider(),
                offer.getPrice(),
                offer.getCurrency()
        );
    }
}
