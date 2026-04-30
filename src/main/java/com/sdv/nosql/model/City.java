package com.sdv.nosql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("City")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    private String code;

    private String name;
    private String country;
}
