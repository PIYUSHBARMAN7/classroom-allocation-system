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

import com.classroomallocation.backend.dto.request.ClassroomRequest;
import com.classroomallocation.backend.dto.request.ClassroomStatusRequest;
import com.classroomallocation.backend.dto.request.ClassroomUpdateRequest;
import com.classroomallocation.backend.dto.response.ClassroomResponse;
import com.classroomallocation.backend.entity.Classroom;
import com.classroomallocation.backend.repository.ClassroomRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ClassroomService(
            ClassroomRepository classroomRepository
    ) {
        this.classroomRepository = classroomRepository;
    }

    public Page<ClassroomResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            Boolean enabled
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by("code").ascending()
        );

        Page<Classroom> classrooms;

        if (enabled == null) {
            classrooms = classroomRepository.findAll(pageable);
        } else {
            classrooms = classroomRepository.findByEnabled(
                    enabled,
                    pageable
            );
        }

        return classrooms.map(ClassroomResponse::fromEntity);
    }

    public ClassroomResponse getById(
            @NotNull @Positive Long id
    ) {
        return ClassroomResponse.fromEntity(findClassroom(id));
    }

    @Transactional
    public ClassroomResponse create(
            @NotNull @Valid ClassroomRequest request
    ) {
        String normalizedCode = normalizeCode(request.code());

        if (classroomRepository.existsByCode(normalizedCode)) {
            throw duplicateCode();
        }

        Classroom classroom = new Classroom(
                normalizedCode,
                request.name(),
                request.building(),
                request.floorNumber(),
                request.capacity(),
                request.roomType(),
                request.hasProjector(),
                request.accessible()
        );

        return save(classroom);
    }

    @Transactional
    public ClassroomResponse update(
            @NotNull @Positive Long id,
            @NotNull @Valid ClassroomUpdateRequest request
    ) {
        Classroom classroom = findClassroom(id);

        checkVersion(classroom, request.version());

        String normalizedCode = normalizeCode(request.code());

        if (classroomRepository.existsByCodeAndIdNot(
                normalizedCode,
                id
        )) {
            throw duplicateCode();
        }

        classroom.updateDetails(
                normalizedCode,
                request.name(),
                request.building(),
                request.floorNumber(),
                request.capacity(),
                request.roomType(),
                request.hasProjector(),
                request.accessible()
        );

        return save(classroom);
    }

    @Transactional
    public ClassroomResponse updateStatus(
            @NotNull @Positive Long id,
            @NotNull @Valid ClassroomStatusRequest request
    ) {
        Classroom classroom = findClassroom(id);

        checkVersion(classroom, request.version());

        classroom.setEnabled(request.enabled());

        return save(classroom);
    }

    private Classroom findClassroom(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Classroom not found."
                ));
    }

    private void checkVersion(
            Classroom classroom,
            long submittedVersion
    ) {
        if (classroom.getVersion() != submittedVersion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This classroom has changed. "
                    + "Refresh it before saving again."
            );
        }
    }

    private ClassroomResponse save(Classroom classroom) {
        try {
            Classroom saved =
                    classroomRepository.saveAndFlush(classroom);

            return ClassroomResponse.fromEntity(saved);

        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another request changed this classroom. "
                    + "Refresh it before saving again."
            );

        } catch (DataIntegrityViolationException exception) {
            /*
             * Database constraints also protect against concurrent
             * requests attempting to save the same classroom code.
             */
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The classroom conflicts with stored data. "
                    + "Check the room code and refresh before retrying."
            );
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException duplicateCode() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A classroom with this code already exists."
        );
    }
}