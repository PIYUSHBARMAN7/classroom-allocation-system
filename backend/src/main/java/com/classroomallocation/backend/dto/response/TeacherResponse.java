package com.classroomallocation.backend.dto.response;

import java.time.Instant;

import com.classroomallocation.backend.entity.Teacher;

public record TeacherResponse(
        Long id,
        String employeeCode,
        String fullName,
        String department,
        Long userId,
        int maxPeriodsPerDay,
        int maxPeriodsPerWeek,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public static TeacherResponse fromEntity(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getEmployeeCode(),
                teacher.getFullName(),
                teacher.getDepartment(),
                teacher.getUserId(),
                teacher.getMaxPeriodsPerDay(),
                teacher.getMaxPeriodsPerWeek(),
                teacher.isEnabled(),
                teacher.getVersion(),
                teacher.getCreatedAt(),
                teacher.getUpdatedAt()
        );
    }
}