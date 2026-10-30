package com.classroomallocation.backend.dto.response;

import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.enums.Role;

public record UserResponse(
        Long id,
        String displayName,
        String email,
        Role role,
        boolean enabled
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}