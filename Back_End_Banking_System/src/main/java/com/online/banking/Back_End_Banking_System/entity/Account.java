package com.online.banking.Back_End_Banking_System.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts") // Explicit table name
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountType; // Dropdown: Saving, etc.

    @Column(nullable = false, unique = true)
    private String accountNumber; // 10-digit auto-generated

    @Column(nullable = false)
    private Double balance = 0.0; // Default starting balance

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Account() {}

    public Account(String accountType, String accountNumber, Double balance, User user) {
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
