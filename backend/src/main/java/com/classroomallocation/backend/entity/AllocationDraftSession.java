package com.classroomallocation.backend.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "allocation_draft_sessions")
public class AllocationDraftSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "draft_id", nullable = false)
    private Long draftId;

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

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected AllocationDraftSession() {
    }

    public AllocationDraftSession(
            Long draftId,
            Long requirementId,
            int sessionNumber,
            Long roomId,
            Long teacherId,
            Long sectionId,
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        this.draftId = draftId;
        this.requirementId = requirementId;
        this.sessionNumber = sessionNumber;
        this.roomId = roomId;
        this.teacherId = teacherId;
        this.sectionId = sectionId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public Long getDraftId() {
        return draftId;
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