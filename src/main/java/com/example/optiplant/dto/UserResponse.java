package com.example.optiplant.dto;

import com.example.optiplant.model.User;
import com.example.optiplant.model.enums.Role;
import java.util.UUID;

/**
 * Response payload for user account identity and authorization data.
 */
public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        Role role,
        UUID branchId
) {

    /**
     * Maps a user entity into its API response form.
     *
     * @param user source user entity
     * @return user response
     */
    public static UserResponse from(User user) {
        UUID branchId = user.getBranch() == null ? null : user.getBranch().getId();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                branchId
        );
    }
}
