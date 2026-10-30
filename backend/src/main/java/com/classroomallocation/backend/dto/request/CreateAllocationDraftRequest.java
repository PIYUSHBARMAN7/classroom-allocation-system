package com.classroomallocation.backend.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAllocationDraftRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @NotEmpty
        @Size(max = 25)
        Set<@NotNull @Positive Long> requirementIds

) {
}