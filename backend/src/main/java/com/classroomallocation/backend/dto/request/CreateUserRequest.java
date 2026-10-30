package com.classroomallocation.backend.dto.request;

import com.classroomallocation.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        String displayName,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 15, max = 72)
        String password,

        @NotNull
        Role role
) {
    @Override
    public String toString() {
        return "CreateUserRequest[credentials omitted]";
    }
}