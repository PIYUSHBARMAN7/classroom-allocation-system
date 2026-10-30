package com.classroomallocation.backend.dto.response;

public record StudentSectionResponse(
        Long userId,
        String displayName,
        String email,
        SectionSummary section,
        Long version
) {

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