package com.classroomallocation.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.response.MyTeacherProfileResponse;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.TeacherRepository;
import com.classroomallocation.backend.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('TEACHER')")
public class TeacherWorkspaceService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;

    public TeacherWorkspaceService(
            UserRepository userRepository,
            TeacherRepository teacherRepository
    ) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
    }

    public MyTeacherProfileResponse getMyProfile(String authenticatedEmail) {
        var user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));

        if (!user.isEnabled() || user.getRole() != Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var teacher = teacherRepository.findByUser_Id(user.getId())
                .orElse(null);

        if (teacher == null) {
            return new MyTeacherProfileResponse(null);
        }

        return new MyTeacherProfileResponse(
                new MyTeacherProfileResponse.TeacherSummary(
                        teacher.getId(),
                        teacher.getEmployeeCode(),
                        teacher.getFullName(),
                        teacher.getDepartment(),
                        teacher.getMaxPeriodsPerDay(),
                        teacher.getMaxPeriodsPerWeek(),
                        teacher.isEnabled()
                )
        );
    }
}