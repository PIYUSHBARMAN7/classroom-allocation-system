package com.classroomallocation.backend.controller;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.TeacherAccountLinkRequest;
import com.classroomallocation.backend.dto.request.TeacherRequest;
import com.classroomallocation.backend.dto.request.TeacherStatusRequest;
import com.classroomallocation.backend.dto.request.TeacherUpdateRequest;
import com.classroomallocation.backend.dto.response.TeacherPageResponse;
import com.classroomallocation.backend.dto.response.TeacherResponse;
import com.classroomallocation.backend.service.TeacherService;

@RestController
@RequestMapping("/api/teachers")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ResponseEntity<TeacherPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        var result = teacherService.list(page, size, enabled);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(TeacherPageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(teacherService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(
            @Valid @RequestBody TeacherRequest request
    ) {
        TeacherResponse created = teacherService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/teachers/" + created.id()
                ))
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TeacherUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(teacherService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TeacherResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TeacherStatusRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(teacherService.updateStatus(id, request));
    }

    @PutMapping("/{id}/account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> linkAccount(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TeacherAccountLinkRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(teacherService.linkAccount(id, request));
    }
}