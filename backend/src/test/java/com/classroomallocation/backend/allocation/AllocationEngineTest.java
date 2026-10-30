package com.classroomallocation.backend.allocation;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.classroomallocation.backend.enums.RoomType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllocationEngineTest {

    private final AllocationEngine engine = new AllocationEngine();

    @Test
    void schedulesAllRequiredWeeklySessions() {
        var requirement = requirement(1, 10, 100, 2, 1, 6, 24);

        var result = engine.allocate(
                List.of(requirement),
                List.of(room(1, 40)),
                List.of(
                        period(1, "09:00", "10:00"),
                        period(1, "10:00", "11:00")
                )
        );

        assertTrue(result.complete());
        assertEquals(2, result.allocations().size());
        assertTrue(result.unallocated().isEmpty());

        assertEquals(
                LocalTime.of(9, 0),
                result.allocations().get(0).startTime()
        );

        assertEquals(
                LocalTime.of(10, 0),
                result.allocations().get(1).startTime()
        );
    }

    @Test
    void combinesConsecutivePeriodsIntoOneSession() {
        var requirement = requirement(1, 10, 100, 1, 2, 6, 24);

        var result = engine.allocate(
                List.of(requirement),
                List.of(room(1, 40)),
                List.of(
                        period(1, "09:00", "10:00"),
                        period(1, "10:00", "11:00")
                )
        );

        assertTrue(result.complete());
        assertEquals(1, result.allocations().size());

        ScheduledSession session = result.allocations().get(0);

        assertEquals(LocalTime.of(9, 0), session.startTime());
        assertEquals(LocalTime.of(11, 0), session.endTime());
    }

    @Test
    void doesNotJoinPeriodsAcrossABreak() {
        var requirement = requirement(1, 10, 100, 1, 2, 6, 24);

        var result = engine.allocate(
                List.of(requirement),
                List.of(room(1, 40)),
                List.of(
                        period(1, "09:00", "10:00"),
                        period(1, "10:30", "11:30")
                )
        );

        assertFalse(result.complete());
        assertTrue(result.allocations().isEmpty());
        assertEquals(1, result.unallocated().size());
        assertEquals(1L, result.unallocated().get(0).requirementId());
    }

    @Test
    void preventsRoomTeacherAndSectionClashes() {
        var result = engine.allocate(
                List.of(
                        requirement(1, 10, 100, 1, 1, 6, 24),
                        requirement(2, 10, 200, 1, 1, 6, 24),
                        requirement(3, 20, 100, 1, 1, 6, 24)
                ),
                List.of(room(1, 40)),
                List.of(
                        period(1, "09:00", "10:00"),
                        period(1, "10:00", "11:00"),
                        period(1, "11:00", "12:00")
                )
        );

        assertTrue(result.complete());
        assertEquals(3, result.allocations().size());

        ConflictChecker checker = new ConflictChecker();

        for (int index = 0; index < result.allocations().size(); index++) {
            ScheduledSession current = result.allocations().get(index);

            List<ScheduledSession> earlier =
                    result.allocations().subList(0, index);

            assertTrue(
                    checker.canPlace(current, earlier),
                    "Generated sessions must not clash."
            );
        }
    }

    @Test
    void enforcesDailyWorkloadAcrossDifferentRequirements() {
        var result = engine.allocate(
                List.of(
                        requirement(1, 10, 100, 1, 1, 1, 24),
                        requirement(2, 10, 200, 1, 1, 1, 24)
                ),
                List.of(room(1, 40)),
                List.of(
                        period(1, "09:00", "10:00"),
                        period(1, "10:00", "11:00")
                )
        );

        assertFalse(result.complete());
        assertEquals(1, result.allocations().size());
        assertEquals(1, result.unallocated().size());
        assertEquals(2L, result.unallocated().get(0).requirementId());
    }

    @Test
    void enforcesWeeklyWorkloadAcrossDifferentDays() {
        var requirement = requirement(1, 10, 100, 2, 1, 6, 1);

        var result = engine.allocate(
                List.of(requirement),
                List.of(room(1, 40)),
                List.of(
                        period(1, "09:00", "10:00"),
                        period(2, "09:00", "10:00")
                )
        );

        assertFalse(result.complete());
        assertEquals(1, result.allocations().size());
        assertEquals(1, result.unallocated().size());
        assertEquals(2, result.unallocated().get(0).sessionNumber());
    }

    @Test
    void reportsWhenNoRoomHasEnoughCapacity() {
        var requirement = requirement(1, 10, 100, 1, 1, 6, 24);

        var result = engine.allocate(
                List.of(requirement),
                List.of(room(1, 20)),
                List.of(period(1, "09:00", "10:00"))
        );

        assertFalse(result.complete());
        assertTrue(result.allocations().isEmpty());
        assertEquals(1, result.unallocated().size());

        assertEquals(
                "No enabled room meets the capacity, type and facility requirements.",
                result.unallocated().get(0).reason()
        );
    }

    @Test
    void rejectsDuplicateRequirementIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.allocate(
                        List.of(
                                requirement(1, 10, 100, 1, 1, 6, 24),
                                requirement(1, 20, 200, 1, 1, 6, 24)
                        ),
                        List.of(room(1, 40)),
                        List.of(period(1, "09:00", "10:00"))
                )
        );
    }

    private static AllocationEngine.Requirement requirement(
            long id,
            long teacherId,
            long sectionId,
            int sessionsPerWeek,
            int periodsPerSession,
            int maxPeriodsPerDay,
            int maxPeriodsPerWeek
    ) {
        return new AllocationEngine.Requirement(
                id,
                teacherId,
                sectionId,
                new RoomNeed(
                        30,
                        RoomType.LECTURE_HALL,
                        false,
                        false
                ),
                sessionsPerWeek,
                periodsPerSession,
                maxPeriodsPerDay,
                maxPeriodsPerWeek
        );
    }

    private static RoomCandidate room(long id, int capacity) {
        return new RoomCandidate(
                id,
                "ROOM-" + id,
                capacity,
                RoomType.LECTURE_HALL,
                false,
                false,
                true
        );
    }

    private static AllocationEngine.TeachingPeriod period(
            int day,
            String start,
            String end
    ) {
        return new AllocationEngine.TeachingPeriod(
                day,
                LocalTime.parse(start),
                LocalTime.parse(end)
        );
    }
}