package com.example.optiplant.services;

import com.example.optiplant.exceptions.UnauthorizedException;
import com.example.optiplant.model.User;
import com.example.optiplant.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Resolves the authenticated domain user from the current security context.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the authenticated user entity.
     *
     * @return authenticated user
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user was not found"));
    }
}
