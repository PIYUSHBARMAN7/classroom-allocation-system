package com.classroomallocation.backend.dto.response;

import java.time.Instant;

import com.classroomallocation.backend.entity.Classroom;
import com.classroomallocation.backend.enums.RoomType;

public record ClassroomResponse(
        Long id,
        String code,
        String name,
        String building,
        int floorNumber,
        int capacity,
        RoomType roomType,
        boolean hasProjector,
        boolean accessible,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public static ClassroomResponse fromEntity(Classroom classroom) {
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getCode(),
                classroom.getName(),
                classroom.getBuilding(),
                classroom.getFloorNumber(),
                classroom.getCapacity(),
                classroom.getRoomType(),
                classroom.isHasProjector(),
                classroom.isAccessible(),
                classroom.isEnabled(),
                classroom.getVersion(),
                classroom.getCreatedAt(),
                classroom.getUpdatedAt()
        );
    }
}