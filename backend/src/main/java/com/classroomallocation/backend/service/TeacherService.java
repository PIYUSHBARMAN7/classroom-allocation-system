package com.classroomallocation.backend.service;

import java.util.Locale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.request.TeacherAccountLinkRequest;
import com.classroomallocation.backend.dto.request.TeacherRequest;
import com.classroomallocation.backend.dto.request.TeacherStatusRequest;
import com.classroomallocation.backend.dto.request.TeacherUpdateRequest;
import com.classroomallocation.backend.dto.response.TeacherResponse;
import com.classroomallocation.backend.entity.Teacher;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.TeacherRepository;
import com.classroomallocation.backend.repository.UserRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public TeacherService(
            TeacherRepository teacherRepository,
            UserRepository userRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    public Page<TeacherResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            Boolean enabled
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by("employeeCode").ascending()
        );

        Page<Teacher> teachers;

        if (enabled == null) {
            teachers = teacherRepository.findAll(pageable);
        } else {
            teachers = teacherRepository.findByEnabled(
                    enabled,
                    pageable
            );
        }

        return teachers.map(TeacherResponse::fromEntity);
    }

    public TeacherResponse getById(
            @NotNull @Positive Long id
    ) {
        return TeacherResponse.fromEntity(findTeacher(id));
    }

    @Transactional
    public TeacherResponse create(
            @NotNull @Valid TeacherRequest request
    ) {
        String employeeCode = normalizeCode(
                request.employeeCode()
        );

        if (teacherRepository.existsByEmployeeCode(employeeCode)) {
            throw duplicateEmployeeCode();
        }

        Teacher teacher = new Teacher(
                employeeCode,
                request.fullName(),
                request.department(),
                request.maxPeriodsPerDay(),
                request.maxPeriodsPerWeek()
        );

        return save(teacher);
    }

    @Transactional
    public TeacherResponse update(
            @NotNull @Positive Long id,
            @NotNull @Valid TeacherUpdateRequest request
    ) {
        Teacher teacher = findTeacher(id);

        checkVersion(teacher, request.version());

        String employeeCode = normalizeCode(
                request.employeeCode()
        );

        if (teacherRepository.existsByEmployeeCodeAndIdNot(
                employeeCode,
                id
        )) {
            throw duplicateEmployeeCode();
        }

        teacher.updateDetails(
                employeeCode,
                request.fullName(),
                request.department(),
                request.maxPeriodsPerDay(),
                request.maxPeriodsPerWeek()
        );

        return save(teacher);
    }

    @Transactional
    public TeacherResponse updateStatus(
            @NotNull @Positive Long id,
            @NotNull @Valid TeacherStatusRequest request
    ) {
        Teacher teacher = findTeacher(id);

        checkVersion(teacher, request.version());

        teacher.setEnabled(request.enabled());

        return save(teacher);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse linkAccount(
            @NotNull @Positive Long id,
            @NotNull @Valid TeacherAccountLinkRequest request
    ) {
        /*
         * Lock the target account before checking its role
         * and assigning it to a teacher profile.
         */
        var user = userRepository
                .findByIdForUpdate(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Login account not found."
                ));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The login account must be enabled."
            );
        }

        if (user.getRole() != Role.TEACHER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only an account with the TEACHER role "
                    + "can be linked."
            );
        }

        Teacher teacher = findTeacher(id);

        checkVersion(teacher, request.version());

        if (teacherRepository.existsByUser_IdAndIdNot(
                user.getId(),
                id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is already linked "
                    + "to another teacher."
            );
        }

        teacher.linkUser(user);

        return save(teacher);
    }

    private Teacher findTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher not found."
                ));
    }

    private void checkVersion(
            Teacher teacher,
            long submittedVersion
    ) {
        if (teacher.getVersion() != submittedVersion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This teacher profile has changed. "
                    + "Refresh it before saving again."
            );
        }
    }

    private TeacherResponse save(Teacher teacher) {
        try {
            Teacher saved =
                    teacherRepository.saveAndFlush(teacher);

            return TeacherResponse.fromEntity(saved);

        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another request changed this teacher. "
                    + "Refresh it before saving again."
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The teacher profile conflicts with stored data. "
                    + "Check the employee code and linked account."
            );
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException duplicateEmployeeCode() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A teacher with this employee code already exists."
        );
    }
}