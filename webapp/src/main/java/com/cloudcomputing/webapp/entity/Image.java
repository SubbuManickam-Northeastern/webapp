package com.cloudcomputing.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer imageId;
    Integer productId;
    String fileName;
    String dateCreated;
    @Column(name = "s3_bucket_path")
    String s3BucketPath;
}
