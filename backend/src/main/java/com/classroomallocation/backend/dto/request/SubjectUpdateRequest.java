package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.classroomallocation.backend.enums.RoomType;

public record SubjectUpdateRequest(

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
        @Size(max = 100)
        String department,

        @NotNull
        RoomType requiredRoomType,

        @NotNull
        @PositiveOrZero
        Long version
) {
}