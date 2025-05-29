package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    // In-memory storage for demo purposes
    private static final User ADMIN_USER = new User(
        "salma@gmail.com",
        "salma",
        "admin"
    );

    public User findByEmail(String email) {
        return ADMIN_USER.getEmail().equals(email) ? ADMIN_USER : null;
    }
}
