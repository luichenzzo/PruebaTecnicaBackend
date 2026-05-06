package com.example.optiplant.controller;

import com.example.optiplant.dto.AuthResponse;
import com.example.optiplant.dto.LoginRequest;
import com.example.optiplant.dto.RegisterRequest;
import com.example.optiplant.dto.UserResponse;
import com.example.optiplant.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for registration, login, and authenticated user lookups.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new operator account and returns a login token.
     *
     * @param request validated registration payload
     * @return authentication token and created user details
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Authenticates a user by username or email.
     *
     * @param request validated login payload
     * @return authentication token and current user details
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Returns the currently authenticated user.
     *
     * @param authentication active Spring Security authentication
     * @return current user details
     */
    @GetMapping({"/me", "/verify"})
    public UserResponse currentUser(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }

    /**
     * Lists application users for managers and administrators.
     *
     * @return all user summaries
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<UserResponse> findAllUsers() {
        return authService.findAll();
    }
}
