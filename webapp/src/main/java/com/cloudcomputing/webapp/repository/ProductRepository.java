package com.cloudcomputing.webapp.repository;

import com.cloudcomputing.webapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("select p.sku from Product p")
    List<String> getSkus();

    @Query("select p from Product p where p.sku = :sku")
    Product getBySku(@Param("sku") String sku);

    @Query("select p from Product p where p.id = :productId")
    Product getByProductId(@Param("productId") Integer productId);

}
