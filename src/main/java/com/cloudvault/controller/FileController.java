package com.cloudvault.controller;

import com.cloudvault.model.FileMetadata;
import com.cloudvault.service.DynamoService;
import com.cloudvault.service.S3Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3;
    private final DynamoService dynamo;

    public FileController(S3Service s3, DynamoService dynamo) {
        this.s3 = s3;
        this.dynamo = dynamo;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("userId") String userId) throws Exception {
        String type = file.getContentType();
        if (!"image/jpeg".equals(type) && !"image/png".equals(type))
            return "Only JPG, JPEG and PNG files are allowed";
        String imageId = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3.upload(imageId, file.getBytes());
        dynamo.save(userId, imageId, LocalDateTime.now().toString());
        return "Upload successful";
    }

    @GetMapping("/{userId}")
    public List<FileMetadata> list(@PathVariable String userId) {
        return dynamo.findByUser(userId).stream()
                .map(f -> new FileMetadata(f.userId(), f.imageId(), f.uploadTime(),
                        s3.presignedUrl(f.imageId())))
                .toList();
    }
}
