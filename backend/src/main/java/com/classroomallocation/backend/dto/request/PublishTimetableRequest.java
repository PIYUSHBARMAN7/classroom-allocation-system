package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PublishTimetableRequest(

        @NotNull
        @Positive
        Long draftId,

        @NotNull
        @PositiveOrZero
        Long draftVersion,

        @Positive
        Long expectedActivePublicationId

) {
}