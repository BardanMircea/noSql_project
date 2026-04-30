package com.sdv.nosql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    private String name;
    private Integer nights;
    private BigDecimal price;
}
