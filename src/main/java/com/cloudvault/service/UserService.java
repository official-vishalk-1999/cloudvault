package com.cloudvault.service;

import com.cloudvault.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.regions.Region;

import java.util.*;

@Service
public class UserService {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private DynamoDbClient getClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    // CHECK IF USER EXISTS
    public boolean userExists(String email) {
        return getUser(email) != null;
    }

    // SAVE USER (WITH DUPLICATE CHECK)
    public String saveUser(User user) {

        if (userExists(user.getEmail())) {
            return "User already exists";
        }

        DynamoDbClient client = getClient();

        String hashedPassword = passwordEncoder.encode(user.getPassword());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("email", AttributeValue.builder().s(user.getEmail()).build());
        item.put("name", AttributeValue.builder().s(user.getName()).build());
        item.put("password", AttributeValue.builder().s(hashedPassword).build());
        item.put("enabled", AttributeValue.builder().s("true").build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("Users")
                .item(item)
                .build();

        client.putItem(request);

        return "Signup successful";
    }

    // GET USER
    public User getUser(String email) {

        DynamoDbClient client = getClient();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("email", AttributeValue.builder().s(email).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName("Users")
                .key(key)
                .build();

        Map<String, AttributeValue> item = client.getItem(request).item();

        if (item == null || item.isEmpty()) return null;

        User user = new User();
        user.setEmail(item.get("email").s());
        user.setName(item.get("name").s());
        user.setPassword(item.get("password").s());
        user.setEnabled(item.getOrDefault("enabled",
                AttributeValue.builder().s("true").build()).s());

        return user;
    }

    // PASSWORD CHECK
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    // GET ALL USERS
    public List<User> getAllUsers() {

        DynamoDbClient client = getClient();

        ScanRequest request = ScanRequest.builder()
                .tableName("Users")
                .build();

        ScanResponse response = client.scan(request);

        List<User> users = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            User user = new User();
            user.setEmail(item.get("email").s());
            user.setName(item.get("name").s());
            user.setPassword(item.get("password").s());
            user.setEnabled(item.getOrDefault("enabled",
                    AttributeValue.builder().s("true").build()).s());
            users.add(user);
        }

        return users;
    }

    // ENABLE / DISABLE
    public void updateUserStatus(String email, String enabled) {

        DynamoDbClient client = getClient();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("email", AttributeValue.builder().s(email).build());

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":val", AttributeValue.builder().s(enabled).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName("Users")
                .key(key)
                .updateExpression("SET enabled = :val")
                .expressionAttributeValues(values)
                .build();

        client.updateItem(request);
    }

    // DELETE USER
    public void deleteUser(String email) {

        DynamoDbClient client = getClient();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("email", AttributeValue.builder().s(email).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName("Users")
                .key(key)
                .build();

        client.deleteItem(request);
    }
}