package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TeacherUpdateRequest(

        @NotBlank
        @Size(max = 30)
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,29}$",
                message = "Use letters, numbers, hyphens or underscores."
        )
        String employeeCode,

        @NotBlank
        @Size(max = 100)
        String fullName,

        @NotBlank
        @Size(max = 100)
        String department,

        @NotNull
        @Min(1)
        @Max(12)
        Integer maxPeriodsPerDay,

        @NotNull
        @Min(1)
        @Max(60)
        Integer maxPeriodsPerWeek,

        @NotNull
        @PositiveOrZero
        Long version
) {
}