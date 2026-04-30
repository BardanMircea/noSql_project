package com.sdv.nosql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Leg {
    private String flightNum;
    private String dep;
    private String arr;
    private String duration;
}
