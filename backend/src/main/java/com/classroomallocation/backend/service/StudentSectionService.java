package com.classroomallocation.backend.service;

import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.request.AssignStudentSectionRequest;
import com.classroomallocation.backend.dto.response.StudentSectionResponse;
import com.classroomallocation.backend.entity.StudentSectionAssignment;
import com.classroomallocation.backend.entity.User;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.SectionRepository;
import com.classroomallocation.backend.repository.StudentSectionAssignmentRepository;
import com.classroomallocation.backend.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class StudentSectionService {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final StudentSectionAssignmentRepository assignmentRepository;

    public StudentSectionService(
            UserRepository userRepository,
            SectionRepository sectionRepository,
            StudentSectionAssignmentRepository assignmentRepository
    ) {
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public StudentSectionResponse lookup(String email) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student account not found."
                ));

        validateStudent(user);

        StudentSectionAssignment assignment =
                assignmentRepository.findById(user.getId())
                        .orElse(null);

        return toResponse(user, assignment);
    }

    @Transactional
    public StudentSectionResponse assign(
            Long userId,
            AssignStudentSectionRequest request,
            String administratorEmail
    ) {
        /*
         * Lock the account first, including when no assignment exists.
         * This serializes concurrent assignments for the same Student.
         */
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student account not found."
                ));

        validateStudent(user);

        StudentSectionAssignment assignment =
                assignmentRepository.findById(userId)
                        .orElse(null);

        if (assignment == null) {
            if (request.version() != null) {
                throw staleAssignment();
            }
        } else if (!Objects.equals(
                request.version(),
                Long.valueOf(assignment.getVersion())
        )) {
            throw staleAssignment();
        }

        var section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Section not found."
                ));

        if (!section.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose an enabled section."
            );
        }

        if (assignment == null) {
            assignment = new StudentSectionAssignment(
                    user,
                    section,
                    administratorEmail
            );
        } else {
            assignment.changeSection(section, administratorEmail);
        }

        StudentSectionAssignment saved =
                assignmentRepository.saveAndFlush(assignment);

        return toResponse(user, saved);
    }

    private void validateStudent(User user) {
        if (user.getRole() != Role.STUDENT || !user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose an enabled Student account."
            );
        }
    }

    private ResponseStatusException staleAssignment() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The assignment has changed. Look up the Student again."
        );
    }

    private StudentSectionResponse toResponse(
            User user,
            StudentSectionAssignment assignment
    ) {
        if (assignment == null) {
            return new StudentSectionResponse(
                    user.getId(),
                    user.getDisplayName(),
                    user.getEmail(),
                    null,
                    null
            );
        }

        var section = assignment.getSection();

        return new StudentSectionResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                new StudentSectionResponse.SectionSummary(
                        section.getId(),
                        section.getCode(),
                        section.getName(),
                        section.getProgramCode(),
                        section.getSemester(),
                        section.getAcademicYearStart(),
                        section.isEnabled()
                ),
                assignment.getVersion()
        );
    }
}