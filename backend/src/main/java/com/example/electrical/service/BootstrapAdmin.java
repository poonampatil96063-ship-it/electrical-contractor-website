package com.example.electrical.service;

import com.example.electrical.entity.User;
import com.example.electrical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdmin {
    @Bean
    CommandLineRunner createAdmin(UserRepository users, PasswordEncoder encoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {
        return args -> {
            if (!users.existsByEmail(email.toLowerCase().trim())) {
                User admin = new User();
                admin.setFullName("Website Administrator");
                admin.setEmail(email);
                admin.setPhone("Not provided");
                admin.setPasswordHash(encoder.encode(password));
                admin.setRole("ADMIN");
                users.save(admin);
                System.out.println("Created admin account for " + email);
            }
        };
    }
}
