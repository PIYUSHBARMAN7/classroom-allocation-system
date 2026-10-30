package com.classroomallocation.backend.dto.response;

import java.time.Instant;

import com.classroomallocation.backend.allocation.AllocationEngine;

public record AllocationDraftCreatedResponse(
        Long id,
        String name,
        long version,
        Instant createdAt,
        AllocationEngine.Result result
) {
}