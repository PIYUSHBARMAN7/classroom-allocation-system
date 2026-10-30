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

import com.classroomallocation.backend.dto.request.SubjectRequest;
import com.classroomallocation.backend.dto.request.SubjectStatusRequest;
import com.classroomallocation.backend.dto.request.SubjectUpdateRequest;
import com.classroomallocation.backend.dto.response.SubjectResponse;
import com.classroomallocation.backend.entity.Subject;
import com.classroomallocation.backend.repository.SubjectRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Page<SubjectResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            Boolean enabled
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by("code").ascending()
        );

        Page<Subject> subjects;

        if (enabled == null) {
            subjects = subjectRepository.findAll(pageable);
        } else {
            subjects = subjectRepository.findByEnabled(
                    enabled,
                    pageable
            );
        }

        return subjects.map(SubjectResponse::fromEntity);
    }

    public SubjectResponse getById(
            @NotNull @Positive Long id
    ) {
        return SubjectResponse.fromEntity(findSubject(id));
    }

    @Transactional
    public SubjectResponse create(
            @NotNull @Valid SubjectRequest request
    ) {
        String code = normalizeCode(request.code());

        if (subjectRepository.existsByCode(code)) {
            throw duplicateCode();
        }

        Subject subject = new Subject(
                code,
                request.name(),
                request.department(),
                request.requiredRoomType()
        );

        return save(subject);
    }

    @Transactional
    public SubjectResponse update(
            @NotNull @Positive Long id,
            @NotNull @Valid SubjectUpdateRequest request
    ) {
        Subject subject = findSubject(id);

        checkVersion(subject, request.version());

        String code = normalizeCode(request.code());

        if (subjectRepository.existsByCodeAndIdNot(code, id)) {
            throw duplicateCode();
        }

        subject.updateDetails(
                code,
                request.name(),
                request.department(),
                request.requiredRoomType()
        );

        return save(subject);
    }

    @Transactional
    public SubjectResponse updateStatus(
            @NotNull @Positive Long id,
            @NotNull @Valid SubjectStatusRequest request
    ) {
        Subject subject = findSubject(id);

        checkVersion(subject, request.version());

        subject.setEnabled(request.enabled());

        return save(subject);
    }

    private Subject findSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Subject not found."
                ));
    }

    private void checkVersion(
            Subject subject,
            long submittedVersion
    ) {
        if (subject.getVersion() != submittedVersion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This subject has changed. "
                    + "Refresh it before saving again."
            );
        }
    }

    private SubjectResponse save(Subject subject) {
        try {
            Subject saved =
                    subjectRepository.saveAndFlush(subject);

            return SubjectResponse.fromEntity(saved);

        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another request changed this subject. "
                    + "Refresh it before saving again."
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The subject conflicts with stored data. "
                    + "Check the subject code and refresh before retrying."
            );
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException duplicateCode() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A subject with this code already exists."
        );
    }
}