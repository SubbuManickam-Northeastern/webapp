package com.cloudcomputing.webapp.serviceimpl;

import com.cloudcomputing.webapp.dto.ProductDataDTO;
import com.cloudcomputing.webapp.entity.Product;
import com.cloudcomputing.webapp.entity.User;
import com.cloudcomputing.webapp.repository.ProductRepository;
import com.cloudcomputing.webapp.repository.UserRepository;
import com.cloudcomputing.webapp.service.ProductService;
import com.cloudcomputing.webapp.vo.ProductDetailsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Override
    public ResponseEntity addProduct(ProductDetailsVO productDetails, String header) {

        ProductDataDTO newProduct = new ProductDataDTO();
        Product product = new Product();

        try {
            String currentDate = String.valueOf(java.time.LocalDateTime.now());

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            String[] values = userService.authenticateUser(header);
            List<String> existingSkus = productRepository.getSkus();

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                return new ResponseEntity<>("Unauthorized. Only valid users can create a product", HttpStatus.UNAUTHORIZED);
            }

            if(existingSkus.contains(productDetails.getSku())) {
                return new ResponseEntity<>("Bad Request. Enter unique SKU", HttpStatus.BAD_REQUEST);
            }

            if(productDetails.getQuantity() < 0) {
                return new ResponseEntity<>("Bad Request. Enter valid quantity", HttpStatus.BAD_REQUEST);
            }

            product.setName(productDetails.getName());
            product.setManufacturer(productDetails.getManufacturer());
            product.setDescription(productDetails.getDescription());
            product.setQuantity(productDetails.getQuantity());
            product.setSku(productDetails.getSku());
            product.setDateAdded(currentDate);
            product.setDateLastUpdated(currentDate);
            product.setOwnerUserId(authUser.getId());
            productRepository.save(product);

            Product createdProduct = productRepository.getBySku(productDetails.getSku());
            newProduct.setId(createdProduct.getId());
            newProduct.setName(createdProduct.getName());
            newProduct.setDescription(createdProduct.getDescription());
            newProduct.setManufacturer(createdProduct.getManufacturer());
            newProduct.setSku(createdProduct.getSku());
            newProduct.setQuantity(createdProduct.getQuantity());
            newProduct.setDateAdded(createdProduct.getDateAdded());
            newProduct.setDateLastUpdated(createdProduct.getDateLastUpdated());
            newProduct.setOwnerUserId(createdProduct.getOwnerUserId());

            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getProduct(Integer productId) {

        ProductDataDTO selectedProduct = new ProductDataDTO();

        try {
            Product product = productRepository.getByProductId(productId);

            if(product == null) {
                return new ResponseEntity<>("Product not found. Enter a valid Product Id", HttpStatus.NOT_FOUND);
            }

            selectedProduct.setId(product.getId());
            selectedProduct.setName(product.getName());
            selectedProduct.setDescription(product.getDescription());
            selectedProduct.setManufacturer(product.getManufacturer());
            selectedProduct.setSku(product.getSku());
            selectedProduct.setQuantity(product.getQuantity());
            selectedProduct.setDateAdded(product.getDateAdded());
            selectedProduct.setDateLastUpdated(product.getDateLastUpdated());
            selectedProduct.setOwnerUserId(product.getOwnerUserId());

            return new ResponseEntity<>(selectedProduct, HttpStatus.OK);

        } catch(Exception e) {
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity deleteProduct(Integer productId, String header) {

        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            Product selectedProduct = productRepository.getByProductId(productId);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                return new ResponseEntity<>("Unauthorized. Only authorized users can delete a product", HttpStatus.UNAUTHORIZED);
            }

            if(selectedProduct == null) {
                return new ResponseEntity<>("Product not found. Enter a valid Product Id", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                return new ResponseEntity<>("Forbidden Access. Only owners can delete a product", HttpStatus.FORBIDDEN);
            }

            productRepository.deleteById(productId);

            return new ResponseEntity<>("Delete Successful", HttpStatus.NO_CONTENT);

        } catch(Exception e) {
            return new ResponseEntity<>("Bad Request" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity updateProductPut(Integer productId, String header, ProductDetailsVO productDetails) {

        try {

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            Product selectedProduct = productRepository.getByProductId(productId);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                return new ResponseEntity<>("Unauthorized Access. Enter valid credentials", HttpStatus.UNAUTHORIZED);
            }

            if(selectedProduct == null) {
                return new ResponseEntity<>("Product not found. Enter a valid Product Id", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                return new ResponseEntity<>("Forbidden Access. Only owners can update products", HttpStatus.FORBIDDEN);
            }

            if(!selectedProduct.getSku().equals(productDetails.getSku())) {
                return new ResponseEntity<>("Bad Request. SKU cannot be updated", HttpStatus.BAD_REQUEST);
            }

            if(productDetails.getQuantity() != null && productDetails.getQuantity() < 0) {
                return new ResponseEntity<>("Bad Request. Enter valid quantity", HttpStatus.BAD_REQUEST);
            }

            String updateDate = String.valueOf(java.time.LocalDateTime.now());

            selectedProduct.setName(productDetails.getName());
            selectedProduct.setSku(productDetails.getSku());
            selectedProduct.setDescription(productDetails.getDescription());
            selectedProduct.setManufacturer(productDetails.getManufacturer());
            selectedProduct.setQuantity(productDetails.getQuantity());
            selectedProduct.setDateLastUpdated(updateDate);
            productRepository.save(selectedProduct);

            return new ResponseEntity<>("Product Updated", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity updateProductPatch(Integer productId, String header, ProductDetailsVO productDetails) {

        try {

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            Product selectedProduct = productRepository.getByProductId(productId);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                return new ResponseEntity<>("Unauthorized Access. Enter valid credentials", HttpStatus.UNAUTHORIZED);
            }

            if(selectedProduct == null) {
                return new ResponseEntity<>("Product not found. Enter a valid Product Id", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                return new ResponseEntity<>("Forbidden Access. Only owners can update products", HttpStatus.FORBIDDEN);
            }

            if(!selectedProduct.getSku().equals(productDetails.getSku())) {
                return new ResponseEntity<>("Bad Request. SKU cannot be updated", HttpStatus.BAD_REQUEST);
            }

            if(productDetails.getQuantity() != null && productDetails.getQuantity() < 0) {
                return new ResponseEntity<>("Bad Request. Enter valid quantity", HttpStatus.BAD_REQUEST);
            }

            String updateDate = String.valueOf(java.time.LocalDateTime.now());

            selectedProduct.setName(productDetails.getName());
            selectedProduct.setSku(productDetails.getSku());
            selectedProduct.setDescription(productDetails.getDescription());
            selectedProduct.setManufacturer(productDetails.getManufacturer());
            selectedProduct.setQuantity(productDetails.getQuantity());
            selectedProduct.setDateLastUpdated(updateDate);
            productRepository.save(selectedProduct);

            return new ResponseEntity<>("Product Updated", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }
}
