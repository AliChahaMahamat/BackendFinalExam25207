package com.online.banking.Back_End_Banking_System.controller;

import com.online.banking.Back_End_Banking_System.dto.UserAccountDTO;

import com.online.banking.Back_End_Banking_System.entity.Account;
import com.online.banking.Back_End_Banking_System.entity.Role;
import com.online.banking.Back_End_Banking_System.entity.User;
import com.online.banking.Back_End_Banking_System.repository.AccountRepository;
import com.online.banking.Back_End_Banking_System.repository.UserRepository;
import com.online.banking.Back_End_Banking_System.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @GetMapping("/users-with-accounts")
    public List<UserAccountDTO> getAllUsersWithAccounts() {
        logger.info("Fetching all users with accounts");

        List<User> users = userRepository.findAll();
        logger.debug("Number of users retrieved: {}", users.size());

        List<UserAccountDTO> userAccountDTOs = new ArrayList<>();
        for (User user : users) {
            List<UserAccountDTO.AccountInfo> accountInfos = new ArrayList<>();
            for (Account account : user.getAccounts()) {
                accountInfos.add(new UserAccountDTO.AccountInfo(account.getAccountType(), account.getAccountNumber(), account.getBalance()));
                logger.debug("Added account info for user ID {}: {}", user.getId(), account);
            }

            Set<Role> roles = user.getRoles();
            logger.debug("Roles for user ID {}: {}", user.getId(), roles);

            userAccountDTOs.add(new UserAccountDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), roles, user.getPhoneNumber(), accountInfos));
            logger.info("Processed user ID {}: {}", user.getId(), user.getUsername());
        }

        logger.info("Successfully processed all users with accounts");
        return userAccountDTOs;
    }

    @DeleteMapping("/accounts/{accountNumber}")
    public ResponseEntity<String> deleteAccount(@PathVariable String accountNumber) {
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);

        if (account.isPresent()) {
            accountRepository.delete(account.get());
            return ResponseEntity.ok("Account deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found.");
        }
    }



    @PutMapping("/users/{userId}")
    public ResponseEntity<String> updateUserDetails(@PathVariable Long userId, @RequestBody UserAccountDTO userAccountDTO) {
        logger.info("Updating user with ID: {}", userId);
        logger.debug("Payload: {}", userAccountDTO);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update basic user fields
            user.setUsername(userAccountDTO.getUsername());
            user.setFullName(userAccountDTO.getFullName());
            user.setEmail(userAccountDTO.getEmail());
            user.setPhoneNumber(userAccountDTO.getPhoneNumber());

            // Update roles
            Set<Role> updatedRoles = Arrays.stream(userAccountDTO.getRoles().split(", "))
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
            user.setRoles(updatedRoles);

            // Update accounts
            List<Account> accounts = user.getAccounts();
            List<UserAccountDTO.AccountInfo> accountInfos = userAccountDTO.getAccounts();

            // Iterate through the DTO's account list to update or add accounts
            for (UserAccountDTO.AccountInfo accountInfo : accountInfos) {
                Account account = accounts.stream()
                        .filter(a -> a.getAccountNumber().equals(accountInfo.getAccountNumber()))
                        .findFirst()
                        .orElse(new Account()); // Create new account if not found

                account.setAccountType(accountInfo.getAccountType());
                account.setBalance(accountInfo.getBalance());
                account.setUser(user); // Ensure the account is linked to the correct user

                if (!accounts.contains(account)) {
                    accounts.add(account); // Add new accounts
                }
            }

            // Remove accounts that are no longer in the DTO
            accounts.removeIf(a -> accountInfos.stream().noneMatch(ai -> ai.getAccountNumber().equals(a.getAccountNumber())));

            userRepository.save(user); // Save updated user with accounts
            logger.info("User with ID: {} updated successfully", userId);

            return ResponseEntity.ok("User updated successfully");
        } catch (Exception e) {
            logger.error("Error updating user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user: " + e.getMessage());
        }
    }

    //add new users
    @PostMapping("/add-user")
    public ResponseEntity<String> addUser(@RequestBody UserAccountDTO userAccountDTO) {
        try {
            // Validate required fields
            if (userAccountDTO.getUsername() == null || userAccountDTO.getEmail() == null) {
                return ResponseEntity.badRequest().body("Username and email are required.");
            }

            // Create a new User entity
            User user = new User();
            user.setUsername(userAccountDTO.getUsername());
            user.setFullName(userAccountDTO.getFullName());
            user.setEmail(userAccountDTO.getEmail());
            user.setPhoneNumber(userAccountDTO.getPhoneNumber());
            user.setRoles(Set.of(Role.valueOf(userAccountDTO.getRoles()))); // Convert string to Role enum
            user.setPassword("defaultPassword"); // Example: Set a default password
            userRepository.save(user);

            // Add accounts if provided
            if (userAccountDTO.getAccounts() != null) {
                for (UserAccountDTO.AccountInfo accountInfo : userAccountDTO.getAccounts()) {
                    Account account = new Account();
                    account.setAccountType(accountInfo.getAccountType());
                    account.setAccountNumber(accountInfo.getAccountNumber());
                    account.setBalance(accountInfo.getBalance());
                    account.setUser(user);
                    accountRepository.save(account);
                }
            }

            return ResponseEntity.ok("User added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add user: " + e.getMessage());
        }
    }

    @GetMapping("/download-users")
    public ResponseEntity<byte[]> downloadUsers() {
        try {
            List<User> users = userService.getAllUsersWithAccounts(); // Fetch all users with accounts
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);

            // Write CSV headers
            writer.write("Username,Full Name,Email,Phone Number,Role,Account Type,Account Number,Balance\n");

            // Write CSV rows
            for (User user : users) {
                for (Account account : user.getAccounts()) {
                    writer.write(String.format(
                            "%s,%s,%s,%s,%s,%s,%s,%.2f\n",
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getRoles(),
                            account.getAccountType(),
                            account.getAccountNumber(),
                            account.getBalance()
                    ));
                }
            }

            writer.flush();
            writer.close();

            byte[] fileBytes = out.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_data.csv");

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating CSV", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
