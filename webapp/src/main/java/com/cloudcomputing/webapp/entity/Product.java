package com.cloudcomputing.webapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String name;
    String description;
    String sku;
    String manufacturer;
    Integer quantity;
    String dateAdded;
    String dateLastUpdated;
    Integer ownerUserId;

}
