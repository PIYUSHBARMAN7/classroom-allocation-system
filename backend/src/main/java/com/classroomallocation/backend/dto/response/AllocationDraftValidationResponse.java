package com.classroomallocation.backend.dto.response;

import java.util.List;

public record AllocationDraftValidationResponse(
        Long draftId,
        long version,
        boolean valid,
        List<String> errors
) {
    public AllocationDraftValidationResponse {
        errors = List.copyOf(errors);
    }
}