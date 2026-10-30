package com.classroomallocation.backend.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "published_timetable_sessions")
public class PublishedTimetableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publication_id", nullable = false)
    private Long publicationId;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "session_number", nullable = false)
    private int sessionNumber;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "room_code", nullable = false, length = 30)
    private String roomCode;

    @Column(name = "teacher_name", nullable = false, length = 100)
    private String teacherName;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected PublishedTimetableSession() {
    }

    public PublishedTimetableSession(
            Long publicationId,
            AllocationDraftSession source,
            String subjectName,
            String roomCode,
            String teacherName,
            String sectionCode
    ) {
        this.publicationId = publicationId;
        this.requirementId = source.getRequirementId();
        this.sessionNumber = source.getSessionNumber();
        this.roomId = source.getRoomId();
        this.teacherId = source.getTeacherId();
        this.sectionId = source.getSectionId();
        this.dayOfWeek = source.getDayOfWeek();
        this.startTime = source.getStartTime();
        this.endTime = source.getEndTime();

        this.subjectName = subjectName;
        this.roomCode = roomCode;
        this.teacherName = teacherName;
        this.sectionCode = sectionCode;
    }

    public Long getId() {
        return id;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}