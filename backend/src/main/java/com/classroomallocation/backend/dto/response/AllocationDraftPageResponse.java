package com.classroomallocation.backend.dto.response;

import java.util.List;

public record AllocationDraftPageResponse(
        List<AllocationDraftSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public AllocationDraftPageResponse {
        content = List.copyOf(content);
    }
}