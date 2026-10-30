package com.classroomallocation.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "class_requirements",
        uniqueConstraints = @UniqueConstraint(
                name = "requirements_section_subject_unique",
                columnNames = {"section_id", "subject_id"}
        )
)
public class ClassRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "sessions_per_week", nullable = false)
    private int sessionsPerWeek;

    @Column(name = "periods_per_session", nullable = false)
    private int periodsPerSession = 1;

    @Column(name = "requires_projector", nullable = false)
    private boolean requiresProjector;

    @Column(name = "requires_accessible_room", nullable = false)
    private boolean requiresAccessibleRoom;

    @Column(nullable = false)
    private boolean enabled = true;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ClassRequirement() {
        // Required by JPA.
    }

    public ClassRequirement(
            Section section,
            Subject subject,
            Teacher teacher,
            int sessionsPerWeek,
            int periodsPerSession,
            boolean requiresProjector,
            boolean requiresAccessibleRoom
    ) {
        updateDetails(
                section,
                subject,
                teacher,
                sessionsPerWeek,
                periodsPerSession,
                requiresProjector,
                requiresAccessibleRoom
        );
    }

    public void updateDetails(
            Section section,
            Subject subject,
            Teacher teacher,
            int sessionsPerWeek,
            int periodsPerSession,
            boolean requiresProjector,
            boolean requiresAccessibleRoom
    ) {
        this.section = section;
        this.subject = subject;
        this.teacher = teacher;
        this.sessionsPerWeek = sessionsPerWeek;
        this.periodsPerSession = periodsPerSession;
        this.requiresProjector = requiresProjector;
        this.requiresAccessibleRoom = requiresAccessibleRoom;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public Subject getSubject() {
        return subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public int getSessionsPerWeek() {
        return sessionsPerWeek;
    }

    public int getPeriodsPerSession() {
        return periodsPerSession;
    }

    public int getWeeklyPeriods() {
        return sessionsPerWeek * periodsPerSession;
    }

    public boolean isRequiresProjector() {
        return requiresProjector;
    }

    public boolean isRequiresAccessibleRoom() {
        return requiresAccessibleRoom;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}