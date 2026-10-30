package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SectionStatusRequest(

        @NotNull
        Boolean enabled,

        @NotNull
        @PositiveOrZero
        Long version
) {
}