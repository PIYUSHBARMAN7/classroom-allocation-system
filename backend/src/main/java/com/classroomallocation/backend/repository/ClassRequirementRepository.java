package com.classroomallocation.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.ClassRequirement;

public interface ClassRequirementRepository
        extends JpaRepository<ClassRequirement, Long> {

    boolean existsBySection_IdAndSubject_Id(
            Long sectionId,
            Long subjectId
    );

    boolean existsBySection_IdAndSubject_IdAndIdNot(
            Long sectionId,
            Long subjectId,
            Long id
    );

    @Override
    @EntityGraph(attributePaths = {
            "section",
            "subject",
            "teacher"
    })
    Optional<ClassRequirement> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {
            "section",
            "subject",
            "teacher"
    })
    Page<ClassRequirement> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "section",
            "subject",
            "teacher"
    })
    Page<ClassRequirement> findByEnabled(
            boolean enabled,
            Pageable pageable
    );
}