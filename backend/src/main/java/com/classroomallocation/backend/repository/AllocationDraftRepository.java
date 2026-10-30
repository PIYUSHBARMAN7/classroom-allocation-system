package com.classroomallocation.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.AllocationDraft;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AllocationDraftRepository
                extends JpaRepository<AllocationDraft, Long> {
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select d from AllocationDraft d where d.id = :id")
        Optional<AllocationDraft> findByIdForPublication(@Param("id") Long id);
}