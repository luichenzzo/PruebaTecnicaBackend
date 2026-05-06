package com.example.optiplant.repository;

import com.example.optiplant.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for application users and authentication lookups.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by username.
     *
     * @param username username
     * @return optional matching user
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email address without case sensitivity.
     *
     * @param email email address
     * @return optional matching user
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks whether a username is already assigned.
     *
     * @param username username
     * @return {@code true} when the username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an email is already assigned without case sensitivity.
     *
     * @param email email address
     * @return {@code true} when the email exists
     */
    boolean existsByEmailIgnoreCase(String email);
}
