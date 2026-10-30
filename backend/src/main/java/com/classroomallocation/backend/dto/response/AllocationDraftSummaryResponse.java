package com.classroomallocation.backend.dto.response;

import java.time.Instant;

public record AllocationDraftSummaryResponse(
        Long id,
        String name,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}