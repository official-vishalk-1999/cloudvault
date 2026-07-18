package com.cloudvault.service;

import com.cloudvault.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.Map;

@Service
public class UserService {

    @Value("${aws.dynamodb.users-table}")
    private String table;

    private final DynamoDbClient db;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(DynamoDbClient db) {
        this.db = db;
    }

    public boolean signup(User user) {
        if (find(user.email()) != null) return false;
        db.putItem(PutItemRequest.builder().tableName(table).item(Map.of(
                "email", str(user.email()),
                "name", str(user.name()),
                "password", str(encoder.encode(user.password())))).build());
        return true;
    }

    public boolean login(String email, String password) {
        User user = find(email);
        return user != null && encoder.matches(password, user.password());
    }

    private User find(String email) {
        Map<String, AttributeValue> item = db.getItem(GetItemRequest.builder()
                .tableName(table).key(Map.of("email", str(email))).build()).item();
        return item.isEmpty() ? null : new User(email, null, item.get("password").s());
    }

    private AttributeValue str(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
