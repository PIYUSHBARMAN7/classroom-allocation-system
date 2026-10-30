package com.classroomallocation.backend.dto.response;

import java.time.Instant;

import com.classroomallocation.backend.entity.Subject;
import com.classroomallocation.backend.enums.RoomType;

public record SubjectResponse(
        Long id,
        String code,
        String name,
        String department,
        RoomType requiredRoomType,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public static SubjectResponse fromEntity(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getDepartment(),
                subject.getRequiredRoomType(),
                subject.isEnabled(),
                subject.getVersion(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}