package com.cloudvault.controller;

import com.cloudvault.model.FileMetadata;
import com.cloudvault.service.DynamoDBService;
import com.cloudvault.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin("*")
public class FileController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private DynamoDBService dynamoDBService;

    // UPLOAD
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("userId") String userId) throws Exception {

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Upload to S3
        s3Service.uploadFile(fileName, file.getBytes());

        // Save metadata to DynamoDB
        FileMetadata metadata = new FileMetadata();
        metadata.setUserId(userId);
        metadata.setImageId(fileName);
        metadata.setUploadTime(LocalDateTime.now().toString());
        metadata.setFileUrl("");

        dynamoDBService.saveFile(metadata);

        return "Upload successful";
    }

    // GET FILES
    @GetMapping("/{userId}")
    public List<FileMetadata> getFiles(@PathVariable String userId) {

        List<FileMetadata> files = dynamoDBService.getFilesByUserId(userId);

        for (FileMetadata file : files) {
            String url = s3Service.generatePresignedUrl(file.getImageId());
            file.setFileUrl(url);
        }

        return files;
    }

    // DELETE
    @DeleteMapping("/delete")
    public String deleteFile(@RequestParam String fileName,
                             @RequestParam String userId) {

        s3Service.deleteFile(fileName);
        dynamoDBService.deleteFile(userId, fileName);

        return "Deleted";
    }
}