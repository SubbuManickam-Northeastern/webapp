package com.cloudcomputing.webapp.dto;

import lombok.Data;

@Data
public class ProductDataDTO {

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
