package com.classroomallocation.backend.service;

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

import com.classroomallocation.backend.dto.request.ClassRequirementRequest;
import com.classroomallocation.backend.dto.request.ClassRequirementStatusRequest;
import com.classroomallocation.backend.dto.request.ClassRequirementUpdateRequest;
import com.classroomallocation.backend.dto.response.ClassRequirementResponse;
import com.classroomallocation.backend.entity.ClassRequirement;
import com.classroomallocation.backend.entity.Section;
import com.classroomallocation.backend.entity.Subject;
import com.classroomallocation.backend.entity.Teacher;
import com.classroomallocation.backend.repository.ClassRequirementRepository;
import com.classroomallocation.backend.repository.SectionRepository;
import com.classroomallocation.backend.repository.SubjectRepository;
import com.classroomallocation.backend.repository.TeacherRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class RequirementService {

    private final ClassRequirementRepository requirementRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public RequirementService(
            ClassRequirementRepository requirementRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TeacherRepository teacherRepository
    ) {
        this.requirementRepository = requirementRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
    }

    public Page<ClassRequirementResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            Boolean enabled
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        Page<ClassRequirement> requirements;

        if (enabled == null) {
            requirements = requirementRepository.findAll(pageable);
        } else {
            requirements = requirementRepository.findByEnabled(
                    enabled,
                    pageable
            );
        }

        return requirements.map(
                ClassRequirementResponse::fromEntity
        );
    }

    public ClassRequirementResponse getById(
            @NotNull @Positive Long id
    ) {
        return ClassRequirementResponse.fromEntity(
                findRequirement(id)
        );
    }

    @Transactional
    public ClassRequirementResponse create(
            @NotNull @Valid ClassRequirementRequest request
    ) {
        References references = loadReferences(
                request.sectionId(),
                request.subjectId(),
                request.teacherId()
        );

        ensureUsable(
                references,
                request.sessionsPerWeek(),
                request.periodsPerSession()
        );

        ensureUnique(
                request.sectionId(),
                request.subjectId(),
                null
        );

        ClassRequirement requirement = new ClassRequirement(
                references.section(),
                references.subject(),
                references.teacher(),
                request.sessionsPerWeek(),
                request.periodsPerSession(),
                request.requiresProjector(),
                request.requiresAccessibleRoom()
        );

        return save(requirement);
    }

    @Transactional
    public ClassRequirementResponse update(
            @NotNull @Positive Long id,
            @NotNull @Valid ClassRequirementUpdateRequest request
    ) {
        ClassRequirement requirement = findRequirement(id);

        checkVersion(requirement, request.version());

        References references = loadReferences(
                request.sectionId(),
                request.subjectId(),
                request.teacherId()
        );

        if (requirement.isEnabled()) {
            ensureUsable(
                    references,
                    request.sessionsPerWeek(),
                    request.periodsPerSession()
            );
        }

        ensureUnique(
                request.sectionId(),
                request.subjectId(),
                id
        );

        requirement.updateDetails(
                references.section(),
                references.subject(),
                references.teacher(),
                request.sessionsPerWeek(),
                request.periodsPerSession(),
                request.requiresProjector(),
                request.requiresAccessibleRoom()
        );

        return save(requirement);
    }

    @Transactional
    public ClassRequirementResponse updateStatus(
            @NotNull @Positive Long id,
            @NotNull @Valid ClassRequirementStatusRequest request
    ) {
        ClassRequirement requirement = findRequirement(id);

        checkVersion(requirement, request.version());

        if (request.enabled()) {
            ensureUsable(
                    new References(
                            requirement.getSection(),
                            requirement.getSubject(),
                            requirement.getTeacher()
                    ),
                    requirement.getSessionsPerWeek(),
                    requirement.getPeriodsPerSession()
            );
        }

        requirement.setEnabled(request.enabled());

        return save(requirement);
    }

    private References loadReferences(
            Long sectionId,
            Long subjectId,
            Long teacherId
    ) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> notFound("Section"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> notFound("Subject"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> notFound("Teacher"));

        return new References(section, subject, teacher);
    }

    private void ensureUsable(
            References references,
            int sessionsPerWeek,
            int periodsPerSession
    ) {
        if (!references.section().isEnabled()) {
            throw badRequest("The selected section is inactive.");
        }

        if (!references.subject().isEnabled()) {
            throw badRequest("The selected subject is inactive.");
        }

        Teacher teacher = references.teacher();

        if (!teacher.isEnabled()) {
            throw badRequest("The selected teacher is inactive.");
        }

        if (periodsPerSession > teacher.getMaxPeriodsPerDay()) {
            throw badRequest(
                    "One session exceeds the teacher's daily period limit."
            );
        }

        int weeklyPeriods = sessionsPerWeek * periodsPerSession;

        if (weeklyPeriods > teacher.getMaxPeriodsPerWeek()) {
            throw badRequest(
                    "This requirement exceeds the teacher's "
                    + "weekly period limit."
            );
        }
    }

    private void ensureUnique(
            Long sectionId,
            Long subjectId,
            Long excludedId
    ) {
        boolean exists;

        if (excludedId == null) {
            exists = requirementRepository
                    .existsBySection_IdAndSubject_Id(
                            sectionId,
                            subjectId
                    );
        } else {
            exists = requirementRepository
                    .existsBySection_IdAndSubject_IdAndIdNot(
                            sectionId,
                            subjectId,
                            excludedId
                    );
        }

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A requirement already exists for this section "
                    + "and subject. Edit or reactivate that requirement."
            );
        }
    }

    private ClassRequirement findRequirement(Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> notFound("Class requirement"));
    }

    private void checkVersion(
            ClassRequirement requirement,
            long submittedVersion
    ) {
        if (requirement.getVersion() != submittedVersion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This requirement has changed. "
                    + "Refresh it before saving again."
            );
        }
    }

    private ClassRequirementResponse save(
            ClassRequirement requirement
    ) {
        try {
            ClassRequirement saved =
                    requirementRepository.saveAndFlush(requirement);

            return ClassRequirementResponse.fromEntity(saved);

        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another request changed this requirement. "
                    + "Refresh it before saving again."
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This requirement conflicts with stored data. "
                    + "Check the selected section, subject and teacher."
            );
        }
    }

    private ResponseStatusException notFound(String resource) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                resource + " not found."
        );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    private record References(
            Section section,
            Subject subject,
            Teacher teacher
    ) {
    }
}