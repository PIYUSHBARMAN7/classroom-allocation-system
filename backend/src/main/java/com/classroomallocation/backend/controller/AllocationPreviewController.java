package com.classroomallocation.backend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.allocation.AllocationEngine;
import com.classroomallocation.backend.dto.request.AllocationPreviewRequest;
import com.classroomallocation.backend.service.AllocationPreviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/allocations")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AllocationPreviewController {

    private final AllocationPreviewService service;

    public AllocationPreviewController(
            AllocationPreviewService service
    ) {
        this.service = service;
    }

    @PostMapping("/preview")
    public ResponseEntity<AllocationEngine.Result> preview(
            @Valid @RequestBody AllocationPreviewRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.preview(request));
    }
}