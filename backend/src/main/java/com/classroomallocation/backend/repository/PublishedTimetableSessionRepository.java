package com.classroomallocation.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.PublishedTimetableSession;

public interface PublishedTimetableSessionRepository
        extends JpaRepository<PublishedTimetableSession, Long> {
    long countByPublicationId(Long publicationId);

    List<PublishedTimetableSession> findByPublicationIdOrderByDayOfWeekAscStartTimeAscIdAsc(
            Long publicationId);

    List<PublishedTimetableSession> findByPublicationIdAndTeacherIdOrderByDayOfWeekAscStartTimeAscIdAsc(
            Long publicationId,
            Long teacherId);

    List<PublishedTimetableSession> findByPublicationIdAndSectionIdOrderByDayOfWeekAscStartTimeAscIdAsc(
            Long publicationId,
            Long sectionId);
}