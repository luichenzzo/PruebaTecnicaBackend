package com.example.optiplant.security;

import com.example.optiplant.model.User;
import com.example.optiplant.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads application users for Spring Security authentication by username or
 * email address.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user principal by username or email.
     *
     * @param usernameOrEmail username or email supplied during authentication
     * @return user details used by Spring Security
     * @throws UsernameNotFoundException if no user matches the supplied value
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user;
    }
}
