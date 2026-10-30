package com.classroomallocation.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.allocation.AllocationEngine;
import com.classroomallocation.backend.dto.request.AllocationPreviewRequest;
import com.classroomallocation.backend.dto.request.CreateAllocationDraftRequest;
import com.classroomallocation.backend.dto.response.AllocationDraftCreatedResponse;
import com.classroomallocation.backend.entity.AllocationDraft;
import com.classroomallocation.backend.entity.AllocationDraftRequirement;
import com.classroomallocation.backend.entity.AllocationDraftSession;
import com.classroomallocation.backend.entity.ClassRequirement;
import com.classroomallocation.backend.repository.AllocationDraftRepository;
import com.classroomallocation.backend.repository.AllocationDraftRequirementRepository;
import com.classroomallocation.backend.repository.AllocationDraftSessionRepository;
import com.classroomallocation.backend.repository.ClassRequirementRepository;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AllocationDraftService {

    private final AllocationPreviewService previewService;
    private final AllocationDraftRepository draftRepository;
    private final AllocationDraftRequirementRepository draftRequirementRepository;
    private final AllocationDraftSessionRepository draftSessionRepository;
    private final ClassRequirementRepository requirementRepository;

    public AllocationDraftService(
            AllocationPreviewService previewService,
            AllocationDraftRepository draftRepository,
            AllocationDraftRequirementRepository draftRequirementRepository,
            AllocationDraftSessionRepository draftSessionRepository,
            ClassRequirementRepository requirementRepository
    ) {
        this.previewService = previewService;
        this.draftRepository = draftRepository;
        this.draftRequirementRepository = draftRequirementRepository;
        this.draftSessionRepository = draftSessionRepository;
        this.requirementRepository = requirementRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AllocationDraftCreatedResponse create(
            CreateAllocationDraftRequest request,
            String authenticatedEmail
    ) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        /*
         * Generate from database records.
         * Never accept room assignments supplied by the browser.
         */
        AllocationEngine.Result result = previewService.preview(
                new AllocationPreviewRequest(request.requirementIds())
        );

        /*
         * REPEATABLE_READ keeps these reads consistent with the
         * database snapshot used to generate this draft.
         */
        List<ClassRequirement> requirements =
                requirementRepository.findAllById(request.requirementIds());

        AllocationDraft draft = draftRepository.saveAndFlush(
                new AllocationDraft(
                        request.name().strip(),
                        authenticatedEmail
                )
        );

        List<AllocationDraftRequirement> selectedRequirements =
                requirements.stream()
                        .map(requirement ->
                                new AllocationDraftRequirement(
                                        draft.getId(),
                                        requirement.getId(),
                                        requirement.getSessionsPerWeek(),
                                        requirement.getPeriodsPerSession()
                                ))
                        .toList();

        /*
         * Insert requirement rows before session rows because
         * sessions have a foreign key to these requirements.
         */
        draftRequirementRepository.saveAllAndFlush(selectedRequirements);

        Map<Long, Integer> sessionNumbers = new HashMap<>();

        List<AllocationDraftSession> sessions =
                result.allocations().stream()
                        .map(session -> {
                            int sessionNumber = sessionNumbers.merge(
                                    session.requirementId(),
                                    1,
                                    Integer::sum
                            );

                            return new AllocationDraftSession(
                                    draft.getId(),
                                    session.requirementId(),
                                    sessionNumber,
                                    session.roomId(),
                                    session.teacherId(),
                                    session.sectionId(),
                                    session.dayOfWeek(),
                                    session.startTime(),
                                    session.endTime()
                            );
                        })
                        .toList();

        draftSessionRepository.saveAllAndFlush(sessions);

        return new AllocationDraftCreatedResponse(
                draft.getId(),
                draft.getName(),
                draft.getVersion(),
                draft.getCreatedAt(),
                result
        );
    }
}