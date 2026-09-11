package com.example.banking.service;

import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BankingService {
    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankingService(BankAccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public BankAccount createAccount(User user) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType("SAVINGS");
        account.setStatus("ACTIVE");
        return accountRepository.save(account);
    }

    public BankAccount getAccount(User user) {
        return accountRepository.findByUser(user).orElse(null);
    }

    public List<Transaction> getTransactions(BankAccount account) {
        return transactionRepository.findByAccountOrderByTransactionDateDesc(account);
    }

    @Transactional
    public void deposit(BankAccount account, BigDecimal amount) {
        validateAmount(amount);
        ensureActive(account);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        saveTransaction(account, "DEPOSIT", amount, "Cash deposit");
    }

    @Transactional
    public void withdraw(BankAccount account, BigDecimal amount) {
        validateAmount(amount);
        ensureActive(account);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        saveTransaction(account, "WITHDRAW", amount, "Cash withdrawal");
    }

    @Transactional
    public void transfer(BankAccount sender, String receiverNumber, BigDecimal amount) {
        validateAmount(amount);
        ensureActive(sender);

        BankAccount receiver = accountRepository.findByAccountNumber(receiverNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found."));

        ensureActive(receiver);

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new IllegalArgumentException("You cannot transfer to your own account.");
        }
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        accountRepository.save(sender);
        accountRepository.save(receiver);

        saveTransaction(sender, "TRANSFER_OUT", amount,
                "Transfer to " + receiver.getAccountNumber());
        saveTransaction(receiver, "TRANSFER_IN", amount,
                "Transfer from " + sender.getAccountNumber());
    }

    private void saveTransaction(BankAccount account, String type,
                                  BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription(description);
        transaction.setReferenceNumber("TXN-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    private void ensureActive(BankAccount account) {
        if (account == null || !"ACTIVE".equals(account.getStatus())) {
            throw new IllegalArgumentException("Account is not active.");
        }
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "10" + (System.currentTimeMillis() % 100000000L);
        } while (accountRepository.findByAccountNumber(number).isPresent());
        return number;
    }
}
