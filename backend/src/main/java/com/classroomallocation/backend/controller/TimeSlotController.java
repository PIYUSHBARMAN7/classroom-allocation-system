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

import com.classroomallocation.backend.dto.request.TimeSlotRequest;
import com.classroomallocation.backend.dto.request.TimeSlotStatusRequest;
import com.classroomallocation.backend.dto.request.TimeSlotUpdateRequest;
import com.classroomallocation.backend.dto.response.TimeSlotPageResponse;
import com.classroomallocation.backend.dto.response.TimeSlotResponse;
import com.classroomallocation.backend.service.TimeSlotService;

@RestController
@RequestMapping("/api/time-slots")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<TimeSlotPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        var result = timeSlotService.list(page, size, enabled);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(TimeSlotPageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(timeSlotService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TimeSlotResponse> create(
            @Valid @RequestBody TimeSlotRequest request
    ) {
        TimeSlotResponse created =
                timeSlotService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/time-slots/" + created.id()
                ))
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TimeSlotUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(timeSlotService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TimeSlotResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody TimeSlotStatusRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(timeSlotService.updateStatus(id, request));
    }
}