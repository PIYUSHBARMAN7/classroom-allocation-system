package com.classroomallocation.backend.dto.response;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

public record MyTimetableResponse(
        Status status,
        String message,
        PublicationSummary publication,
        List<SessionSummary> sessions
) {
    public MyTimetableResponse {
        sessions = List.copyOf(sessions);
    }

    public enum Status {
        LINK_REQUIRED,
        PROFILE_DISABLED,
        NO_PUBLICATION,
        NO_SESSIONS,
        AVAILABLE
    }

    public record PublicationSummary(
            Long id,
            String name,
            Instant publishedAt
    ) {
    }

    public record SessionSummary(
            Long id,
            String subjectName,
            String roomCode,
            String teacherName,
            String sectionCode,
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }
}