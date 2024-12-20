package com.online.banking.Back_End_Banking_System.service;

import com.online.banking.Back_End_Banking_System.dto.SignupRequest;
import com.online.banking.Back_End_Banking_System.dto.ResetPasswordRequest;
import com.online.banking.Back_End_Banking_System.entity.Account;
import com.online.banking.Back_End_Banking_System.entity.PasswordResetToken;
import com.online.banking.Back_End_Banking_System.entity.Role;
import com.online.banking.Back_End_Banking_System.entity.User;
import com.online.banking.Back_End_Banking_System.exception.ResourceNotFoundException;
import com.online.banking.Back_End_Banking_System.repository.AccountRepository;
import com.online.banking.Back_End_Banking_System.repository.PasswordResetTokenRepository;
import com.online.banking.Back_End_Banking_System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Fetch user details by ID
    public User getUserDetails(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Signup new user
    public User signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setIdType(request.getIdType());
        user.setIdNumber(request.getIdNumber());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Role.ROLE_USER);  // Default role
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Create and save user account
        Account account = new Account();
        account.setAccountType(request.getAccountType());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(0.0);  // Initial balance
        account.setUser(savedUser);
        accountRepository.save(account);

        return savedUser;
    }

    // Helper method to generate a random account number
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

    // Get all users with their accounts
    public List<User> getAllUsersWithAccounts() {
        return userRepository.findAllUsersWithAccounts();
    }

    // Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    // Save password reset token for user
    public void savePasswordResetToken(Long userId, String token) {
        PasswordResetToken resetToken = new PasswordResetToken(userId, token,
                new Date(System.currentTimeMillis() + 3600000)); // Token valid for 1 hour
        passwordResetTokenRepository.save(resetToken);
    }

    // Validate the reset token and check if expired
    public Long validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken.isEmpty() || passwordResetToken.get().isExpired()) {
            return null; // Invalid or expired token
        }
        return passwordResetToken.get().getUserId();  // Return user ID if token is valid
    }

    // Update user password after successful token validation
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
