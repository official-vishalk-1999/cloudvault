package com.cloudvault.controller;

import com.cloudvault.model.User;
import com.cloudvault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User request) {

        User user = userService.getUser(request.getEmail());

        if (user == null) return "User not found";

        if (!userService.checkPassword(request.getPassword(), user.getPassword()))
            return "Invalid password";

        if ("false".equals(user.getEnabled()))
            return "User disabled";

        return "Login successful";
    }
}