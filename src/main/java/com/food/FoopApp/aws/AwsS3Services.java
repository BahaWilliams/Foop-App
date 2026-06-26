package com.food.FoopApp.aws;

import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

public interface AwsS3Services {
    URL uploadFile(String keyName, MultipartFile file);
    void delete(String keyName);
}
