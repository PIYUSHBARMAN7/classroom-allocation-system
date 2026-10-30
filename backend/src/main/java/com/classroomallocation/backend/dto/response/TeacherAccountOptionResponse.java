package com.classroomallocation.backend.dto.response;

import com.classroomallocation.backend.entity.User;

public record TeacherAccountOptionResponse(
        Long id,
        String displayName,
        String email
) {

    public static TeacherAccountOptionResponse fromEntity(User user) {
        return new TeacherAccountOptionResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail()
        );
    }
}