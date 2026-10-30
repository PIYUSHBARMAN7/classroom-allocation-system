package com.classroomallocation.backend.allocation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AllocationEngine {

    private final RoomMatcher roomMatcher = new RoomMatcher();
    private final ConflictChecker conflictChecker = new ConflictChecker();

    public record TeachingPeriod(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        public TeachingPeriod {
            Objects.requireNonNull(startTime, "Start time is required.");
            Objects.requireNonNull(endTime, "End time is required.");

            if (dayOfWeek < 1 || dayOfWeek > 7) {
                throw new IllegalArgumentException(
                        "Day must be between 1 and 7."
                );
            }

            if (!startTime.isBefore(endTime)) {
                throw new IllegalArgumentException(
                        "Start time must be before end time."
                );
            }

            if (startTime.getSecond() != 0
                    || startTime.getNano() != 0
                    || endTime.getSecond() != 0
                    || endTime.getNano() != 0) {
                throw new IllegalArgumentException(
                        "Teaching periods must use whole minutes."
                );
            }
        }
    }

    public record Requirement(
            long id,
            long teacherId,
            long sectionId,
            RoomNeed roomNeed,
            int sessionsPerWeek,
            int periodsPerSession,
            int maxPeriodsPerDay,
            int maxPeriodsPerWeek
    ) {
        public Requirement {
            Objects.requireNonNull(roomNeed, "Room requirements are required.");

            if (id <= 0 || teacherId <= 0 || sectionId <= 0) {
                throw new IllegalArgumentException(
                        "Requirement, teacher and section IDs must be positive."
                );
            }

            if (sessionsPerWeek < 1 || sessionsPerWeek > 20) {
                throw new IllegalArgumentException(
                        "Sessions per week must be between 1 and 20."
                );
            }

            if (periodsPerSession < 1 || periodsPerSession > 4) {
                throw new IllegalArgumentException(
                        "Periods per session must be between 1 and 4."
                );
            }

            if (maxPeriodsPerDay < 1 || maxPeriodsPerDay > 12
                    || maxPeriodsPerWeek < 1 || maxPeriodsPerWeek > 60) {
                throw new IllegalArgumentException(
                        "Teacher workload limits are invalid."
                );
            }
        }
    }

    public record UnallocatedSession(
            long requirementId,
            int sessionNumber,
            String reason
    ) {
    }

    public record Result(
            List<ScheduledSession> allocations,
            List<UnallocatedSession> unallocated
    ) {
        public Result {
            allocations = List.copyOf(allocations);
            unallocated = List.copyOf(unallocated);
        }

        public boolean complete() {
            return unallocated.isEmpty();
        }
    }

    private record TeacherDay(long teacherId, int dayOfWeek) {
    }

    private record TeacherLimits(int daily, int weekly) {
    }

    public Result allocate(
            List<Requirement> requirements,
            List<RoomCandidate> rooms,
            List<TeachingPeriod> teachingPeriods
    ) {
        List<Requirement> tasks = List.copyOf(requirements);
        List<RoomCandidate> availableRooms = List.copyOf(rooms);

        List<TeachingPeriod> periods = teachingPeriods.stream()
                .sorted(Comparator
                        .comparingInt(TeachingPeriod::dayOfWeek)
                        .thenComparing(TeachingPeriod::startTime)
                        .thenComparing(TeachingPeriod::endTime))
                .toList();

        validateInputs(tasks, availableRooms, periods);

        Map<Long, List<RoomCandidate>> matches = new HashMap<>();

        for (Requirement task : tasks) {
            matches.put(
                    task.id(),
                    roomMatcher.findMatches(availableRooms, task.roomNeed())
            );
        }

        // Place requirements with fewer room options first.
        // Longer sessions get priority when room options are equal.
        List<Requirement> orderedTasks = tasks.stream()
                .sorted(Comparator
                        .comparingInt((Requirement task) ->
                                matches.get(task.id()).size())
                        .thenComparing(
                                Comparator.comparingInt(
                                        Requirement::periodsPerSession
                                ).reversed()
                        )
                        .thenComparingLong(Requirement::id))
                .toList();

        List<ScheduledSession> allocations = new ArrayList<>();
        List<UnallocatedSession> unallocated = new ArrayList<>();

        Map<TeacherDay, Integer> dailyLoads = new HashMap<>();
        Map<Long, Integer> weeklyLoads = new HashMap<>();

        for (Requirement task : orderedTasks) {
            List<RoomCandidate> matchingRooms = matches.get(task.id());

            for (int sessionNumber = 1;
                    sessionNumber <= task.sessionsPerWeek();
                    sessionNumber++) {

                if (matchingRooms.isEmpty()) {
                    unallocated.add(new UnallocatedSession(
                            task.id(),
                            sessionNumber,
                            "No enabled room meets the capacity, type and facility requirements."
                    ));
                    continue;
                }

                ScheduledSession placement = findPlacement(
                        task,
                        matchingRooms,
                        periods,
                        allocations,
                        dailyLoads,
                        weeklyLoads
                );

                if (placement == null) {
                    unallocated.add(new UnallocatedSession(
                            task.id(),
                            sessionNumber,
                            "No placement was found within the teaching periods, conflict checks and teacher workload limits."
                    ));
                    continue;
                }

                allocations.add(placement);

                TeacherDay teacherDay = new TeacherDay(
                        task.teacherId(),
                        placement.dayOfWeek()
                );

                dailyLoads.merge(
                        teacherDay,
                        task.periodsPerSession(),
                        Integer::sum
                );

                weeklyLoads.merge(
                        task.teacherId(),
                        task.periodsPerSession(),
                        Integer::sum
                );
            }
        }

        return new Result(allocations, unallocated);
    }

    private ScheduledSession findPlacement(
            Requirement task,
            List<RoomCandidate> rooms,
            List<TeachingPeriod> periods,
            List<ScheduledSession> allocations,
            Map<TeacherDay, Integer> dailyLoads,
            Map<Long, Integer> weeklyLoads
    ) {
        int weeklyLoad = weeklyLoads.getOrDefault(task.teacherId(), 0);

        if (weeklyLoad + task.periodsPerSession()
                > task.maxPeriodsPerWeek()) {
            return null;
        }

        for (int startIndex = 0;
                startIndex < periods.size();
                startIndex++) {

            TeachingPeriod first = periods.get(startIndex);

            TeacherDay teacherDay = new TeacherDay(
                    task.teacherId(),
                    first.dayOfWeek()
            );

            int dailyLoad = dailyLoads.getOrDefault(teacherDay, 0);

            if (dailyLoad + task.periodsPerSession()
                    > task.maxPeriodsPerDay()) {
                continue;
            }

            LocalTime endTime = consecutiveEnd(
                    periods,
                    startIndex,
                    task.periodsPerSession()
            );

            if (endTime == null) {
                continue;
            }

            for (RoomCandidate room : rooms) {
                ScheduledSession candidate = new ScheduledSession(
                        task.id(),
                        room.id(),
                        task.teacherId(),
                        task.sectionId(),
                        first.dayOfWeek(),
                        first.startTime(),
                        endTime
                );

                if (conflictChecker.canPlace(candidate, allocations)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private LocalTime consecutiveEnd(
            List<TeachingPeriod> periods,
            int startIndex,
            int requiredPeriods
    ) {
        if (startIndex + requiredPeriods > periods.size()) {
            return null;
        }

        TeachingPeriod first = periods.get(startIndex);
        LocalTime endTime = first.endTime();

        for (int offset = 1; offset < requiredPeriods; offset++) {
            TeachingPeriod next = periods.get(startIndex + offset);

            // A session cannot cross a day boundary or a gap/break.
            if (next.dayOfWeek() != first.dayOfWeek()
                    || !next.startTime().equals(endTime)) {
                return null;
            }

            endTime = next.endTime();
        }

        return endTime;
    }

    private void validateInputs(
            List<Requirement> requirements,
            List<RoomCandidate> rooms,
            List<TeachingPeriod> periods
    ) {
        Set<Long> requirementIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        Map<Long, TeacherLimits> teacherLimits = new HashMap<>();

        for (Requirement requirement : requirements) {
            if (!requirementIds.add(requirement.id())) {
                throw new IllegalArgumentException(
                        "Duplicate requirement ID: " + requirement.id()
                );
            }

            TeacherLimits limits = new TeacherLimits(
                    requirement.maxPeriodsPerDay(),
                    requirement.maxPeriodsPerWeek()
            );

            TeacherLimits previous = teacherLimits.putIfAbsent(
                    requirement.teacherId(),
                    limits
            );

            if (previous != null && !previous.equals(limits)) {
                throw new IllegalArgumentException(
                        "The same teacher must have consistent workload limits."
                );
            }
        }

        for (RoomCandidate room : rooms) {
            if (!roomIds.add(room.id())) {
                throw new IllegalArgumentException(
                        "Duplicate room ID: " + room.id()
                );
            }
        }

        for (int index = 1; index < periods.size(); index++) {
            TeachingPeriod previous = periods.get(index - 1);
            TeachingPeriod current = periods.get(index);

            if (previous.dayOfWeek() == current.dayOfWeek()
                    && current.startTime().isBefore(previous.endTime())) {
                throw new IllegalArgumentException(
                        "Teaching periods must not overlap."
                );
            }
        }
    }
}