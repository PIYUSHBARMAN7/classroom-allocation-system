package com.classroomallocation.backend.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.allocation.AllocationEngine;
import com.classroomallocation.backend.allocation.RoomCandidate;
import com.classroomallocation.backend.allocation.RoomNeed;
import com.classroomallocation.backend.dto.request.AllocationPreviewRequest;
import com.classroomallocation.backend.entity.ClassRequirement;
import com.classroomallocation.backend.enums.SlotType;
import com.classroomallocation.backend.repository.ClassRequirementRepository;
import com.classroomallocation.backend.repository.ClassroomRepository;
import com.classroomallocation.backend.repository.TimeSlotRepository;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AllocationPreviewService {

    private static final int MAX_ROOMS = 100;
    private static final int MAX_TEACHING_PERIODS = 100;
    private static final int MAX_SESSIONS = 50;

    private final ClassRequirementRepository requirementRepository;
    private final ClassroomRepository classroomRepository;
    private final TimeSlotRepository timeSlotRepository;

    private final AllocationEngine engine = new AllocationEngine();

    public AllocationPreviewService(
            ClassRequirementRepository requirementRepository,
            ClassroomRepository classroomRepository,
            TimeSlotRepository timeSlotRepository
    ) {
        this.requirementRepository = requirementRepository;
        this.classroomRepository = classroomRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public AllocationEngine.Result preview(
            AllocationPreviewRequest request
    ) {
        List<ClassRequirement> requirements =
                requirementRepository.findAllById(request.requirementIds());

        if (requirements.size() != request.requirementIds().size()) {
            throw badRequest(
                    "One or more selected requirements do not exist."
            );
        }

        for (ClassRequirement requirement : requirements) {
            validateRequirement(requirement);
        }

        int requestedSessions = requirements.stream()
                .mapToInt(ClassRequirement::getSessionsPerWeek)
                .sum();

        if (requestedSessions > MAX_SESSIONS) {
            throw badRequest(
                    "Select requirements totalling no more than 50 sessions."
            );
        }

        var roomPage = classroomRepository.findByEnabled(
                true,
                PageRequest.of(0, MAX_ROOMS + 1)
        );

        if (roomPage.getTotalElements() > MAX_ROOMS) {
            throw badRequest(
                    "This preview currently supports up to 100 enabled rooms."
            );
        }

        List<RoomCandidate> rooms = roomPage.getContent().stream()
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

        /*
         * Fetch a bounded number of enabled slots.
         * BREAK slots are excluded below.
         */
        var slotPage = timeSlotRepository.findByEnabled(
                true,
                PageRequest.of(0, MAX_TEACHING_PERIODS + 1)
        );

        if (slotPage.getTotalElements() > MAX_TEACHING_PERIODS) {
            throw badRequest(
                    "This preview currently supports up to 100 enabled time slots."
            );
        }

        List<AllocationEngine.TeachingPeriod> teachingPeriods =
                slotPage.getContent().stream()
                        .filter(slot ->
                                slot.getSlotType() == SlotType.TEACHING)
                        .map(slot -> new AllocationEngine.TeachingPeriod(
                                slot.getDayOfWeek(),
                                slot.getStartTime(),
                                slot.getEndTime()
                        ))
                        .toList();

        List<AllocationEngine.Requirement> engineRequirements =
                requirements.stream()
                        .map(this::toEngineRequirement)
                        .toList();

        return engine.allocate(
                engineRequirements,
                rooms,
                teachingPeriods
        );
    }

    private void validateRequirement(ClassRequirement requirement) {
        if (!requirement.isEnabled()
                || !requirement.getSection().isEnabled()
                || !requirement.getSubject().isEnabled()
                || !requirement.getTeacher().isEnabled()) {
            throw badRequest(
                    "Selected requirements and their sections, subjects "
                            + "and teachers must be enabled."
            );
        }

        var teacher = requirement.getTeacher();

        if (requirement.getPeriodsPerSession()
                > teacher.getMaxPeriodsPerDay()) {
            throw badRequest(
                    "A session exceeds its teacher's daily period limit."
            );
        }
    }

    private AllocationEngine.Requirement toEngineRequirement(
            ClassRequirement requirement
    ) {
        var section = requirement.getSection();
        var subject = requirement.getSubject();
        var teacher = requirement.getTeacher();

        RoomNeed roomNeed = new RoomNeed(
                section.getStudentCount(),
                subject.getRequiredRoomType(),
                requirement.isRequiresProjector(),
                requirement.isRequiresAccessibleRoom()
        );

        return new AllocationEngine.Requirement(
                requirement.getId(),
                teacher.getId(),
                section.getId(),
                roomNeed,
                requirement.getSessionsPerWeek(),
                requirement.getPeriodsPerSession(),
                teacher.getMaxPeriodsPerDay(),
                teacher.getMaxPeriodsPerWeek()
        );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}