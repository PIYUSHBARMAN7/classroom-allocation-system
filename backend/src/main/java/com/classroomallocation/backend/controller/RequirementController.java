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

import com.classroomallocation.backend.dto.request.ClassRequirementRequest;
import com.classroomallocation.backend.dto.request.ClassRequirementStatusRequest;
import com.classroomallocation.backend.dto.request.ClassRequirementUpdateRequest;
import com.classroomallocation.backend.dto.response.ClassRequirementPageResponse;
import com.classroomallocation.backend.dto.response.ClassRequirementResponse;
import com.classroomallocation.backend.service.RequirementService;

@RestController
@RequestMapping("/api/requirements")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(
            RequirementService requirementService
    ) {
        this.requirementService = requirementService;
    }

    @GetMapping
    public ResponseEntity<ClassRequirementPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        var result = requirementService.list(page, size, enabled);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ClassRequirementPageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassRequirementResponse> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(requirementService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClassRequirementResponse> create(
            @Valid @RequestBody ClassRequirementRequest request
    ) {
        ClassRequirementResponse created =
                requirementService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/requirements/" + created.id()
                ))
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassRequirementResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ClassRequirementUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(requirementService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClassRequirementResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ClassRequirementStatusRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(requirementService.updateStatus(id, request));
    }
}