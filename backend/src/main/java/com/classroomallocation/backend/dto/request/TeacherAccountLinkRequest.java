package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record TeacherAccountLinkRequest(

        @NotNull
        @Positive
        Long userId,

        @NotNull
        @PositiveOrZero
        Long version
) {
}