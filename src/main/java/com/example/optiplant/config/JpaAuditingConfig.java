package com.example.optiplant.config;

import com.example.optiplant.model.User;
import com.example.optiplant.repository.UserRepository;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configures Spring Data auditing so created and updated entities can capture
 * the authenticated user.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * Resolves the current auditor from the Spring Security context.
     *
     * @param userRepository repository used to load a user principal by username
     * @return auditor provider for JPA auditing callbacks
     */
    @Bean
    public AuditorAware<User> auditorAware(UserRepository userRepository) {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return Optional.of(user);
            }

            if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
                return userRepository.findByUsername(userDetails.getUsername());
            }

            if (principal instanceof String username && !"anonymousUser".equals(username)) {
                return userRepository.findByUsername(username);
            }

            return Optional.empty();
        };
    }
}
