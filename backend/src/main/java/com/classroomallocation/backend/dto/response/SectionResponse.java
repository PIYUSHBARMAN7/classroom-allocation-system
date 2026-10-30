package com.classroomallocation.backend.dto.response;

import java.time.Instant;

import com.classroomallocation.backend.entity.Section;

public record SectionResponse(
        Long id,
        String code,
        String name,
        String programCode,
        String department,
        int semester,
        int academicYearStart,
        int studentCount,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public static SectionResponse fromEntity(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getCode(),
                section.getName(),
                section.getProgramCode(),
                section.getDepartment(),
                section.getSemester(),
                section.getAcademicYearStart(),
                section.getStudentCount(),
                section.isEnabled(),
                section.getVersion(),
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }
}
