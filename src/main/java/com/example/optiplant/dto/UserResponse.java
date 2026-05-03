package com.example.optiplant.dto;

import com.example.optiplant.model.User;
import com.example.optiplant.model.enums.Role;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        Role role,
        UUID branchId
) {

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
