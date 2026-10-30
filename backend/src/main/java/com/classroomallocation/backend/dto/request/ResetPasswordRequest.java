package com.classroomallocation.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$")
        String token,

        @NotBlank
        @Size(min = 15, max = 72)
        String newPassword
) {
    @Override
    public String toString() {
        return "ResetPasswordRequest[credentials omitted]";
    }
}