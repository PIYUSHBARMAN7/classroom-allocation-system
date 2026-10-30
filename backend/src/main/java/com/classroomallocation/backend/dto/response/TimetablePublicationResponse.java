package com.classroomallocation.backend.dto.response;

import java.time.Instant;

public record TimetablePublicationResponse(
        Long id,
        Long sourceDraftId,
        long sourceDraftVersion,
        String name,
        Instant publishedAt,
        long sessionCount
) {
}