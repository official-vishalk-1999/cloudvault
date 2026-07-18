package com.cloudvault.controller;

import com.cloudvault.model.User;
import com.cloudvault.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        return users.signup(user) ? "Signup successful" : "User already exists";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return users.login(user.email(), user.password()) ? "Login successful" : "Invalid credentials";
    }
}
