package com.classroomallocation.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.StudentSectionAssignment;

public interface StudentSectionAssignmentRepository
        extends JpaRepository<StudentSectionAssignment, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "section"})
    Optional<StudentSectionAssignment> findById(Long userId);
}