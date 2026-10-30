package com.classroomallocation.backend.allocation;

import java.time.LocalTime;
import java.util.Objects;

public record ScheduledSession(
        long requirementId,
        long roomId,
        long teacherId,
        long sectionId,
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {

    public ScheduledSession {
        Objects.requireNonNull(startTime, "Start time is required.");
        Objects.requireNonNull(endTime, "End time is required.");

        if (requirementId <= 0
                || roomId <= 0
                || teacherId <= 0
                || sectionId <= 0) {
            throw new IllegalArgumentException(
                    "Requirement, room, teacher and section IDs "
                    + "must be positive."
            );
        }

        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException(
                    "Day must be between 1 and 7."
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }

        if (startTime.getSecond() != 0
                || startTime.getNano() != 0
                || endTime.getSecond() != 0
                || endTime.getNano() != 0) {
            throw new IllegalArgumentException(
                    "Session times must use whole minutes."
            );
        }
    }
}