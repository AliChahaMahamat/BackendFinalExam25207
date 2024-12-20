package com.online.banking.Back_End_Banking_System.service;

import com.online.banking.Back_End_Banking_System.entity.PasswordResetToken;
import com.online.banking.Back_End_Banking_System.entity.User;
import com.online.banking.Back_End_Banking_System.repository.PasswordResetTokenRepository;
import com.online.banking.Back_End_Banking_System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    public void generateAndSendResetToken(String email) {
        // Retrieve the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate a token
        String token = UUID.randomUUID().toString();

        // Save the token to the database
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setToken(token);
        resetToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1-hour expiration

        tokenRepository.save(resetToken);

        // Send the reset email
        String resetUrl = "http://localhost:8081/reset-password?token=" + token;
        emailService.sendSimpleEmail(user.getEmail(), "Password Reset Request",
                "To reset your password, click the link below:\n" + resetUrl);
    }

    public boolean validateToken(String token) {
        // Find the token in the database
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        // Check if the token is valid and not expired
        return resetToken.isPresent() && !resetToken.get().isExpired();
    }

    public void updatePassword(String token, String newPassword) {
        // Retrieve the token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        // Retrieve the user by ID
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update the user's password
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));

        userRepository.save(user);

        // Delete the token after successful password reset
        tokenRepository.delete(resetToken);
    }
}
