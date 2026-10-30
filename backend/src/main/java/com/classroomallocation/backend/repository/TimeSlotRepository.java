package com.classroomallocation.backend.repository;

import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.classroomallocation.backend.entity.TimeSlot;

public interface TimeSlotRepository
        extends JpaRepository<TimeSlot, Long> {

    boolean existsByDayOfWeekAndStartTimeAndEndTime(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    );

    boolean existsByDayOfWeekAndStartTimeAndEndTimeAndIdNot(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long id
    );

    Page<TimeSlot> findByEnabled(
            boolean enabled,
            Pageable pageable
    );

    @Query("""
            select count(t)
            from TimeSlot t
            where t.enabled = true
              and t.dayOfWeek = :dayOfWeek
              and t.startTime < :endTime
              and t.endTime > :startTime
              and (:excludedId is null or t.id <> :excludedId)
            """)
    long countActiveOverlaps(
            @Param("dayOfWeek") int dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedId") Long excludedId
    );
}