package com.online.banking.Back_End_Banking_System.controller;

import com.online.banking.Back_End_Banking_System.dto.LoginRequest;
import com.online.banking.Back_End_Banking_System.dto.ResetPasswordRequest;
import com.online.banking.Back_End_Banking_System.dto.SignupRequest;
import com.online.banking.Back_End_Banking_System.entity.Role;
import com.online.banking.Back_End_Banking_System.entity.User;
import com.online.banking.Back_End_Banking_System.exception.EmailServiceException;
import com.online.banking.Back_End_Banking_System.repository.UserRepository;
import com.online.banking.Back_End_Banking_System.service.EmailService;
import com.online.banking.Back_End_Banking_System.service.UserService;
import com.online.banking.Back_End_Banking_System.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest) {
        try {
            User user = userService.signup(signupRequest);
            return ResponseEntity.ok("User registered successfully with username: " + user.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Handle multiple roles
            Set<Role> roles = user.getRoles();
            String rolesString = roles.stream()
                    .map(Role::name) // Convert each Role enum to its name
                    .collect(Collectors.joining(", ")); // Combine roles into a single string

            // Generate JWT token with roles
            String token = jwtUtil.generateToken(user.getUsername(), rolesString);

            // Create response
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("roles", rolesString);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

}
