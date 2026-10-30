package com.classroomallocation.backend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record ClassRequirementPageResponse(
        List<ClassRequirementResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static ClassRequirementPageResponse from(
            Page<ClassRequirementResponse> result
    ) {
        return new ClassRequirementPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}