package com.example.easytrade.controller;

import com.example.easytrade.model.Session; // Import Session model
import com.example.easytrade.model.User;
import com.example.easytrade.service.SessionService; // Import SessionService
import com.example.easytrade.service.UserService;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest for IP/User-Agent
import org.springframework.beans.factory.annotation.Autowired; // Ensure Autowired is imported
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SessionService sessionService; // Inject SessionService

    @Autowired // Constructor injection for both services
    public AuthController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User newUser = new User();
            newUser.setName(registerRequest.getName());
            newUser.setSurname(registerRequest.getSurname());
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(registerRequest.getPassword());
            newUser.setEmail(registerRequest.getEmail());

            User savedUser = userService.registerUser(newUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("username", savedUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) { // Catch specific known exceptions first
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) { // Catch other runtime exceptions from service layer
             Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            // Consider a different status if it's a server-side issue vs. bad request
            return ResponseEntity.badRequest().body(errorResponse); 
        } catch (Exception e) { // Generic catch-all
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred during registration.");
            // Log the full exception server-side for debugging
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/login")
    // Add HttpServletRequest request to capture IP and User-Agent
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            User user = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
            
            // Record the login session after successful user authentication
            Session session = sessionService.recordLoginSession(user, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", user.getUsername());
            response.put("userId", user.getId());
            // Include the session token in the response.
            // The client (Next.js app) will need to store this for subsequent authenticated requests.
            response.put("token", session.getSessionToken()); // Changed key to "token" for common practice
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) { // Specific for bad input from user
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) { // For issues like "User not found" or "Invalid credentials"
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse); // Unauthorized
        } catch (Exception e) { // Generic catch-all
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred during login.");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.toLowerCase().startsWith("bearer ")) {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            sessionService.invalidateSession(token);
            return ResponseEntity.ok(Map.of("message", "Logout successful. Session invalidated."));
        }
        // If no token provided, maybe the client is just clearing its local state.
        return ResponseEntity.ok(Map.of("message", "Logout processed. No session token provided for server-side invalidation."));
    }

    // Static inner classes for request DTOs
    public static class RegisterRequest {
        private String name;
        private String surname;
        private String username;
        private String password;
        private String email;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSurname() { return surname; }
        public void setSurname(String surname) { this.surname = surname; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class LoginRequest {
        private String username;
        private String password;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}