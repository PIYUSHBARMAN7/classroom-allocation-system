package com.classroomallocation.backend.dto.response;

import java.time.Instant;
import java.time.LocalTime;

import com.classroomallocation.backend.entity.TimeSlot;
import com.classroomallocation.backend.enums.SlotType;

public record TimeSlotResponse(
        Long id,
        String label,
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        SlotType slotType,
        boolean enabled,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public static TimeSlotResponse fromEntity(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getLabel(),
                timeSlot.getDayOfWeek(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getSlotType(),
                timeSlot.isEnabled(),
                timeSlot.getVersion(),
                timeSlot.getCreatedAt(),
                timeSlot.getUpdatedAt()
        );
    }
}