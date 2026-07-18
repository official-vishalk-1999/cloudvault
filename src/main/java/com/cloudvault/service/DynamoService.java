package com.cloudvault.service;

import com.cloudvault.model.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.*;

@Service
public class DynamoService {

    @Value("${aws.dynamodb.images-table}")
    private String table;

    private final DynamoDbClient db;

    public DynamoService(DynamoDbClient db) {
        this.db = db;
    }

    public void save(String userId, String imageId, String uploadTime) {
        db.putItem(PutItemRequest.builder().tableName(table).item(Map.of(
                "userId", str(userId),
                "imageId", str(imageId),
                "uploadTime", str(uploadTime))).build());
    }

    public List<FileMetadata> findByUser(String userId) {
        QueryResponse res = db.query(QueryRequest.builder().tableName(table)
                .keyConditionExpression("userId = :uid")
                .expressionAttributeValues(Map.of(":uid", str(userId))).build());
        List<FileMetadata> files = new ArrayList<>();
        for (Map<String, AttributeValue> item : res.items())
            files.add(new FileMetadata(item.get("userId").s(), item.get("imageId").s(),
                    item.get("uploadTime").s(), null));
        return files;
    }

    private AttributeValue str(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
