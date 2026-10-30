package com.classroomallocation.backend.allocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConflictChecker {

    public List<AllocationConflict> findConflicts(
            ScheduledSession candidate,
            List<ScheduledSession> existingSessions
    ) {
        Objects.requireNonNull(
                candidate,
                "Candidate session is required."
        );

        List<AllocationConflict> conflicts = new ArrayList<>();

        for (ScheduledSession existing :
                List.copyOf(existingSessions)) {

            if (!overlaps(candidate, existing)) {
                continue;
            }

            if (candidate.roomId() == existing.roomId()) {
                conflicts.add(new AllocationConflict(
                        ConflictType.ROOM,
                        existing
                ));
            }

            if (candidate.teacherId() == existing.teacherId()) {
                conflicts.add(new AllocationConflict(
                        ConflictType.TEACHER,
                        existing
                ));
            }

            if (candidate.sectionId() == existing.sectionId()) {
                conflicts.add(new AllocationConflict(
                        ConflictType.SECTION,
                        existing
                ));
            }
        }

        return List.copyOf(conflicts);
    }

    public boolean canPlace(
            ScheduledSession candidate,
            List<ScheduledSession> existingSessions
    ) {
        return findConflicts(candidate, existingSessions).isEmpty();
    }

    private boolean overlaps(
            ScheduledSession first,
            ScheduledSession second
    ) {
        return first.dayOfWeek() == second.dayOfWeek()
                && first.startTime().isBefore(second.endTime())
                && first.endTime().isAfter(second.startTime());
    }
}