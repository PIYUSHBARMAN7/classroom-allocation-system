package com.classroomallocation.backend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record TeacherAccountPageResponse(
        List<TeacherAccountOptionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static TeacherAccountPageResponse from(
            Page<TeacherAccountOptionResponse> result
    ) {
        return new TeacherAccountPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}