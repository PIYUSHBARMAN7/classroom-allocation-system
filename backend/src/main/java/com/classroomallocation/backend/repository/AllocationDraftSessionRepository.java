package com.classroomallocation.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.AllocationDraftSession;

public interface AllocationDraftSessionRepository
        extends JpaRepository<AllocationDraftSession, Long> {

    List<AllocationDraftSession>
            findByDraftIdOrderByDayOfWeekAscStartTimeAscIdAsc(Long draftId);
}