package com.classroomallocation.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.Teacher;

public interface TeacherRepository
        extends JpaRepository<Teacher, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndIdNot(
            String employeeCode,
            Long id
    );

    Optional<Teacher> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    boolean existsByUser_IdAndIdNot(
            Long userId,
            Long id
    );

    Page<Teacher> findByEnabled(
            boolean enabled,
            Pageable pageable
    );
}