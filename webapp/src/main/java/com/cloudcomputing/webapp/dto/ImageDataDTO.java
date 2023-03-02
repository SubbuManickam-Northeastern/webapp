package com.cloudcomputing.webapp.dto;

import lombok.Data;

@Data
public class ImageDataDTO {

    Integer imageId;
    Integer productId;
    String fileName;
    String dateCreated;
    String s3BucketPath;
}
