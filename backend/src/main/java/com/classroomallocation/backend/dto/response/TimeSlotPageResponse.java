package com.classroomallocation.backend.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record TimeSlotPageResponse(
        List<TimeSlotResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static TimeSlotPageResponse from(
            Page<TimeSlotResponse> result
    ) {
        return new TimeSlotPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}