package com.classroomallocation.backend.dto.response;

public record MySectionResponse(SectionSummary section) {

    public record SectionSummary(
            Long id,
            String code,
            String name,
            String programCode,
            int semester,
            int academicYearStart,
            boolean enabled
    ) {
    }
}