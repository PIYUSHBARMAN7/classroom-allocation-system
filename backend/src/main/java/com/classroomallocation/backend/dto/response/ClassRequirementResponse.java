package com.classroomallocation.backend.dto.response;

import java.time.Instant;

import com.classroomallocation.backend.entity.ClassRequirement;
import com.classroomallocation.backend.enums.RoomType;

public record ClassRequirementResponse(
        Long id,
        SectionSummary section,
        SubjectSummary subject,
        TeacherSummary teacher,
        int sessionsPerWeek,
        int periodsPerSession,
        int weeklyPeriods,
        boolean requiresProjector,
        boolean requiresAccessibleRoom,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public record SectionSummary(
            Long id,
            String code,
            String name,
            String programCode,
            int semester,
            int academicYearStart,
            int studentCount,
            boolean enabled
    ) {
    }

    public record SubjectSummary(
            Long id,
            String code,
            String name,
            RoomType requiredRoomType,
            boolean enabled
    ) {
    }

    public record TeacherSummary(
            Long id,
            String employeeCode,
            String fullName,
            boolean enabled
    ) {
    }

    public static ClassRequirementResponse fromEntity(
            ClassRequirement requirement
    ) {
        var section = requirement.getSection();
        var subject = requirement.getSubject();
        var teacher = requirement.getTeacher();

        return new ClassRequirementResponse(
                requirement.getId(),

                new SectionSummary(
                        section.getId(),
                        section.getCode(),
                        section.getName(),
                        section.getProgramCode(),
                        section.getSemester(),
                        section.getAcademicYearStart(),
                        section.getStudentCount(),
                        section.isEnabled()
                ),

                new SubjectSummary(
                        subject.getId(),
                        subject.getCode(),
                        subject.getName(),
                        subject.getRequiredRoomType(),
                        subject.isEnabled()
                ),

                new TeacherSummary(
                        teacher.getId(),
                        teacher.getEmployeeCode(),
                        teacher.getFullName(),
                        teacher.isEnabled()
                ),

                requirement.getSessionsPerWeek(),
                requirement.getPeriodsPerSession(),
                requirement.getWeeklyPeriods(),
                requirement.isRequiresProjector(),
                requirement.isRequiresAccessibleRoom(),
                requirement.isEnabled(),
                requirement.getVersion(),
                requirement.getCreatedAt(),
                requirement.getUpdatedAt()
        );
    }
}