package com.classroomallocation.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.Classroom;

public interface ClassroomRepository
        extends JpaRepository<Classroom, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Page<Classroom> findByEnabled(
            boolean enabled,
            Pageable pageable
    );
}