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

import com.classroomallocation.backend.dto.request.SectionRequest;
import com.classroomallocation.backend.dto.request.SectionStatusRequest;
import com.classroomallocation.backend.dto.request.SectionUpdateRequest;
import com.classroomallocation.backend.dto.response.SectionResponse;
import com.classroomallocation.backend.entity.Section;
import com.classroomallocation.backend.repository.SectionRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public Page<SectionResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            Boolean enabled
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("academicYearStart"),
                        Sort.Order.asc("programCode"),
                        Sort.Order.asc("semester"),
                        Sort.Order.asc("code")
                )
        );

        Page<Section> sections;

        if (enabled == null) {
            sections = sectionRepository.findAll(pageable);
        } else {
            sections = sectionRepository.findByEnabled(
                    enabled,
                    pageable
            );
        }

        return sections.map(SectionResponse::fromEntity);
    }

    public SectionResponse getById(
            @NotNull @Positive Long id
    ) {
        return SectionResponse.fromEntity(findSection(id));
    }

    @Transactional
    public SectionResponse create(
            @NotNull @Valid SectionRequest request
    ) {
        String code = normalizeCode(request.code());
        String programCode = normalizeCode(request.programCode());

        ensureUnique(
                programCode,
                request.academicYearStart(),
                request.semester(),
                code,
                null
        );

        Section section = new Section(
                code,
                request.name(),
                programCode,
                request.department(),
                request.semester(),
                request.academicYearStart(),
                request.studentCount()
        );

        return save(section);
    }

    @Transactional
    public SectionResponse update(
            @NotNull @Positive Long id,
            @NotNull @Valid SectionUpdateRequest request
    ) {
        Section section = findSection(id);

        checkVersion(section, request.version());

        String code = normalizeCode(request.code());
        String programCode = normalizeCode(request.programCode());

        ensureUnique(
                programCode,
                request.academicYearStart(),
                request.semester(),
                code,
                id
        );

        section.updateDetails(
                code,
                request.name(),
                programCode,
                request.department(),
                request.semester(),
                request.academicYearStart(),
                request.studentCount()
        );

        return save(section);
    }

    @Transactional
    public SectionResponse updateStatus(
            @NotNull @Positive Long id,
            @NotNull @Valid SectionStatusRequest request
    ) {
        Section section = findSection(id);

        checkVersion(section, request.version());

        section.setEnabled(request.enabled());

        return save(section);
    }

    private Section findSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Section not found."
                ));
    }

    private void checkVersion(
            Section section,
            long submittedVersion
    ) {
        if (section.getVersion() != submittedVersion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This section has changed. "
                    + "Refresh it before saving again."
            );
        }
    }

    private void ensureUnique(
            String programCode,
            int academicYearStart,
            int semester,
            String code,
            Long excludedId
    ) {
        boolean exists;

        if (excludedId == null) {
            exists = sectionRepository
                    .existsByProgramCodeAndAcademicYearStartAndSemesterAndCode(
                            programCode,
                            academicYearStart,
                            semester,
                            code
                    );
        } else {
            exists = sectionRepository
                    .existsByProgramCodeAndAcademicYearStartAndSemesterAndCodeAndIdNot(
                            programCode,
                            academicYearStart,
                            semester,
                            code,
                            excludedId
                    );
        }

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This section already exists for the selected "
                    + "program, academic year and semester."
            );
        }
    }

    private SectionResponse save(Section section) {
        try {
            Section saved =
                    sectionRepository.saveAndFlush(section);

            return SectionResponse.fromEntity(saved);

        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another request changed this section. "
                    + "Refresh it before saving again."
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The section conflicts with stored data. "
                    + "Check its program, year, semester and code."
            );
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}