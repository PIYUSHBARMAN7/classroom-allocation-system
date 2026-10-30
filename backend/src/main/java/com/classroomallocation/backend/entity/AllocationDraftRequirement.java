package com.classroomallocation.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "allocation_draft_requirements")
@IdClass(AllocationDraftRequirementId.class)
public class AllocationDraftRequirement {

    @Id
    @Column(name = "draft_id", nullable = false)
    private Long draftId;

    @Id
    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "sessions_requested", nullable = false)
    private int sessionsRequested;

    @Column(name = "periods_per_session", nullable = false)
    private int periodsPerSession;

    protected AllocationDraftRequirement() {
    }

    public AllocationDraftRequirement(
            Long draftId,
            Long requirementId,
            int sessionsRequested,
            int periodsPerSession
    ) {
        this.draftId = draftId;
        this.requirementId = requirementId;
        this.sessionsRequested = sessionsRequested;
        this.periodsPerSession = periodsPerSession;
    }

    public Long getDraftId() {
        return draftId;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public int getSessionsRequested() {
        return sessionsRequested;
    }

    public int getPeriodsPerSession() {
        return periodsPerSession;
    }
}