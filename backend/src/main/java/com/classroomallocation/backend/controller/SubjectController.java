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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.SubjectRequest;
import com.classroomallocation.backend.dto.request.SubjectStatusRequest;
import com.classroomallocation.backend.dto.request.SubjectUpdateRequest;
import com.classroomallocation.backend.dto.response.SubjectPageResponse;
import com.classroomallocation.backend.dto.response.SubjectResponse;
import com.classroomallocation.backend.service.SubjectService;

@RestController
@RequestMapping("/api/subjects")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<SubjectPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        var result = subjectService.list(page, size, enabled);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(SubjectPageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(subjectService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> create(
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse created = subjectService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/subjects/" + created.id()
                ))
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SubjectUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(subjectService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SubjectResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SubjectStatusRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(subjectService.updateStatus(id, request));
    }
}