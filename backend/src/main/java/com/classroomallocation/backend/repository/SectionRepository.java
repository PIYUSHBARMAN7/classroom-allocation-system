package com.classroomallocation.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.Section;

public interface SectionRepository
        extends JpaRepository<Section, Long> {

    boolean existsByProgramCodeAndAcademicYearStartAndSemesterAndCode(
            String programCode,
            int academicYearStart,
            int semester,
            String code
    );

    boolean existsByProgramCodeAndAcademicYearStartAndSemesterAndCodeAndIdNot(
            String programCode,
            int academicYearStart,
            int semester,
            String code,
            Long id
    );

    Page<Section> findByEnabled(
            boolean enabled,
            Pageable pageable
    );
}