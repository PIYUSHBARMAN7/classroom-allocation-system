package com.classroomallocation.backend.allocation;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConflictCheckerTest {

    private final ConflictChecker checker = new ConflictChecker();

    private final ScheduledSession existing = session(
            10, 100, 1000, 1, "09:00", "10:00"
    );

    @Test
    void detectsRoomOverlap() {
        var candidate = session(
                10, 200, 2000, 1, "09:30", "10:30"
        );

        assertEquals(
                Set.of(ConflictType.ROOM),
                conflictTypes(candidate)
        );
    }

    @Test
    void detectsTeacherOverlap() {
        var candidate = session(
                20, 100, 2000, 1, "09:30", "10:30"
        );

        assertEquals(
                Set.of(ConflictType.TEACHER),
                conflictTypes(candidate)
        );
    }

    @Test
    void detectsSectionOverlap() {
        var candidate = session(
                20, 200, 1000, 1, "09:30", "10:30"
        );

        assertEquals(
                Set.of(ConflictType.SECTION),
                conflictTypes(candidate)
        );
    }

    @Test
    void reportsAllThreeSharedResources() {
        var candidate = session(
                10, 100, 1000, 1, "09:00", "10:00"
        );

        assertEquals(
                Set.of(
                        ConflictType.ROOM,
                        ConflictType.TEACHER,
                        ConflictType.SECTION
                ),
                conflictTypes(candidate)
        );

        assertFalse(
                checker.canPlace(candidate, List.of(existing))
        );
    }

    @Test
    void allowsAdjacentSessions() {
        var candidate = session(
                10, 100, 1000, 1, "10:00", "11:00"
        );

        assertTrue(
                checker.canPlace(candidate, List.of(existing))
        );
    }

    @Test
    void allowsSameTimeOnDifferentDays() {
        var candidate = session(
                10, 100, 1000, 2, "09:00", "10:00"
        );

        assertTrue(
                checker.canPlace(candidate, List.of(existing))
        );
    }

    @Test
    void allowsOverlapWhenResourcesAreDifferent() {
        var candidate = session(
                20, 200, 2000, 1, "09:00", "10:00"
        );

        assertTrue(
                checker.canPlace(candidate, List.of(existing))
        );
    }

    @Test
    void detectsContainedAndContainingIntervals() {
        var shorter = session(
                10, 200, 2000, 1, "09:15", "09:45"
        );

        var longer = session(
                10, 200, 2000, 1, "08:30", "10:30"
        );

        assertEquals(
                Set.of(ConflictType.ROOM),
                conflictTypes(shorter)
        );

        assertEquals(
                Set.of(ConflictType.ROOM),
                conflictTypes(longer)
        );
    }

    @Test
    void rejectsInvalidSessionTimes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> session(
                        10, 100, 1000, 1, "10:00", "09:00"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session(
                        10, 100, 1000, 1, "09:00", "09:00"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> session(
                        10, 100, 1000, 1, "09:00:30", "10:00"
                )
        );
    }

    private Set<ConflictType> conflictTypes(
        ScheduledSession candidate
) {
    return checker.findConflicts(candidate, List.of(existing))
            .stream()
            .map(conflict -> conflict.type())
            .collect(Collectors.toSet());
}

    private static ScheduledSession session(
            long roomId,
            long teacherId,
            long sectionId,
            int dayOfWeek,
            String startTime,
            String endTime
    ) {
        return new ScheduledSession(
                1,
                roomId,
                teacherId,
                sectionId,
                dayOfWeek,
                LocalTime.parse(startTime),
                LocalTime.parse(endTime)
        );
    }
}