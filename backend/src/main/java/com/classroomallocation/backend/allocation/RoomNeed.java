package com.classroomallocation.backend.allocation;

import java.util.Objects;

import com.classroomallocation.backend.enums.RoomType;

public record RoomNeed(
        int studentCount,
        RoomType requiredRoomType,
        boolean requiresProjector,
        boolean requiresAccessibleRoom
) {

    public RoomNeed {
        Objects.requireNonNull(
                requiredRoomType,
                "Required room type is missing."
        );

        if (studentCount <= 0) {
            throw new IllegalArgumentException(
                    "Student count must be positive."
            );
        }
    }
}