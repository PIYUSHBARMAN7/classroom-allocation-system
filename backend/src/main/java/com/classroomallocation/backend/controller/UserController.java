package com.classroomallocation.backend.controller;

import com.classroomallocation.backend.dto.request.CreateUserRequest;
import com.classroomallocation.backend.dto.response.UserResponse;
import com.classroomallocation.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse user = userService.create(request);

        return ResponseEntity
                .created(URI.create("/api/users/" + user.id()))
                .cacheControl(CacheControl.noStore())
                .body(user);
    }
}