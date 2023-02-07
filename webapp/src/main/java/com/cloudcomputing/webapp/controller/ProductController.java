package com.cloudcomputing.webapp.controller;

import com.cloudcomputing.webapp.service.ProductService;
import com.cloudcomputing.webapp.vo.ProductDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping
    public ResponseEntity addProduct(@RequestBody ProductDetailsVO productDetails, @RequestHeader("Authorization") String header) {
        ResponseEntity productData = productService.addProduct(productDetails, header);
        return productData;
    }

    @GetMapping("/{productId}")
    public ResponseEntity getProduct(@PathVariable("productId") Integer productId) {
        ResponseEntity productData = productService.getProduct(productId);
        return productData;
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity deleteProduct(@PathVariable("productId") Integer productId, @RequestHeader("Authorization") String header) {
        ResponseEntity deletedProduct = productService.deleteProduct(productId, header);
        return deletedProduct;
    }
}
