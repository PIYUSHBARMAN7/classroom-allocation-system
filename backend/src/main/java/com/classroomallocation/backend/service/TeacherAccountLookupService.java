package com.classroomallocation.backend.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.classroomallocation.backend.dto.response.TeacherAccountOptionResponse;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.UserRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class TeacherAccountLookupService {

    private final UserRepository userRepository;

    public TeacherAccountLookupService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public Page<TeacherAccountOptionResponse> listAvailable(
            @Min(0) int page,
            @Min(1) @Max(50) int size
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by("displayName")
                        .ascending()
                        .and(Sort.by("id").ascending())
        );

        return userRepository
                .findAvailableTeacherAccounts(Role.TEACHER, pageable)
                .map(TeacherAccountOptionResponse::fromEntity);
    }
}