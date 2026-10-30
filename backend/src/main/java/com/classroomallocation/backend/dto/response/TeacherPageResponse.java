package com.classroomallocation.backend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record TeacherPageResponse(
        List<TeacherResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static TeacherPageResponse from(
            Page<TeacherResponse> result
    ) {
        return new TeacherPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}