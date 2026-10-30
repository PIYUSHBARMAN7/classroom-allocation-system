package com.classroomallocation.backend.controller;

import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> currentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);

        if (user == null || !user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new CurrentUserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole()
        ));
    }

    public record CurrentUserResponse(
            Long id,
            String displayName,
            String email,
            Role role
    ) {
    }
}