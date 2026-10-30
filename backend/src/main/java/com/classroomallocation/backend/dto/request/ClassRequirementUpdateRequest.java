package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ClassRequirementUpdateRequest(

        @NotNull
        @Positive
        Long sectionId,

        @NotNull
        @Positive
        Long subjectId,

        @NotNull
        @Positive
        Long teacherId,

        @NotNull
        @Min(1)
        @Max(20)
        Integer sessionsPerWeek,

        @NotNull
        @Min(1)
        @Max(4)
        Integer periodsPerSession,

        @NotNull
        Boolean requiresProjector,

        @NotNull
        Boolean requiresAccessibleRoom,

        @NotNull
        @PositiveOrZero
        Long version
) {
}