package com.cloudcomputing.webapp.controller;

import com.cloudcomputing.webapp.service.ProductService;
import com.cloudcomputing.webapp.vo.ProductDetailsVO;
import com.timgroup.statsd.StatsDClient;
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

    @Autowired
    StatsDClient statsDClient;

    @PostMapping
    public ResponseEntity addProduct(@RequestBody ProductDetailsVO productDetails,
                                     @RequestHeader(value = "Authorization", required = false) String header) {
        statsDClient.incrementCounter("endpoint.addProduct.post");
        ResponseEntity productData = productService.addProduct(productDetails, header);
        return productData;
    }

    @GetMapping("/{productId}")
    public ResponseEntity getProduct(@PathVariable("productId") Integer productId) {
        statsDClient.incrementCounter("endpoint.getProduct.get");
        ResponseEntity productData = productService.getProduct(productId);
        return productData;
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity deleteProduct(@PathVariable("productId") Integer productId,
                                        @RequestHeader(value = "Authorization", required = false) String header) {
        statsDClient.incrementCounter("endpoint.deleteProduct.delete");
        ResponseEntity deletedProduct = productService.deleteProduct(productId, header);
        return deletedProduct;
    }

    @PutMapping("/{productId}")
    public ResponseEntity updateProductPut(@PathVariable("productId") Integer productId,
                                           @RequestHeader(value = "Authorization", required = false) String header, @RequestBody ProductDetailsVO productDetails) {
        statsDClient.incrementCounter("endpoint.updateProduct.put");
        ResponseEntity updatedProduct = productService.updateProductPut(productId, header, productDetails);
        return updatedProduct;
    }

    @PatchMapping("/{productId}")
    public ResponseEntity updateProductPatch(@PathVariable("productId") Integer productId,
                                             @RequestHeader(value = "Authorization", required = false) String header, @RequestBody ProductDetailsVO productDetails) {
        statsDClient.incrementCounter("endpoint.updateProduct.patch");
        ResponseEntity updatedProduct = productService.updateProductPatch(productId, header, productDetails);
        return updatedProduct;
    }

    @PostMapping("/{productId}/image")
    public ResponseEntity uploadImage(@PathVariable("productId") Integer productId, @RequestHeader(value = "Authorization", required = false) String header,
                                      @RequestParam("file") MultipartFile productImage) {
        statsDClient.incrementCounter("endpoint.uploadImage.post");
        ResponseEntity uploadedImage = productService.uploadImage(productId, header, productImage);
        return uploadedImage;
    }

    @GetMapping("/{productId}/image")
    public ResponseEntity fetchImageList(@PathVariable("productId") Integer productId, @RequestHeader(value = "Authorization", required = false) String header) {
        statsDClient.incrementCounter("endpoint.fetchImageList.get");
        ResponseEntity imageList = productService.fetchImageList(productId, header);
        return imageList;
    }

    @GetMapping("/{productId}/image/{imageId}")
    public ResponseEntity fetchImageDetails(@PathVariable("productId") Integer productId, @PathVariable("imageId") Integer imageId,
                                            @RequestHeader(value = "Authorization", required = false) String header) {
        statsDClient.incrementCounter("endpoint.fetchImageDetails.get");
        ResponseEntity imageDetails = productService.fetchImageDetails(productId, imageId, header);
        return imageDetails;
    }

    @DeleteMapping("/{productId}/image/{imageId}")
    public ResponseEntity deleteImage(@PathVariable("productId") Integer productId, @PathVariable("imageId") Integer imageId,
                                      @RequestHeader(value = "Authorization", required = false) String header) {
        statsDClient.incrementCounter("endpoint.deleteImage.delete");
        ResponseEntity deletedImage = productService.deleteImage(productId, imageId, header);
        return deletedImage;
    }
}
