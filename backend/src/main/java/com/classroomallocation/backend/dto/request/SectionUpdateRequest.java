package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SectionUpdateRequest(

        @NotBlank
        @Size(max = 30)
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,29}$",
                message = "Use letters, numbers, hyphens or underscores."
        )
        String code,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 30)
        @Pattern(
                regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,29}$",
                message = "Use letters, numbers, hyphens or underscores."
        )
        String programCode,

        @NotBlank
        @Size(max = 100)
        String department,

        @NotNull
        @Min(1)
        @Max(12)
        Integer semester,

        @NotNull
        @Min(2000)
        @Max(2200)
        Integer academicYearStart,

        @NotNull
        @Min(1)
        @Max(2000)
        Integer studentCount,

        @NotNull
        @PositiveOrZero
        Long version
) {
}