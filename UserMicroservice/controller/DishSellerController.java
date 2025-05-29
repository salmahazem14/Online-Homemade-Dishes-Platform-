package com.example.demo.controller;

import com.example.demo.model.DishSeller;
import com.example.demo.service.DishSellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish-sellers")
public class DishSellerController {

    @Autowired
    private DishSellerService dishSellerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDishSeller(@RequestBody DishSeller dishSeller) {
        try {
            DishSeller registeredDishSeller = dishSellerService.registerDishSeller(dishSeller);
            return ResponseEntity.ok(registeredDishSeller);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginDishSeller(@RequestBody LoginRequest loginRequest) {
        try {
            DishSeller authenticatedDishSeller = dishSellerService.login(
                    loginRequest.getCompanyName(),
                    loginRequest.getPassword()
            );
            return ResponseEntity.ok(authenticatedDishSeller);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/getAllDishSellers")
    public List<DishSeller> getAllDishSellers() {
        return dishSellerService.getAllDishSellers();
    }

    public static class LoginRequest {
        private String companyName;
        private String password;

        // getters and setters
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}