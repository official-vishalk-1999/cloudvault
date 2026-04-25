package com.cloudvault.service;

import com.cloudvault.model.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.regions.Region;

import java.util.*;

@Service
public class DynamoDBService {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

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

    // SAVE FILE
    public void saveFile(FileMetadata file) {

        DynamoDbClient client = getClient();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("userId", AttributeValue.builder().s(file.getUserId()).build());
        item.put("imageId", AttributeValue.builder().s(file.getImageId()).build());
        item.put("uploadTime", AttributeValue.builder().s(file.getUploadTime()).build());
        item.put("fileUrl", AttributeValue.builder().s(file.getFileUrl()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("Images")
                .item(item)
                .build();

        client.putItem(request);
    }

    // GET FILES
    public List<FileMetadata> getFilesByUserId(String userId) {

        DynamoDbClient client = getClient();

        Map<String, String> names = new HashMap<>();
        names.put("#uid", "userId");

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":uid", AttributeValue.builder().s(userId).build());

        QueryRequest request = QueryRequest.builder()
                .tableName("Images")
                .keyConditionExpression("#uid = :uid")
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();

        QueryResponse response = client.query(request);

        List<FileMetadata> files = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            FileMetadata file = new FileMetadata();
            file.setUserId(item.get("userId").s());
            file.setImageId(item.get("imageId").s());
            file.setUploadTime(item.get("uploadTime").s());
            file.setFileUrl(item.get("fileUrl").s());
            files.add(file);
        }

        return files;
    }

    // DELETE
    public void deleteFile(String userId, String imageId) {

        DynamoDbClient client = getClient();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("userId", AttributeValue.builder().s(userId).build());
        key.put("imageId", AttributeValue.builder().s(imageId).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName("Images")
                .key(key)
                .build();

        client.deleteItem(request);
    }

    // ADMIN
    public List<FileMetadata> getAllFiles() {

        DynamoDbClient client = getClient();

        ScanRequest request = ScanRequest.builder()
                .tableName("Images")
                .build();

        ScanResponse response = client.scan(request);

        List<FileMetadata> files = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            FileMetadata file = new FileMetadata();
            file.setUserId(item.get("userId").s());
            file.setImageId(item.get("imageId").s());
            file.setUploadTime(item.get("uploadTime").s());
            file.setFileUrl(item.get("fileUrl").s());
            files.add(file);
        }

        return files;
    }
}