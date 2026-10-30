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

import com.classroomallocation.backend.dto.request.ClassroomRequest;
import com.classroomallocation.backend.dto.request.ClassroomStatusRequest;
import com.classroomallocation.backend.dto.request.ClassroomUpdateRequest;
import com.classroomallocation.backend.dto.response.ClassroomPageResponse;
import com.classroomallocation.backend.dto.response.ClassroomResponse;
import com.classroomallocation.backend.service.ClassroomService;

@RestController
@RequestMapping("/api/classrooms")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ResponseEntity<ClassroomPageResponse> list(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        var result = classroomService.list(page, size, enabled);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ClassroomPageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomResponse> getById(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(classroomService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClassroomResponse> create(
            @Valid @RequestBody ClassroomRequest request
    ) {
        ClassroomResponse created =
                classroomService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/classrooms/" + created.id()
                ))
                .cacheControl(CacheControl.noStore())
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassroomResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ClassroomUpdateRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(classroomService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClassroomResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ClassroomStatusRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(classroomService.updateStatus(id, request));
    }
}