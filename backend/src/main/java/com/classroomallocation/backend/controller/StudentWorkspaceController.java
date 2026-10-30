package com.classroomallocation.backend.controller;

import java.security.Principal;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.response.MySectionResponse;
import com.classroomallocation.backend.service.StudentWorkspaceService;

@RestController
@RequestMapping("/api/student/me")
@PreAuthorize("hasRole('STUDENT')")
public class StudentWorkspaceController {

    private final StudentWorkspaceService service;

    public StudentWorkspaceController(StudentWorkspaceService service) {
        this.service = service;
    }

    @GetMapping("/section")
    public ResponseEntity<MySectionResponse> getMySection(
            Principal principal
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.getMySection(principal.getName()));
    }
}