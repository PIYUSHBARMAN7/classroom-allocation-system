package com.classroomallocation.backend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record SubjectPageResponse(
        List<SubjectResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static SubjectPageResponse from(
            Page<SubjectResponse> result
    ) {
        return new SubjectPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}