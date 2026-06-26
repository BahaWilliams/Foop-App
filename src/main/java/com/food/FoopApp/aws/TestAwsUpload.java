//package com.food.FoopApp.aws;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.net.URL;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/upload")
//public class TestAwsUpload {
//    private final AwsS3Services awsS3Services;
//
//    @PostMapping
//    public ResponseEntity<String> upload(@RequestParam("file")MultipartFile file,
//                                         @RequestParam("keyName")String keyName){
//        URL saveFile = awsS3Services.uploadFile(keyName, file);
//        return ResponseEntity.ok("Status saved: " + saveFile.toString());
//    }
//}
