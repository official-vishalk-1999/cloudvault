package com.cloudvault.controller;

import com.cloudvault.model.FileMetadata;
import com.cloudvault.model.User;
import com.cloudvault.service.DynamoDBService;
import com.cloudvault.service.UserService;
import com.cloudvault.service.S3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private DynamoDBService dynamoDBService;

    @Autowired
    private S3Service s3Service;

    // GET USERS
    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {

        List<User> users = userService.getAllUsers();
        List<FileMetadata> files = dynamoDBService.getAllFiles();

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {

            long count = files.stream()
                    .filter(f -> f.getUserId().equals(user.getEmail()))
                    .count();

            Map<String, Object> map = new HashMap<>();
            map.put("name", user.getName());
            map.put("email", user.getEmail());
            map.put("enabled", user.getEnabled());
            map.put("imageCount", count);

            result.add(map);
        }

        return result;
    }

    // TOGGLE USER
    @PostMapping("/toggle")
    public void toggleUser(@RequestParam String email,
                           @RequestParam String enabled) {
        userService.updateUserStatus(email, enabled);
    }

    // DELETE USER WITH FULL CLEANUP
    @DeleteMapping("/delete")
    public void deleteUser(@RequestParam String email) {

        // 1️⃣ Get all files of user
        List<FileMetadata> files = dynamoDBService.getFilesByUserId(email);

        // 2️⃣ Delete each file from S3 + DynamoDB
        for (FileMetadata file : files) {

            String imageId = file.getImageId();

            // delete from S3
            s3Service.deleteFile(imageId);

            // delete from DynamoDB
            dynamoDBService.deleteFile(email, imageId);
        }

        // 3️⃣ Delete user
        userService.deleteUser(email);
    }
}