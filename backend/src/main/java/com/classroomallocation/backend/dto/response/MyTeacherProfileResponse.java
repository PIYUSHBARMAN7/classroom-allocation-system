package com.classroomallocation.backend.dto.response;

public record MyTeacherProfileResponse(TeacherSummary teacher) {

    public record TeacherSummary(
            Long id,
            String employeeCode,
            String fullName,
            String department,
            int maxPeriodsPerDay,
            int maxPeriodsPerWeek,
            boolean enabled
    ) {
    }
}