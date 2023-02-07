package com.cloudcomputing.webapp.vo;

import lombok.Data;

@Data
public class ProductDetailsVO {

    String name;
    String description;
    String sku;
    String manufacturer;
    Integer quantity;
}
