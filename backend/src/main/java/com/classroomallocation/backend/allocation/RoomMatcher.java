package com.classroomallocation.backend.allocation;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RoomMatcher {

    public List<RoomCandidate> findMatches(
            List<RoomCandidate> rooms,
            RoomNeed need
    ) {
        Objects.requireNonNull(need, "Room requirements are missing.");

        return List.copyOf(rooms)
                .stream()
                .filter(RoomCandidate::enabled)
                .filter(room ->
                        room.capacity() >= need.studentCount()
                )
                .filter(room ->
                        room.roomType() == need.requiredRoomType()
                )
                .filter(room ->
                        !need.requiresProjector()
                        || room.hasProjector()
                )
                .filter(room ->
                        !need.requiresAccessibleRoom()
                        || room.accessible()
                )
                .sorted(
                        Comparator.comparingInt(
                                RoomCandidate::capacity
                        )
                        .thenComparing(RoomCandidate::code)
                        .thenComparingLong(RoomCandidate::id)
                )
                .toList();
    }

    public Optional<RoomCandidate> findBestMatch(
            List<RoomCandidate> rooms,
            RoomNeed need
    ) {
        return findMatches(rooms, need)
                .stream()
                .findFirst();
    }
}