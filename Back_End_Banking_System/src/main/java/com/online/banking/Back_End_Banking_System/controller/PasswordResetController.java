package com.online.banking.Back_End_Banking_System.controller;

import com.online.banking.Back_End_Banking_System.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    // Handle Forgot Password form submission
    @PostMapping("/forgot-password")
    public ResponseEntity<?> handleForgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            passwordResetService.generateAndSendResetToken(email);
            return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email address not found."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An error occurred while sending the email."));
        }
    }

    // Handle Reset Password request
    @PostMapping("/reset-password")
    public ResponseEntity<?> handleResetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (passwordResetService.validateToken(token)) {
            passwordResetService.updatePassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password successfully reset."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token."));
        }
    }
}
