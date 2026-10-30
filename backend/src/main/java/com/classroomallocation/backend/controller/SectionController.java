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

import com.classroomallocation.backend.dto.request.SectionRequest;
import com.classroomallocation.backend.dto.request.SectionStatusRequest;
import com.classroomallocation.backend.dto.request.SectionUpdateRequest;
import com.classroomallocation.backend.dto.response.SectionPageResponse;
import com.classroomallocation.backend.dto.response.SectionResponse;
import com.classroomallocation.backend.service.SectionService;

@RestController
@RequestMapping("/api/sections")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public ResponseEntity<SectionPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        var result = sectionService.list(page, size, enabled);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(SectionPageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionResponse> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sectionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SectionResponse> create(
            @Valid @RequestBody SectionRequest request
    ) {
        SectionResponse created = sectionService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/sections/" + created.id()
                ))
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SectionUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sectionService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SectionResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SectionStatusRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(sectionService.updateStatus(id, request));
    }
}