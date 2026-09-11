package com.example.banking.repository;

import com.example.banking.entity.BankAccount;
import com.example.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByUser(User user);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
