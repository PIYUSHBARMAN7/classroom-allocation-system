package com.classroomallocation.backend.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.response.AllocationDraftValidationResponse;
import com.classroomallocation.backend.service.AllocationDraftValidationService;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/allocation-drafts")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
@Validated
public class AllocationDraftValidationController {

    private final AllocationDraftValidationService service;

    public AllocationDraftValidationController(
            AllocationDraftValidationService service
    ) {
        this.service = service;
    }

    @GetMapping("/{draftId}/validation")
    public ResponseEntity<AllocationDraftValidationResponse> validate(
            @PathVariable("draftId") @Positive Long draftId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.validate(draftId));
    }
}