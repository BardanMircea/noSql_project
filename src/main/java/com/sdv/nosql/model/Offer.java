package com.sdv.nosql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    private String id;

    private String from;
    private String to;

    private LocalDate departDate;
    private LocalDate returnDate;

    private String provider;
    private BigDecimal price;
    private String currency;

    private List<Leg> legs;

    private Hotel hotel;
    private Activity activity;
}