package com.example.banking.controller;

import com.example.banking.entity.BankAccount;
import com.example.banking.entity.User;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.BankingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
public class BankingController {
    private final UserRepository userRepository;
    private final BankingService bankingService;

    public BankingController(UserRepository userRepository, BankingService bankingService) {
        this.userRepository = userRepository;
        this.bankingService = bankingService;
    }

    private User currentUser(HttpSession session) {
        Long id = (Long) session.getAttribute("userId");
        return id == null ? null : userRepository.findById(id).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";

        BankAccount account = bankingService.getAccount(user);
        if (account == null) account = bankingService.createAccount(user);

        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("transactions", bankingService.getTransactions(account));
        return "dashboard";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, HttpSession session,
                          Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";
        try {
            bankingService.deposit(bankingService.getAccount(user), amount);
            return "redirect:/dashboard?success=Deposit+successful";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return dashboard(session, model);
        }
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, HttpSession session,
                           Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";
        try {
            bankingService.withdraw(bankingService.getAccount(user), amount);
            return "redirect:/dashboard?success=Withdrawal+successful";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return dashboard(session, model);
        }
    }

    @GetMapping("/transfer")
    public String transferForm(HttpSession session, Model model) {
        if (currentUser(session) == null) return "redirect:/login";
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String receiverAccount,
                            @RequestParam BigDecimal amount,
                            HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";
        try {
            bankingService.transfer(bankingService.getAccount(user), receiverAccount, amount);
            return "redirect:/dashboard?success=Transfer+successful";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "transfer";
        }
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = currentUser(session);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("account", bankingService.getAccount(user));
        return "profile";
    }
}
