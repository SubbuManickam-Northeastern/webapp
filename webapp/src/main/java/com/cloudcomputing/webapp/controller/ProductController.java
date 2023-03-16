package com.cloudcomputing.webapp.controller;

import com.cloudcomputing.webapp.service.ProductService;
import com.cloudcomputing.webapp.vo.ProductDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/v1/product", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping
    public ResponseEntity addProduct(@RequestBody ProductDetailsVO productDetails,
                                     @RequestHeader(value = "Authorization", required = false) String header) {
        ResponseEntity productData = productService.addProduct(productDetails, header);
        return productData;
    }

    @GetMapping("/{productId}")
    public ResponseEntity getProduct(@PathVariable("productId") Integer productId) {
        ResponseEntity productData = productService.getProduct(productId);
        return productData;
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity deleteProduct(@PathVariable("productId") Integer productId,
                                        @RequestHeader(value = "Authorization", required = false) String header) {
        ResponseEntity deletedProduct = productService.deleteProduct(productId, header);
        return deletedProduct;
    }

    @PutMapping("/{productId}")
    public ResponseEntity updateProductPut(@PathVariable("productId") Integer productId,
                                           @RequestHeader(value = "Authorization", required = false) String header, @RequestBody ProductDetailsVO productDetails) {
        ResponseEntity updatedProduct = productService.updateProductPut(productId, header, productDetails);
        return updatedProduct;
    }

    @PatchMapping("/{productId}")
    public ResponseEntity updateProductPatch(@PathVariable("productId") Integer productId,
                                             @RequestHeader(value = "Authorization", required = false) String header, @RequestBody ProductDetailsVO productDetails) {
        ResponseEntity updatedProduct = productService.updateProductPatch(productId, header, productDetails);
        return updatedProduct;
    }

    @PostMapping("/{productId}/image")
    public ResponseEntity uploadImage(@PathVariable("productId") Integer productId, @RequestHeader(value = "Authorization", required = false) String header,
                                      @RequestParam("file") MultipartFile productImage) {
        ResponseEntity uploadedImage = productService.uploadImage(productId, header, productImage);
        return uploadedImage;
    }

    @GetMapping("/{productId}/image")
    public ResponseEntity fetchImageList(@PathVariable("productId") Integer productId, @RequestHeader(value = "Authorization", required = false) String header) {
        ResponseEntity imageList = productService.fetchImageList(productId, header);
        return imageList;
    }

    @GetMapping("/{productId}/image/{imageId}")
    public ResponseEntity fetchImageDetails(@PathVariable("productId") Integer productId, @PathVariable("imageId") Integer imageId,
                                            @RequestHeader(value = "Authorization", required = false) String header) {
        ResponseEntity imageDetails = productService.fetchImageDetails(productId, imageId, header);
        return imageDetails;
    }

    @DeleteMapping("/{productId}/image/{imageId}")
    public ResponseEntity deleteImage(@PathVariable("productId") Integer productId, @PathVariable("imageId") Integer imageId,
                                      @RequestHeader(value = "Authorization", required = false) String header) {
        ResponseEntity deletedImage = productService.deleteImage(productId, imageId, header);
        return deletedImage;
    }
}
