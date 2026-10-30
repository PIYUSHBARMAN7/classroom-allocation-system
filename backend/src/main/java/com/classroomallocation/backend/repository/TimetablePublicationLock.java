package com.classroomallocation.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TimetablePublicationLock {

    private final JdbcTemplate jdbcTemplate;

    public TimetablePublicationLock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void acquire() {
        Integer id = jdbcTemplate.queryForObject(
                """
                SELECT id
                FROM timetable_publication_lock
                WHERE id = 1
                FOR UPDATE
                """,
                Integer.class
        );

        if (id == null || id != 1) {
            throw new IllegalStateException(
                    "The timetable publication lock is unavailable."
            );
        }
    }
}