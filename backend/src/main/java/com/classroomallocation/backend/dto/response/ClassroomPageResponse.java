package com.classroomallocation.backend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record ClassroomPageResponse(
        List<ClassroomResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static ClassroomPageResponse from(
            Page<ClassroomResponse> result
    ) {
        return new ClassroomPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}