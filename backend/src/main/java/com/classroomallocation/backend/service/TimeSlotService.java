package com.classroomallocation.backend.service;

import java.time.LocalTime;

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

import com.classroomallocation.backend.dto.request.TimeSlotRequest;
import com.classroomallocation.backend.dto.request.TimeSlotStatusRequest;
import com.classroomallocation.backend.dto.request.TimeSlotUpdateRequest;
import com.classroomallocation.backend.dto.response.TimeSlotResponse;
import com.classroomallocation.backend.entity.TimeSlot;
import com.classroomallocation.backend.repository.TimeSlotRepository;

@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(
            TimeSlotRepository timeSlotRepository
    ) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public Page<TimeSlotResponse> list(
            @Min(0) int page,
            @Min(1) @Max(100) int size,
            Boolean enabled
    ) {
        var pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("dayOfWeek"),
                        Sort.Order.asc("startTime"),
                        Sort.Order.asc("endTime")
                )
        );

        Page<TimeSlot> slots;

        if (enabled == null) {
            slots = timeSlotRepository.findAll(pageable);
        } else {
            slots = timeSlotRepository.findByEnabled(
                    enabled,
                    pageable
            );
        }

        return slots.map(TimeSlotResponse::fromEntity);
    }

    public TimeSlotResponse getById(
            @NotNull @Positive Long id
    ) {
        return TimeSlotResponse.fromEntity(findTimeSlot(id));
    }

    @Transactional
    public TimeSlotResponse create(
            @NotNull @Valid TimeSlotRequest request
    ) {
        ensureUnique(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                null
        );

        ensureNoActiveOverlap(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                null
        );

        TimeSlot slot = new TimeSlot(
                request.label(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.slotType()
        );

        return save(slot);
    }

    @Transactional
    public TimeSlotResponse update(
            @NotNull @Positive Long id,
            @NotNull @Valid TimeSlotUpdateRequest request
    ) {
        TimeSlot slot = findTimeSlot(id);

        checkVersion(slot, request.version());

        ensureUnique(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                id
        );

        if (slot.isEnabled()) {
            ensureNoActiveOverlap(
                    request.dayOfWeek(),
                    request.startTime(),
                    request.endTime(),
                    id
            );
        }

        slot.updateDetails(
                request.label(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.slotType()
        );

        return save(slot);
    }

    @Transactional
    public TimeSlotResponse updateStatus(
            @NotNull @Positive Long id,
            @NotNull @Valid TimeSlotStatusRequest request
    ) {
        TimeSlot slot = findTimeSlot(id);

        checkVersion(slot, request.version());

        if (request.enabled()) {
            ensureNoActiveOverlap(
                    slot.getDayOfWeek(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    id
            );
        }

        slot.setEnabled(request.enabled());

        return save(slot);
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Time slot not found."
                ));
    }

    private void checkVersion(
            TimeSlot slot,
            long submittedVersion
    ) {
        if (slot.getVersion() != submittedVersion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This time slot has changed. "
                    + "Refresh it before saving again."
            );
        }
    }

    private void ensureUnique(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludedId
    ) {
        boolean exists;

        if (excludedId == null) {
            exists = timeSlotRepository
                    .existsByDayOfWeekAndStartTimeAndEndTime(
                            dayOfWeek,
                            startTime,
                            endTime
                    );
        } else {
            exists = timeSlotRepository
                    .existsByDayOfWeekAndStartTimeAndEndTimeAndIdNot(
                            dayOfWeek,
                            startTime,
                            endTime,
                            excludedId
                    );
        }

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A time slot already exists for this day "
                    + "and time range. Edit or reactivate that slot."
            );
        }
    }

    private void ensureNoActiveOverlap(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long excludedId
    ) {
        long overlaps = timeSlotRepository.countActiveOverlaps(
                dayOfWeek,
                startTime,
                endTime,
                excludedId
        );

        if (overlaps > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This period overlaps an active teaching "
                    + "period or break on the same day."
            );
        }
    }

    private TimeSlotResponse save(TimeSlot slot) {
        try {
            TimeSlot saved =
                    timeSlotRepository.saveAndFlush(slot);

            return TimeSlotResponse.fromEntity(saved);

        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another request changed this time slot. "
                    + "Refresh it before saving again."
            );

        } catch (DataIntegrityViolationException exception) {
            /*
             * Database constraints also reject duplicates and
             * overlapping active periods from concurrent requests.
             */
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This time slot conflicts with stored data. "
                    + "Check for duplicate or overlapping periods."
            );
        }
    }
}