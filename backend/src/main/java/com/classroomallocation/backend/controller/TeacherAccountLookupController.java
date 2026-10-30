package com.classroomallocation.backend.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.response.TeacherAccountPageResponse;
import com.classroomallocation.backend.service.TeacherAccountLookupService;

@RestController
@RequestMapping("/api/users/available-teacher-accounts")
@PreAuthorize("hasRole('ADMIN')")
public class TeacherAccountLookupController {

    private final TeacherAccountLookupService lookupService;

    public TeacherAccountLookupController(
            TeacherAccountLookupService lookupService
    ) {
        this.lookupService = lookupService;
    }

    @GetMapping
    public ResponseEntity<TeacherAccountPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(50) int size
    ) {
        var result = lookupService.listAvailable(page, size);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(TeacherAccountPageResponse.from(result));
    }
}