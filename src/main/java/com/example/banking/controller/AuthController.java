package com.example.banking.controller;

import com.example.banking.entity.User;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.BankingService;
import com.example.banking.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final BankingService bankingService;

    public AuthController(UserRepository userRepository,
                          UserService userService,
                          BankingService bankingService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.bankingService = bankingService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        // Login accepts either username or email.
        User user = userRepository
                .findByUsernameOrEmail(username, username)
                .orElse(null);

        if (user == null) {
            model.addAttribute("error",
                    "Account not found. Check your username or email.");
            return "login";
        }

        if (!user.isActive()) {
            model.addAttribute("error",
                    "Your account has been blocked. Contact the administrator.");
            return "login";
        }

        if (!userService.matches(password, user.getPassword())) {
            model.addAttribute("error", "Incorrect password.");
            return "login";
        }

        session.setAttribute("userId", user.getId());

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        if (bankingService.getAccount(user) == null) {
            bankingService.createAccount(user);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            User savedUser = userService.register(user);
            bankingService.createAccount(savedUser);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
