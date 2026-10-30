package com.classroomallocation.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.response.MySectionResponse;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.StudentSectionAssignmentRepository;
import com.classroomallocation.backend.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('STUDENT')")
public class StudentWorkspaceService {

    private final UserRepository userRepository;
    private final StudentSectionAssignmentRepository assignmentRepository;

    public StudentWorkspaceService(
            UserRepository userRepository,
            StudentSectionAssignmentRepository assignmentRepository
    ) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public MySectionResponse getMySection(String authenticatedEmail) {
        var user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));

        if (!user.isEnabled() || user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var assignment = assignmentRepository.findById(user.getId())
                .orElse(null);

        if (assignment == null) {
            return new MySectionResponse(null);
        }

        var section = assignment.getSection();

        return new MySectionResponse(
                new MySectionResponse.SectionSummary(
                        section.getId(),
                        section.getCode(),
                        section.getName(),
                        section.getProgramCode(),
                        section.getSemester(),
                        section.getAcademicYearStart(),
                        section.isEnabled()
                )
        );
    }
}