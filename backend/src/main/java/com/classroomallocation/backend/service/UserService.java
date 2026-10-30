package com.classroomallocation.backend.service;

import com.classroomallocation.backend.dto.request.CreateUserRequest;
import com.classroomallocation.backend.dto.response.UserResponse;
import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(CreateUserRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String displayName = request.displayName().trim();

        if (displayName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Display name is required."
            );
        }

        if (request.password()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password exceeds the supported byte length."
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account could not be created."
            );
        }

        User user = new User(
                displayName,
                email,
                passwordEncoder.encode(request.password()),
                request.role()
        );

        try {
            return UserResponse.from(
                    userRepository.saveAndFlush(user)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account could not be created."
            );
        }
    }
}