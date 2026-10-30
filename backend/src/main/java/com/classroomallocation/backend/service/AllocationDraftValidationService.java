package com.classroomallocation.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.allocation.AllocationEngine;
import com.classroomallocation.backend.allocation.DraftScheduleValidator;
import com.classroomallocation.backend.allocation.RoomCandidate;
import com.classroomallocation.backend.allocation.RoomNeed;
import com.classroomallocation.backend.allocation.ScheduledSession;
import com.classroomallocation.backend.dto.response.AllocationDraftValidationResponse;
import com.classroomallocation.backend.enums.SlotType;
import com.classroomallocation.backend.repository.AllocationDraftRepository;
import com.classroomallocation.backend.repository.AllocationDraftRequirementRepository;
import com.classroomallocation.backend.repository.AllocationDraftSessionRepository;
import com.classroomallocation.backend.repository.ClassRequirementRepository;
import com.classroomallocation.backend.repository.ClassroomRepository;
import com.classroomallocation.backend.repository.TimeSlotRepository;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AllocationDraftValidationService {

    private final AllocationDraftRepository draftRepository;
    private final AllocationDraftRequirementRepository draftRequirementRepository;
    private final AllocationDraftSessionRepository draftSessionRepository;
    private final ClassRequirementRepository requirementRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;

    private final DraftScheduleValidator validator =
            new DraftScheduleValidator();

    public AllocationDraftValidationService(
            AllocationDraftRepository draftRepository,
            AllocationDraftRequirementRepository draftRequirementRepository,
            AllocationDraftSessionRepository draftSessionRepository,
            ClassRequirementRepository requirementRepository,
            ClassroomRepository classroomRepository,
            TimeSlotRepository timeSlotRepository
    ) {
        this.draftRepository = draftRepository;
        this.draftRequirementRepository = draftRequirementRepository;
        this.draftSessionRepository = draftSessionRepository;
        this.requirementRepository = requirementRepository;
        this.classroomRepository = classroomRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )
    public AllocationDraftValidationResponse validate(Long draftId) {
        var draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Allocation draft not found."
                ));

        var snapshots =
                draftRequirementRepository
                        .findByDraftIdOrderByRequirementIdAsc(draftId);

        var savedRows =
                draftSessionRepository
                        .findByDraftIdOrderByDayOfWeekAscStartTimeAscIdAsc(
                                draftId
                        );

        if (snapshots.size() > 25 || savedRows.size() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This draft exceeds the supported validation limits."
            );
        }

        var requirementIds = snapshots.stream()
                .map(snapshot -> snapshot.getRequirementId())
                .toList();

        var currentRequirements =
                requirementRepository.findAllById(requirementIds);

        var currentById = currentRequirements.stream()
                .collect(Collectors.toMap(
                        requirement -> requirement.getId(),
                        Function.identity()
                ));

        List<String> errors = new ArrayList<>();
        List<AllocationEngine.Requirement> engineRequirements =
                new ArrayList<>();

        for (var snapshot : snapshots) {
            var current = currentById.get(snapshot.getRequirementId());

            if (current == null) {
                errors.add(
                        "Requirement #" + snapshot.getRequirementId()
                                + " no longer exists."
                );
                continue;
            }

            var section = current.getSection();
            var subject = current.getSubject();
            var teacher = current.getTeacher();

            if (!current.isEnabled()
                    || !section.isEnabled()
                    || !subject.isEnabled()
                    || !teacher.isEnabled()) {
                errors.add(
                        "Requirement #" + current.getId()
                                + " or its section, subject or teacher is disabled."
                );
            }

            if (snapshot.getSessionsRequested()
                    != current.getSessionsPerWeek()
                    || snapshot.getPeriodsPerSession()
                    != current.getPeriodsPerSession()) {
                errors.add(
                        "The session requirements changed for requirement #"
                                + current.getId()
                                + ". Generate a new draft."
                );
            }

            engineRequirements.add(
                    new AllocationEngine.Requirement(
                            current.getId(),
                            teacher.getId(),
                            section.getId(),
                            new RoomNeed(
                                    section.getStudentCount(),
                                    subject.getRequiredRoomType(),
                                    current.isRequiresProjector(),
                                    current.isRequiresAccessibleRoom()
                            ),
                            current.getSessionsPerWeek(),
                            current.getPeriodsPerSession(),
                            teacher.getMaxPeriodsPerDay(),
                            teacher.getMaxPeriodsPerWeek()
                    )
            );
        }

        var roomIds = savedRows.stream()
                .map(row -> row.getRoomId())
                .distinct()
                .toList();

        List<RoomCandidate> rooms =
                classroomRepository.findAllById(roomIds).stream()
                        .map(room -> new RoomCandidate(
                                room.getId(),
                                room.getCode(),
                                room.getCapacity(),
                                room.getRoomType(),
                                room.isHasProjector(),
                                room.isAccessible(),
                                room.isEnabled()
                        ))
                        .toList();

        var slotPage = timeSlotRepository.findByEnabled(
                true,
                PageRequest.of(0, 101)
        );

        if (slotPage.getTotalElements() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Validation currently supports up to 100 enabled time slots."
            );
        }

        var teachingPeriods = slotPage.getContent().stream()
                .filter(slot -> slot.getSlotType() == SlotType.TEACHING)
                .map(slot -> new AllocationEngine.TeachingPeriod(
                        slot.getDayOfWeek(),
                        slot.getStartTime(),
                        slot.getEndTime()
                ))
                .toList();

        var savedSessions = savedRows.stream()
                .map(row -> new ScheduledSession(
                        row.getRequirementId(),
                        row.getRoomId(),
                        row.getTeacherId(),
                        row.getSectionId(),
                        row.getDayOfWeek(),
                        row.getStartTime(),
                        row.getEndTime()
                ))
                .toList();

        var result = validator.validate(
                engineRequirements,
                rooms,
                teachingPeriods,
                savedSessions
        );

        errors.addAll(result.errors());

        List<String> uniqueErrors = errors.stream()
                .distinct()
                .toList();

        return new AllocationDraftValidationResponse(
                draft.getId(),
                draft.getVersion(),
                uniqueErrors.isEmpty(),
                uniqueErrors
        );
    }
}