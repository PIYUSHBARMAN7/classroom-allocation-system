package com.classroomallocation.backend.allocation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DraftScheduleValidator {

    private final RoomMatcher roomMatcher = new RoomMatcher();
    private final ConflictChecker conflictChecker = new ConflictChecker();

    public record ValidationResult(List<String> errors) {

        public ValidationResult {
            errors = List.copyOf(errors);
        }

        public boolean valid() {
            return errors.isEmpty();
        }
    }

    private record TeacherDay(long teacherId, int dayOfWeek) {
    }

    public ValidationResult validate(
            List<AllocationEngine.Requirement> requirements,
            List<RoomCandidate> rooms,
            List<AllocationEngine.TeachingPeriod> teachingPeriods,
            List<ScheduledSession> savedSessions
    ) {
        requirements = List.copyOf(requirements);
        rooms = List.copyOf(rooms);
        teachingPeriods = List.copyOf(teachingPeriods);
        savedSessions = List.copyOf(savedSessions);

        List<String> errors = new ArrayList<>();

        if (requirements.isEmpty()) {
            errors.add("The draft contains no class requirements.");
        }

        Map<Long, AllocationEngine.Requirement> requirementsById =
                requirements.stream().collect(Collectors.toMap(
                        AllocationEngine.Requirement::id,
                        Function.identity()
                ));

        Map<Long, RoomCandidate> roomsById =
                rooms.stream().collect(Collectors.toMap(
                        RoomCandidate::id,
                        Function.identity()
                ));

        Map<Long, Long> sessionCounts =
                savedSessions.stream().collect(Collectors.groupingBy(
                        ScheduledSession::requirementId,
                        Collectors.counting()
                ));

        Map<TeacherDay, Integer> dailyLoads = new HashMap<>();
        Map<Long, Integer> weeklyLoads = new HashMap<>();

        for (var requirement : requirements) {
            long allocated = sessionCounts.getOrDefault(
                    requirement.id(),
                    0L
            );

            if (allocated != requirement.sessionsPerWeek()) {
                errors.add(
                        "Requirement #" + requirement.id()
                                + " needs " + requirement.sessionsPerWeek()
                                + " sessions, but has " + allocated + "."
                );
            }
        }

        List<ScheduledSession> checkedSessions = new ArrayList<>();

        for (ScheduledSession session : savedSessions) {
            var requirement = requirementsById.get(
                    session.requirementId()
            );

            if (requirement == null) {
                errors.add(
                        "A saved session refers to an unselected requirement #"
                                + session.requirementId() + "."
                );
            } else {
                validateSession(
                        session,
                        requirement,
                        roomsById,
                        teachingPeriods,
                        errors
                );

                dailyLoads.merge(
                        new TeacherDay(
                                session.teacherId(),
                                session.dayOfWeek()
                        ),
                        requirement.periodsPerSession(),
                        Integer::sum
                );

                weeklyLoads.merge(
                        session.teacherId(),
                        requirement.periodsPerSession(),
                        Integer::sum
                );
            }

            if (!conflictChecker.canPlace(session, checkedSessions)) {
                errors.add(
                        "Requirement #" + session.requirementId()
                                + " has a room, teacher or section clash on day "
                                + session.dayOfWeek()
                                + " at " + session.startTime() + "."
                );
            }

            checkedSessions.add(session);
        }

        for (var requirement : requirements) {
            int weeklyLoad = weeklyLoads.getOrDefault(
                    requirement.teacherId(),
                    0
            );

            if (weeklyLoad > requirement.maxPeriodsPerWeek()) {
                errors.add(
                        "Teacher #" + requirement.teacherId()
                                + " exceeds the weekly teaching limit."
                );
            }

            for (int day = 1; day <= 7; day++) {
                int dailyLoad = dailyLoads.getOrDefault(
                        new TeacherDay(requirement.teacherId(), day),
                        0
                );

                if (dailyLoad > requirement.maxPeriodsPerDay()) {
                    errors.add(
                            "Teacher #" + requirement.teacherId()
                                    + " exceeds the daily teaching limit on day "
                                    + day + "."
                    );
                }
            }
        }

        return new ValidationResult(
                errors.stream().distinct().toList()
        );
    }

    private void validateSession(
            ScheduledSession session,
            AllocationEngine.Requirement requirement,
            Map<Long, RoomCandidate> roomsById,
            List<AllocationEngine.TeachingPeriod> teachingPeriods,
            List<String> errors
    ) {
        if (session.teacherId() != requirement.teacherId()) {
            errors.add(
                    "Requirement #" + requirement.id()
                            + " has a different teacher from its current assignment."
            );
        }

        if (session.sectionId() != requirement.sectionId()) {
            errors.add(
                    "Requirement #" + requirement.id()
                            + " has a different section from its current assignment."
            );
        }

        RoomCandidate room = roomsById.get(session.roomId());

        if (room == null || roomMatcher.findBestMatch(
                List.of(room),
                requirement.roomNeed()
        ).isEmpty()) {
            errors.add(
                    "Room #" + session.roomId()
                            + " no longer meets requirement #"
                            + requirement.id() + "."
            );
        }

        if (!coversTeachingPeriods(
                session,
                requirement.periodsPerSession(),
                teachingPeriods
        )) {
            errors.add(
                    "Requirement #" + requirement.id()
                            + " does not fit the required consecutive teaching periods."
            );
        }
    }

    private boolean coversTeachingPeriods(
            ScheduledSession session,
            int requiredPeriods,
            List<AllocationEngine.TeachingPeriod> teachingPeriods
    ) {
        LocalTime nextStart = session.startTime();

        for (int count = 0; count < requiredPeriods; count++) {
            AllocationEngine.TeachingPeriod matchingPeriod = null;

            for (var period : teachingPeriods) {
                if (period.dayOfWeek() == session.dayOfWeek()
                        && period.startTime().equals(nextStart)) {
                    matchingPeriod = period;
                    break;
                }
            }

            if (matchingPeriod == null) {
                return false;
            }

            nextStart = matchingPeriod.endTime();
        }

        return nextStart.equals(session.endTime());
    }
}