package com.online.banking.Back_End_Banking_System.dto;

import com.online.banking.Back_End_Banking_System.entity.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserAccountDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String roles; // Updated to support multiple roles
    private String phoneNumber;
    private List<AccountInfo> accounts;

    public UserAccountDTO(Long id, String username, String fullName, String email, Set<Role> roles, String phoneNumber, List<AccountInfo> accounts) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles.stream().map(Role::name).collect(Collectors.joining(", "));
        this.phoneNumber = phoneNumber;
        this.accounts = accounts;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<AccountInfo> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountInfo> accounts) {
        this.accounts = accounts;
    }

    // Nested AccountInfo Class
    public static class AccountInfo {
        private String accountType;
        private String accountNumber;
        private Double balance;

        public AccountInfo(String accountType, String accountNumber, Double balance) {
            this.accountType = accountType;
            this.accountNumber = accountNumber;
            this.balance = balance;
        }

        // Getters and Setters
        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public Double getBalance() {
            return balance;
        }

        public void setBalance(Double balance) {
            this.balance = balance;
        }
    }

    @Override
    public String toString() {
        return "UserAccountDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", accounts=" + accounts +
                '}';
    }
}
