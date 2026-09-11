package com.example.banking.controller;

import com.example.banking.entity.BankAccount;
import com.example.banking.entity.Transaction;
import com.example.banking.entity.User;
import com.example.banking.repository.BankAccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminController(UserRepository userRepository,
                           BankAccountRepository accountRepository,
                           TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    private boolean isAdmin(HttpSession session) {
        Long id = (Long) session.getAttribute("userId");
        if (id == null) return false;
        return userRepository.findById(id)
                .map(u -> "ADMIN".equals(u.getRole()) && u.isActive())
                .orElse(false);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("customers",
                userRepository.findAll().stream()
                        .filter(u -> "CUSTOMER".equals(u.getRole())).toList());
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("transactions", transactionRepository.findAllByOrderByTransactionDateDesc());
        return "admin-dashboard";
    }

    @PostMapping("/customer/{id}/toggle")
    public String toggleCustomer(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(!user.isActive());
            userRepository.save(user);
        });
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/account/{id}/toggle")
    public String toggleAccount(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        accountRepository.findById(id).ifPresent(account -> {
            account.setStatus("ACTIVE".equals(account.getStatus()) ? "BLOCKED" : "ACTIVE");
            accountRepository.save(account);
        });
        return "redirect:/admin/dashboard";
    }
}
