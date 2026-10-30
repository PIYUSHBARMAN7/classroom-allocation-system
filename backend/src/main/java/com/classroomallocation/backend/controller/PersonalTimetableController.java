package com.classroomallocation.backend.controller;

import java.security.Principal;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.response.MyTimetableResponse;
import com.classroomallocation.backend.service.PersonalTimetableService;

@RestController
@RequestMapping("/api/me/timetable")
@PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
public class PersonalTimetableController {

    private final PersonalTimetableService service;

    public PersonalTimetableController(PersonalTimetableService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<MyTimetableResponse> getMyTimetable(
            Principal principal
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.getMyTimetable(principal.getName()));
    }
}