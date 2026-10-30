package com.classroomallocation.backend.allocation;

import java.util.Objects;

import com.classroomallocation.backend.enums.RoomType;

public record RoomCandidate(
        long id,
        String code,
        int capacity,
        RoomType roomType,
        boolean hasProjector,
        boolean accessible,
        boolean enabled
) {

    public RoomCandidate {
        Objects.requireNonNull(code, "Room code is required.");
        Objects.requireNonNull(roomType, "Room type is required.");

        if (id <= 0 || capacity <= 0 || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Room ID, capacity and code must be valid."
            );
        }
    }
}