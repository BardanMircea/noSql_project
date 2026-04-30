package com.sdv.nosql.repository;

import com.sdv.nosql.model.City;
import org.springframework.data.neo4j.repository.Neo4jRepository;


public interface CityRepository extends Neo4jRepository<City, String> {

   }
