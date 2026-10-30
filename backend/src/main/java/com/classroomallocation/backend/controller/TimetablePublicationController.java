package com.classroomallocation.backend.controller;

import java.security.Principal;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomallocation.backend.dto.request.PublishTimetableRequest;
import com.classroomallocation.backend.dto.response.TimetablePublicationResponse;
import com.classroomallocation.backend.service.TimetablePublicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/timetable-publications")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TimetablePublicationController {

    private final TimetablePublicationService service;

    public TimetablePublicationController(
            TimetablePublicationService service
    ) {
        this.service = service;
    }

    @GetMapping("/active")
    public ResponseEntity<TimetablePublicationResponse> getActive() {
        var publication = service.getActive();

        if (publication == null) {
            return ResponseEntity.noContent()
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(publication);
    }

    @PostMapping
    public ResponseEntity<TimetablePublicationResponse> publish(
            @Valid @RequestBody PublishTimetableRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.publish(
                        request,
                        principal.getName()
                ));
    }
}