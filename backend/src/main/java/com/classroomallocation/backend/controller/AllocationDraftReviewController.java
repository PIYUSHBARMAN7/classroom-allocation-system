package com.classroomallocation.backend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.response.AllocationDraftDetailResponse;
import com.classroomallocation.backend.dto.response.AllocationDraftPageResponse;
import com.classroomallocation.backend.service.AllocationDraftReviewService;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/allocation-drafts")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
@Validated
public class AllocationDraftReviewController {

    private final AllocationDraftReviewService service;

    public AllocationDraftReviewController(
            AllocationDraftReviewService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<AllocationDraftPageResponse> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.list(page, size));
    }

    @GetMapping("/{draftId}")
    public ResponseEntity<AllocationDraftDetailResponse> get(
            @PathVariable("draftId") @Positive Long draftId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.get(draftId));
    }
}