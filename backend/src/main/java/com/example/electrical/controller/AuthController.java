package com.example.electrical.controller;

import org.springframework.security.core.AuthenticationException;
import com.example.electrical.entity.User;
import com.example.electrical.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager;
    }

    record RegisterBody(@NotBlank @Size(max=120) String fullName,
                        @Email @NotBlank String email,
                        @NotBlank @Size(max=30) String phone,
                        @NotBlank @Size(min=8, max=100) String password) {}
    record LoginBody(@Email @NotBlank String email, @NotBlank String password) {}

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody RegisterBody body) {
        String email = body.email().toLowerCase().trim();
        if (users.existsByEmail(email)) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email is already registered"));
        User user = new User();
        user.setFullName(body.fullName().trim());
        user.setEmail(email);
        user.setPhone(body.phone().trim());
        user.setPasswordHash(encoder.encode(body.password()));
        user.setRole("CUSTOMER");
        users.save(user);
        return ResponseEntity.ok(Map.of("message", "Registration successful. Please log in."));
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginBody body, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email().toLowerCase().trim(), body.password()));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true);
            new HttpSessionSecurityContextRepository().saveContext(context, request, null);
            User user = users.findByEmail(authentication.getName()).orElseThrow();
            return ResponseEntity.ok(Map.of("id", user.getId(), "fullName", user.getFullName(),
                    "email", user.getEmail(), "role", user.getRole()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        User user = users.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of("id", user.getId(), "fullName", user.getFullName(),
                "email", user.getEmail(), "role", user.getRole()));
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
