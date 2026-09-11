package com.example.banking;

import com.example.banking.entity.User;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlineBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineBankingApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, UserService userService) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setFullName("Bank Administrator");
                admin.setUsername("admin");
                admin.setPassword(userService.encodePassword("Admin@2026"));
                admin.setEmail("admin@onlinebank.com");
                admin.setPhone("9999999999");
                admin.setRole("ADMIN");
                admin.setActive(true);
                userRepository.save(admin);
            }
        };
    }
}
