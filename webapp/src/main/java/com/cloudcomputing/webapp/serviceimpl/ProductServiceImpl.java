package com.cloudcomputing.webapp.serviceimpl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cloudcomputing.webapp.config.S3Configuration;
import com.cloudcomputing.webapp.dto.ImageDataDTO;
import com.cloudcomputing.webapp.dto.ProductDataDTO;
import com.cloudcomputing.webapp.entity.Image;
import com.cloudcomputing.webapp.entity.Product;
import com.cloudcomputing.webapp.entity.User;
import com.cloudcomputing.webapp.repository.ImageRepository;
import com.cloudcomputing.webapp.repository.ProductRepository;
import com.cloudcomputing.webapp.repository.UserRepository;
import com.cloudcomputing.webapp.service.ProductService;
import com.cloudcomputing.webapp.vo.ProductDetailsVO;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    S3Configuration s3Configuration;

    AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public ResponseEntity addProduct(ProductDetailsVO productDetails, String header) {

        ProductDataDTO newProduct = new ProductDataDTO();
        Product product = new Product();

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /addProduct");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            String currentDate = String.valueOf(java.time.LocalDateTime.now());
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            String[] values = userService.authenticateUser(header);
            List<String> existingSkus = productRepository.getSkus();

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized. Only valid users can create a product in api /addProduct");
                return new ResponseEntity<>("Unauthorized. Only valid users can create a product", HttpStatus.UNAUTHORIZED);
            }

            if(productDetails.getQuantity() == null || productDetails.getSku().isBlank() ||
                    productDetails.getDescription().isBlank() || productDetails.getName().isBlank() ||
                    productDetails.getManufacturer().isBlank() || productDetails.getSku() == null ||
                    productDetails.getDescription() == null || productDetails.getName() == null ||
                    productDetails.getManufacturer() == null) {
                logger.error("Bad Request. Enter all required fields for creating in api /addProduct");
                return new ResponseEntity<>("Bad Request. Enter all required fields for creating", HttpStatus.BAD_REQUEST);
            }

            if(existingSkus.contains(productDetails.getSku())) {
                logger.error("Bad Request. Enter unique SKU in api /addProduct");
                return new ResponseEntity<>("Bad Request. Enter unique SKU", HttpStatus.BAD_REQUEST);
            }

            if(productDetails.getQuantity() < 0 || productDetails.getQuantity() > 100) {
                logger.error("Bad Request. Enter valid quantity in api /addProduct");
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

            logger.info("Product Created in api /addProduct");
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Bad Request in api /addProduct");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getProduct(Integer productId) {

        ProductDataDTO selectedProduct = new ProductDataDTO();

        try {
            Product product = productRepository.getByProductId(productId);

            if(product == null) {
                logger.error("Product not found. Enter a valid Product ID in api /getProduct");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
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

            logger.info("Get Product success in api /getProduct");
            return new ResponseEntity<>(selectedProduct, HttpStatus.OK);

        } catch(Exception e) {
            logger.error("Bad Request in api /getProduct");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity deleteProduct(Integer productId, String header) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /deleteProduct");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            String s3bucketName = s3Configuration.getBucketName();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            Product selectedProduct = productRepository.getByProductId(productId);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized. Only authorized users can delete a product in api /deleteProduct");
                return new ResponseEntity<>("Unauthorized. Only authorized users can delete a product", HttpStatus.UNAUTHORIZED);
            }

            if(selectedProduct == null) {
                logger.error("Product not found. Enter a valid Product ID in api /deleteProduct");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                logger.error("Forbidden Access. Only owners can delete a product in api /deleteProduct");
                return new ResponseEntity<>("Forbidden Access. Only owners can delete a product", HttpStatus.FORBIDDEN);
            }

            List<Image> relatedImages = imageRepository.getImageListByProduct(productId);
            if(!relatedImages.isEmpty()) {
                for (Image image : relatedImages) {
                    s3Client.deleteObject(new DeleteObjectRequest(s3bucketName, image.getS3BucketPath()));
                    imageRepository.deleteById(image.getImageId());
                }
            }

            productRepository.deleteById(productId);

            logger.info("Delete Successful in api /deleteProduct");
            return new ResponseEntity<>("Delete Successful", HttpStatus.NO_CONTENT);

        } catch(Exception e) {
            logger.error("Bad Request in api /deleteProduct");
            return new ResponseEntity<>("Bad Request" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity updateProductPut(Integer productId, String header, ProductDetailsVO productDetails) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /updateProductPut");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            Product selectedProduct = productRepository.getByProductId(productId);
            String[] values = userService.authenticateUser(header);
            List<String> existingSkus = productRepository.getSkus();

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized Access. Enter valid credentials in api /updateProductPut");
                return new ResponseEntity<>("Unauthorized Access. Enter valid credentials", HttpStatus.UNAUTHORIZED);
            }

            if(selectedProduct == null) {
                logger.error("Product not found. Enter a valid Product ID in api /updateProductPut");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                logger.error("Forbidden Access. Only owners can update products in api /updateProductPut");
                return new ResponseEntity<>("Forbidden Access. Only owners can update products", HttpStatus.FORBIDDEN);
            }

            if(productDetails.getQuantity() == null || productDetails.getSku().isBlank() ||
                    productDetails.getDescription().isBlank() || productDetails.getName().isBlank() ||
                    productDetails.getManufacturer().isBlank() || productDetails.getSku() == null ||
                    productDetails.getDescription() == null || productDetails.getName() == null ||
                    productDetails.getManufacturer() == null) {
                logger.error("Bad Request. Enter all fields for updating in api /updateProductPut");
                return new ResponseEntity<>("Bad Request. Enter all fields for updating", HttpStatus.BAD_REQUEST);
            }

            if(existingSkus.contains(productDetails.getSku())) {
                logger.error("Bad Request. Enter unique SKU in api /updateProductPut");
                return new ResponseEntity<>("Bad Request. Enter unique SKU", HttpStatus.BAD_REQUEST);
            }

            if(productDetails.getQuantity() < 0 || productDetails.getQuantity() > 100) {
                logger.error("Bad Request. Enter valid quantity in api /updateProductPut");
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

            logger.info("Product Updated in api /updateProductPut");
            return new ResponseEntity<>("Product Updated", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            logger.error("Bad Request in api /updateProductPut");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity updateProductPatch(Integer productId, String header, ProductDetailsVO productDetails) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /updateProductPatch");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            Product selectedProduct = productRepository.getByProductId(productId);
            String[] values = userService.authenticateUser(header);
            List<String> existingSkus = productRepository.getSkus();

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized Access. Enter valid credentials in api /updateProductPatch");
                return new ResponseEntity<>("Unauthorized Access. Enter valid credentials", HttpStatus.UNAUTHORIZED);
            }

            if(selectedProduct == null) {
                logger.error("Product not found. Enter a valid Product ID in api /updateProductPatch");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                logger.error("Forbidden Access. Only owners can update products in api /updateProductPatch");
                return new ResponseEntity<>("Forbidden Access. Only owners can update products", HttpStatus.FORBIDDEN);
            }

            if(existingSkus.contains(productDetails.getSku())) {
                logger.error("Bad Request. Enter unique SKU in api /updateProductPatch");
                return new ResponseEntity<>("Bad Request. Enter unique SKU", HttpStatus.BAD_REQUEST);
            }

            if(productDetails.getQuantity() != null && (productDetails.getQuantity() < 0 || productDetails.getQuantity() > 100)) {
                logger.error("Bad Request. Enter valid quantity in api /updateProductPatch");
                return new ResponseEntity<>("Bad Request. Enter valid quantity", HttpStatus.BAD_REQUEST);
            }

            String updateDate = String.valueOf(java.time.LocalDateTime.now());

            if(productDetails.getName() != null) {
                selectedProduct.setName(productDetails.getName());
            }
            if(productDetails.getSku() != null) {
                selectedProduct.setSku(productDetails.getSku());
            }
            if(productDetails.getDescription() != null) {
                selectedProduct.setDescription(productDetails.getDescription());
            }
            if(productDetails.getManufacturer() != null) {
                selectedProduct.setManufacturer(productDetails.getManufacturer());
            }
            if(productDetails.getQuantity() != null) {
                selectedProduct.setQuantity(productDetails.getQuantity());
            }
            selectedProduct.setDateLastUpdated(updateDate);
            productRepository.save(selectedProduct);

            logger.info("Product Updated in api /updateProductPatch");
            return new ResponseEntity<>("Product Updated", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            logger.error("Bad Request in api /updateProductPatch");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity uploadImage(Integer productId, String header, MultipartFile productImage) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /uploadImage");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            String s3bucketName = s3Configuration.getBucketName();
            String currentDate = String.valueOf(java.time.LocalDateTime.now());

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized. Only valid users can upload an image in api /uploadImage");
                return new ResponseEntity<>("Unauthorized. Only valid users can upload an image", HttpStatus.UNAUTHORIZED);
            }

            ImageDataDTO imageDetails = new ImageDataDTO();
            Image image = new Image();

            String contentType = new Tika().detect(productImage.getInputStream());
            if(!contentType.startsWith("image/")) {
                logger.error("Invalid image type in api /uploadImage");
                return new ResponseEntity<>("Invalid image type", HttpStatus.BAD_REQUEST);
            }

            String fileName = UUID.randomUUID().toString() + "_" + productImage.getOriginalFilename();
            String path = authUser.getId() + "/" + productId + "/" + fileName;
            List<String> paths = imageRepository.getPaths();

            image.setProductId(productId);
            image.setFileName(fileName);
            image.setDateCreated(currentDate);
            image.setS3BucketPath(path);
            imageRepository.save(image);

            Image uplodedImage = imageRepository.getByPath(path);

            InputStream inputStream = productImage.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(productImage.getSize());
            metadata.setContentType(productImage.getContentType());
            metadata.addUserMetadata("id", String.valueOf(productId));
            metadata.addUserMetadata("fileName", fileName);
            metadata.addUserMetadata("path", path);

            PutObjectRequest putObjectRequest = new PutObjectRequest(s3bucketName, path, inputStream, metadata);
            s3Client.putObject(putObjectRequest);

            imageDetails.setImageId(uplodedImage.getImageId());
            imageDetails.setProductId(uplodedImage.getProductId());
            imageDetails.setFileName(uplodedImage.getFileName());
            imageDetails.setDateCreated(uplodedImage.getDateCreated());
            imageDetails.setS3BucketPath(uplodedImage.getS3BucketPath());

            logger.info("Image Created in api /uploadImage");
            return new ResponseEntity<>(imageDetails, HttpStatus.CREATED);

        } catch(Exception e) {
            logger.error("Bad Request in api /uploadImage");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity fetchImageList(Integer productId, String header) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /fetchImageList");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if(authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized. Only valid users can view product images in api /fetchImageList");
                return new ResponseEntity<>("Unauthorized. Only valid users can view product images", HttpStatus.UNAUTHORIZED);
            }

            Product selectedProduct = productRepository.getByProductId(productId);
            if(selectedProduct == null) {
                logger.error("Product not found. Enter a valid Product ID in api /fetchImageList");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if(!loggedUser.getUsername().equals(values[0])) {
                logger.error("Forbidden Access. Only owners can view product images in api /fetchImageList");
                return new ResponseEntity<>("Forbidden Access. Only owners can view product images", HttpStatus.FORBIDDEN);
            }

            List<Image> imageList = imageRepository.getImageListByProduct(productId);
            logger.info("Fetch Image List success in api /fetchImageList");
            return new ResponseEntity<>(imageList, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Bad Request in api /fetchImageList");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity fetchImageDetails(Integer productId, Integer imageId, String header) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /fetchImageDetails");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if (authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized. Only valid users can view image details in api /fetchImageDetails");
                return new ResponseEntity<>("Unauthorized. Only valid users can view image details", HttpStatus.UNAUTHORIZED);
            }

            Product selectedProduct = productRepository.getByProductId(productId);
            if (selectedProduct == null) {
                logger.error("Product not found. Enter a valid Product ID in api /fetchImageDetails");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
            }

            Image selectedImage = imageRepository.getByImageId(imageId);
            if (selectedImage == null) {
                logger.error("Image not found. Enter a valid Image ID in api /fetchImageDetails");
                return new ResponseEntity<>("Image not found. Enter a valid Image ID", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if (!loggedUser.getUsername().equals(values[0])) {
                logger.error("Forbidden Access. Only owners can view image details in api /fetchImageDetails");
                return new ResponseEntity<>("Forbidden Access. Only owners can view image details", HttpStatus.FORBIDDEN);
            }

            if(!selectedImage.getProductId().equals(productId)) {
                logger.error("Product and image id not related in api /fetchImageDetails");
                return new ResponseEntity<>("Product and image id not related", HttpStatus.FORBIDDEN);
            }

            logger.info("Fetch Image Details success in api /fetchImageDetails");
            return new ResponseEntity<>(selectedImage, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Bad Request in api /fetchImageDetails");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity deleteImage(Integer productId, Integer imageId, String header) {

        try {

            if(header == null || header.isBlank()) {
                logger.error("Unauthorized Access. Provide credentials in api /deleteImage");
                return new ResponseEntity<>("Unauthorized Access. Provide credentials", HttpStatus.UNAUTHORIZED);
            }

            String s3bucketName = s3Configuration.getBucketName();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            String[] values = userService.authenticateUser(header);

            User authUser = userRepository.getByUsername(values[0]);
            if (authUser == null || !encoder.matches(values[1], authUser.getPassword())) {
                logger.error("Unauthorized. Only valid users can delete a product in api /deleteImage");
                return new ResponseEntity<>("Unauthorized. Only valid users can delete a product", HttpStatus.UNAUTHORIZED);
            }

            Product selectedProduct = productRepository.getByProductId(productId);
            if (selectedProduct == null) {
                logger.error("Product not found. Enter a valid Product ID in api /deleteImage");
                return new ResponseEntity<>("Product not found. Enter a valid Product ID", HttpStatus.NOT_FOUND);
            }

            Image selectedImage = imageRepository.getByImageId(imageId);
            if (selectedImage == null) {
                logger.error("Image not found. Enter a valid Image ID in api /deleteImage");
                return new ResponseEntity<>("Image not found. Enter a valid Image ID", HttpStatus.NOT_FOUND);
            }

            User loggedUser = userRepository.getByUserId(selectedProduct.getOwnerUserId());
            if (!loggedUser.getUsername().equals(values[0])) {
                logger.error("Forbidden Access. Only owners can delete images in api /deleteImage");
                return new ResponseEntity<>("Forbidden Access. Only owners can delete images", HttpStatus.FORBIDDEN);
            }

            if (!selectedImage.getProductId().equals(productId)) {
                logger.error("Product and image id not related in api /deleteImage");
                return new ResponseEntity<>("Product and image id not related", HttpStatus.FORBIDDEN);
            }

            s3Client.deleteObject(new DeleteObjectRequest(s3bucketName, selectedImage.getS3BucketPath()));

            imageRepository.deleteById(imageId);

            logger.info("Delete success in api /deleteImage");
            return new ResponseEntity<>("Deleted Successfully", HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            logger.error("Bad Request in api /deleteImage");
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
    }
}
