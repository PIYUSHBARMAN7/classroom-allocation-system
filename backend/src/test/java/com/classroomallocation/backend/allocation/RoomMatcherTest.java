package com.classroomallocation.backend.allocation;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.classroomallocation.backend.enums.RoomType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomMatcherTest {

    private final RoomMatcher matcher = new RoomMatcher();

    @Test
    void rejectsRoomsWithInsufficientCapacity() {
        var room = new RoomCandidate(
                1, "ROOM-101", 30,
                RoomType.LECTURE_HALL,
                true, true, true
        );

        var need = new RoomNeed(
                60,
                RoomType.LECTURE_HALL,
                false,
                false
        );

        assertTrue(
                matcher.findMatches(List.of(room), need).isEmpty()
        );
    }

    @Test
    void rejectsWrongRoomType() {
        var room = new RoomCandidate(
                1, "ROOM-101", 100,
                RoomType.LECTURE_HALL,
                true, true, true
        );

        var need = new RoomNeed(
                40,
                RoomType.COMPUTER_LAB,
                false,
                false
        );

        assertTrue(
                matcher.findMatches(List.of(room), need).isEmpty()
        );
    }

    @Test
    void requiresBothRequestedFacilities() {
        var noProjector = new RoomCandidate(
                1, "LAB-01", 60,
                RoomType.COMPUTER_LAB,
                false, true, true
        );

        var notAccessible = new RoomCandidate(
                2, "LAB-02", 60,
                RoomType.COMPUTER_LAB,
                true, false, true
        );

        var suitable = new RoomCandidate(
                3, "LAB-03", 60,
                RoomType.COMPUTER_LAB,
                true, true, true
        );

        var need = new RoomNeed(
                50,
                RoomType.COMPUTER_LAB,
                true,
                true
        );

        var result = matcher.findMatches(
                List.of(noProjector, notAccessible, suitable),
                need
        );

        assertEquals(List.of(suitable), result);
    }

    @Test
    void rejectsInactiveRooms() {
        var room = new RoomCandidate(
                1, "ROOM-101", 100,
                RoomType.LECTURE_HALL,
                true, true, false
        );

        var need = new RoomNeed(
                40,
                RoomType.LECTURE_HALL,
                false,
                false
        );

        assertTrue(
                matcher.findBestMatch(List.of(room), need).isEmpty()
        );
    }

    @Test
    void choosesSmallestSuitableRoomWithConsistentTieBreaking() {
        var large = new RoomCandidate(
                1, "ROOM-C", 120,
                RoomType.LECTURE_HALL,
                false, false, true
        );

        var smallerB = new RoomCandidate(
                2, "ROOM-B", 60,
                RoomType.LECTURE_HALL,
                false, false, true
        );

        var smallerA = new RoomCandidate(
                3, "ROOM-A", 60,
                RoomType.LECTURE_HALL,
                false, false, true
        );

        var need = new RoomNeed(
                50,
                RoomType.LECTURE_HALL,
                false,
                false
        );

        var result = matcher.findMatches(
                List.of(large, smallerB, smallerA),
                need
        );

        assertEquals(
                List.of(smallerA, smallerB, large),
                result
        );

        assertEquals(
                smallerA,
                matcher.findBestMatch(
                        List.of(large, smallerB, smallerA),
                        need
                ).orElseThrow()
        );
    }
}