package com.cloudcomputing.webapp.repository;

import com.cloudcomputing.webapp.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("select i.s3BucketPath from Image i")
    List<String> getPaths();

    @Query("select i from Image i where i.s3BucketPath = :s3BucketPath")
    Image getByPath(@Param("s3BucketPath") String s3BucketPath);

    @Query("select i from Image i where i.productId = :productId")
    List<Image> getImageListByProduct(@Param(("productId")) Integer productId);

    @Query("select i from Image i where i.imageId = :imageId")
    Image getByImageId(@Param("imageId") Integer imageId);

}
