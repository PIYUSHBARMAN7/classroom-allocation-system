package com.classroomallocation.backend.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.classroomallocation.backend.enums.SlotType;

public record TimeSlotUpdateRequest(

        @NotBlank
        @Size(max = 60)
        String label,

        @NotNull
        @Min(1)
        @Max(7)
        Integer dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        SlotType slotType,

        @NotNull
        @PositiveOrZero
        Long version
) {

    @AssertTrue(message = "End time must be after start time.")
    public boolean isTimeRangeValid() {
        return startTime == null
                || endTime == null
                || startTime.isBefore(endTime);
    }

    @AssertTrue(message = "Times must use whole minutes.")
    public boolean isMinutePrecisionValid() {
        return isWholeMinute(startTime)
                && isWholeMinute(endTime);
    }

    private static boolean isWholeMinute(LocalTime time) {
        return time == null
                || (time.getSecond() == 0 && time.getNano() == 0);
    }
}