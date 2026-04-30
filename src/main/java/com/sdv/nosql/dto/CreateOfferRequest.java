package com.sdv.nosql.dto;

import com.sdv.nosql.model.Activity;
import com.sdv.nosql.model.Hotel;
import com.sdv.nosql.model.Leg;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateOfferRequest {

    @NotBlank
    private String from;

    @NotBlank
    private String to;

    @NotNull
    private LocalDate departDate;

    private LocalDate returnDate;

    @NotBlank
    private String provider;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotBlank
    private String currency;

    private List<Leg> legs;
    private Hotel hotel;
    private Activity activity;
}
