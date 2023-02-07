package com.cloudcomputing.webapp.service;

import com.cloudcomputing.webapp.vo.ProductDetailsVO;
import org.springframework.http.ResponseEntity;

public interface ProductService {

    ResponseEntity addProduct(ProductDetailsVO productDetails, String header);

    ResponseEntity getProduct(Integer productId);

    ResponseEntity deleteProduct(Integer productId, String header);
}
