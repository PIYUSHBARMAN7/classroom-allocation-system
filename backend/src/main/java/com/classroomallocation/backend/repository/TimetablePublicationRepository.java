package com.classroomallocation.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.TimetablePublication;

public interface TimetablePublicationRepository
        extends JpaRepository<TimetablePublication, Long> {

    Optional<TimetablePublication> findByActiveTrue();

    Optional<TimetablePublication>
            findBySourceDraftIdAndSourceDraftVersion(
                    Long sourceDraftId,
                    long sourceDraftVersion
            );
}