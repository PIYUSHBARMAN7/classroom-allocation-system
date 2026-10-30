package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AssignStudentSectionRequest(

        @NotNull
        @Positive
        Long sectionId,

        @PositiveOrZero
        Long version

) {
}