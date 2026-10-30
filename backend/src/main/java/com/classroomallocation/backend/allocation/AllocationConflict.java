package com.classroomallocation.backend.allocation;

import java.util.Objects;

public record AllocationConflict(
        ConflictType type,
        ScheduledSession existingSession
) {

    public AllocationConflict {
        Objects.requireNonNull(type, "Conflict type is required.");

        Objects.requireNonNull(
                existingSession,
                "Existing session is required."
        );
    }
}