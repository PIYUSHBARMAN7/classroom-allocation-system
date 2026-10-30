package com.classroomallocation.backend.controller;

import java.security.Principal;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.response.MyTeacherProfileResponse;
import com.classroomallocation.backend.service.TeacherWorkspaceService;

@RestController
@RequestMapping("/api/teacher/me")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherWorkspaceController {

    private final TeacherWorkspaceService service;

    public TeacherWorkspaceController(TeacherWorkspaceService service) {
        this.service = service;
    }

    @GetMapping("/profile")
    public ResponseEntity<MyTeacherProfileResponse> getMyProfile(
            Principal principal
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.getMyProfile(principal.getName()));
    }
}