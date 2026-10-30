package com.classroomallocation.backend.allocation;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.classroomallocation.backend.enums.RoomType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DraftScheduleValidatorTest {

    private final DraftScheduleValidator validator =
            new DraftScheduleValidator();

    @Test
    void acceptsACompleteValidSchedule() {
        var result = validator.validate(
                List.of(requirement(1, 1, 6, 24)),
                List.of(room(40)),
                List.of(period("09:00", "10:00")),
                List.of(session("09:00", "10:00"))
        );

        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void rejectsMissingSessions() {
        var result = validator.validate(
                List.of(requirement(2, 1, 6, 24)),
                List.of(room(40)),
                List.of(period("09:00", "10:00")),
                List.of(session("09:00", "10:00"))
        );

        assertFalse(result.valid());

        assertTrue(result.errors().stream().anyMatch(
                error -> error.contains("needs 2 sessions")
        ));
    }

    @Test
    void rejectsARoomThatIsNowTooSmall() {
        var result = validator.validate(
                List.of(requirement(1, 1, 6, 24)),
                List.of(room(20)),
                List.of(period("09:00", "10:00")),
                List.of(session("09:00", "10:00"))
        );

        assertFalse(result.valid());

        assertTrue(result.errors().stream().anyMatch(
                error -> error.contains("no longer meets")
        ));
    }

    @Test
    void rejectsOverlappingSavedSessions() {
        var result = validator.validate(
                List.of(requirement(2, 1, 6, 24)),
                List.of(room(40)),
                List.of(period("09:00", "10:00")),
                List.of(
                        session("09:00", "10:00"),
                        session("09:00", "10:00")
                )
        );

        assertFalse(result.valid());

        assertTrue(result.errors().stream().anyMatch(
                error -> error.contains("clash")
        ));
    }

    @Test
    void rejectsASessionCrossingABreak() {
        var result = validator.validate(
                List.of(requirement(1, 2, 6, 24)),
                List.of(room(40)),
                List.of(
                        period("09:00", "10:00"),
                        period("10:30", "11:30")
                ),
                List.of(session("09:00", "11:30"))
        );

        assertFalse(result.valid());

        assertTrue(result.errors().stream().anyMatch(
                error -> error.contains("consecutive teaching periods")
        ));
    }

    @Test
    void rejectsExcessiveDailyWorkload() {
        var result = validator.validate(
                List.of(requirement(2, 1, 1, 24)),
                List.of(room(40)),
                List.of(
                        period("09:00", "10:00"),
                        period("10:00", "11:00")
                ),
                List.of(
                        session("09:00", "10:00"),
                        session("10:00", "11:00")
                )
        );

        assertFalse(result.valid());

        assertTrue(result.errors().stream().anyMatch(
                error -> error.contains("daily teaching limit")
        ));
    }

    private static AllocationEngine.Requirement requirement(
            int sessionsPerWeek,
            int periodsPerSession,
            int dailyLimit,
            int weeklyLimit
    ) {
        return new AllocationEngine.Requirement(
                1,
                10,
                100,
                new RoomNeed(
                        30,
                        RoomType.LECTURE_HALL,
                        false,
                        false
                ),
                sessionsPerWeek,
                periodsPerSession,
                dailyLimit,
                weeklyLimit
        );
    }

    private static RoomCandidate room(int capacity) {
        return new RoomCandidate(
                1,
                "ROOM-101",
                capacity,
                RoomType.LECTURE_HALL,
                false,
                false,
                true
        );
    }

    private static AllocationEngine.TeachingPeriod period(
            String start,
            String end
    ) {
        return new AllocationEngine.TeachingPeriod(
                1,
                LocalTime.parse(start),
                LocalTime.parse(end)
        );
    }

    private static ScheduledSession session(
            String start,
            String end
    ) {
        return new ScheduledSession(
                1,
                1,
                10,
                100,
                1,
                LocalTime.parse(start),
                LocalTime.parse(end)
        );
    }
}