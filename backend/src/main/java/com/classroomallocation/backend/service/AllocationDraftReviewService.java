package com.classroomallocation.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.response.AllocationDraftDetailResponse;
import com.classroomallocation.backend.dto.response.AllocationDraftPageResponse;
import com.classroomallocation.backend.dto.response.AllocationDraftSummaryResponse;
import com.classroomallocation.backend.entity.AllocationDraft;
import com.classroomallocation.backend.entity.AllocationDraftSession;
import com.classroomallocation.backend.repository.AllocationDraftRepository;
import com.classroomallocation.backend.repository.AllocationDraftRequirementRepository;
import com.classroomallocation.backend.repository.AllocationDraftSessionRepository;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AllocationDraftReviewService {

    private final AllocationDraftRepository draftRepository;
    private final AllocationDraftRequirementRepository requirementRepository;
    private final AllocationDraftSessionRepository sessionRepository;

    public AllocationDraftReviewService(
            AllocationDraftRepository draftRepository,
            AllocationDraftRequirementRepository requirementRepository,
            AllocationDraftSessionRepository sessionRepository
    ) {
        this.draftRepository = draftRepository;
        this.requirementRepository = requirementRepository;
        this.sessionRepository = sessionRepository;
    }

    public AllocationDraftPageResponse list(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 50."
            );
        }

        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        var result = draftRepository.findAll(pageable);

        List<AllocationDraftSummaryResponse> content =
                result.getContent().stream()
                        .map(this::toSummary)
                        .toList();

        return new AllocationDraftPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )
    public AllocationDraftDetailResponse get(Long draftId) {
        AllocationDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Allocation draft not found."
                ));

        var requirements =
                requirementRepository.findByDraftIdOrderByRequirementIdAsc(
                        draftId
                );

        var sessions =
                sessionRepository
                        .findByDraftIdOrderByDayOfWeekAscStartTimeAscIdAsc(
                                draftId
                        );

        Map<Long, Long> allocatedByRequirement =
                sessions.stream()
                        .collect(Collectors.groupingBy(
                                AllocationDraftSession::getRequirementId,
                                Collectors.counting()
                        ));

        List<AllocationDraftDetailResponse.RequirementSummary>
                requirementSummaries = requirements.stream()
                        .map(requirement -> {
                            int allocated = allocatedByRequirement
                                    .getOrDefault(
                                            requirement.getRequirementId(),
                                            0L
                                    )
                                    .intValue();

                            int missing = Math.max(
                                    0,
                                    requirement.getSessionsRequested()
                                            - allocated
                            );

                            return new AllocationDraftDetailResponse
                                    .RequirementSummary(
                                            requirement.getRequirementId(),
                                            requirement.getSessionsRequested(),
                                            requirement.getPeriodsPerSession(),
                                            allocated,
                                            missing
                                    );
                        })
                        .toList();

        List<AllocationDraftDetailResponse.SessionSummary>
                sessionSummaries = sessions.stream()
                        .map(session ->
                                new AllocationDraftDetailResponse.SessionSummary(
                                        session.getId(),
                                        session.getRequirementId(),
                                        session.getSessionNumber(),
                                        session.getRoomId(),
                                        session.getTeacherId(),
                                        session.getSectionId(),
                                        session.getDayOfWeek(),
                                        session.getStartTime(),
                                        session.getEndTime()
                                ))
                        .toList();

        int requestedSessions = requirementSummaries.stream()
                .mapToInt(
                        AllocationDraftDetailResponse
                                .RequirementSummary::sessionsRequested
                )
                .sum();

        int unallocatedSessions = requirementSummaries.stream()
                .mapToInt(
                        AllocationDraftDetailResponse
                                .RequirementSummary::unallocatedSessions
                )
                .sum();

        boolean allSessionsAllocated =
                !requirementSummaries.isEmpty()
                        && requirementSummaries.stream().allMatch(
                                requirement ->
                                        requirement.allocatedSessions()
                                                == requirement.sessionsRequested()
                        );

        return new AllocationDraftDetailResponse(
                draft.getId(),
                draft.getName(),
                draft.getVersion(),
                draft.getCreatedAt(),
                requestedSessions,
                sessions.size(),
                unallocatedSessions,
                allSessionsAllocated,
                requirementSummaries,
                sessionSummaries
        );
    }

    private AllocationDraftSummaryResponse toSummary(
            AllocationDraft draft
    ) {
        return new AllocationDraftSummaryResponse(
                draft.getId(),
                draft.getName(),
                draft.getVersion(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }
}