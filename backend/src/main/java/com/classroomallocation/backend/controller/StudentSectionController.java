package com.classroomallocation.backend.controller;

import java.security.Principal;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.AssignStudentSectionRequest;
import com.classroomallocation.backend.dto.response.StudentSectionResponse;
import com.classroomallocation.backend.service.StudentSectionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/student-section-assignments")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class StudentSectionController {

    private final StudentSectionService service;

    public StudentSectionController(StudentSectionService service) {
        this.service = service;
    }

    @GetMapping("/lookup")
    public ResponseEntity<StudentSectionResponse> lookup(
            @RequestParam("email")
            @NotBlank
            @Email
            @Size(max = 254)
            String email
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.lookup(email));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<StudentSectionResponse> assign(
            @PathVariable("userId") @Positive Long userId,
            @Valid @RequestBody AssignStudentSectionRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.assign(
                        userId,
                        request,
                        principal.getName()
                ));
    }
}