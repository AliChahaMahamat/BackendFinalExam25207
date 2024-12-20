package com.online.banking.Back_End_Banking_System.repository;

import com.online.banking.Back_End_Banking_System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts")
    List<User> findAllUsersWithAccounts();

    Optional<User> findByEmail(String email); // Add this method

}
