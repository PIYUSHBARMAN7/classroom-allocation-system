package com.classroomallocation.backend.dto.response;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

public record AllocationDraftDetailResponse(
        Long id,
        String name,
        long version,
        Instant createdAt,
        int requestedSessions,
        int allocatedSessions,
        int unallocatedSessions,
        boolean allSessionsAllocated,
        List<RequirementSummary> requirements,
        List<SessionSummary> sessions
) {
    public AllocationDraftDetailResponse {
        requirements = List.copyOf(requirements);
        sessions = List.copyOf(sessions);
    }

    public record RequirementSummary(
            Long requirementId,
            int sessionsRequested,
            int periodsPerSession,
            int allocatedSessions,
            int unallocatedSessions
    ) {
    }

    public record SessionSummary(
            Long id,
            Long requirementId,
            int sessionNumber,
            Long roomId,
            Long teacherId,
            Long sectionId,
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }
}