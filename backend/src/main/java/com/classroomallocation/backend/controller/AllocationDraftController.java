package com.classroomallocation.backend.controller;

import java.security.Principal;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.CreateAllocationDraftRequest;
import com.classroomallocation.backend.dto.response.AllocationDraftCreatedResponse;
import com.classroomallocation.backend.service.AllocationDraftService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/allocation-drafts")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AllocationDraftController {

    private final AllocationDraftService service;

    public AllocationDraftController(
            AllocationDraftService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AllocationDraftCreatedResponse> create(
            @Valid @RequestBody CreateAllocationDraftRequest request,
            Principal principal
    ) {
        AllocationDraftCreatedResponse response = service.create(
                request,
                principal.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}