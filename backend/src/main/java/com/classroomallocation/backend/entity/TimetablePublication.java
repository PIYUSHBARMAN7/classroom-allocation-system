package com.classroomallocation.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "timetable_publications")
public class TimetablePublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_draft_id", nullable = false)
    private Long sourceDraftId;

    @Column(name = "source_draft_version", nullable = false)
    private long sourceDraftVersion;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "published_by", nullable = false, length = 254)
    private String publishedBy;

    @Column(name = "published_at", nullable = false, updatable = false)
    private Instant publishedAt;

    protected TimetablePublication() {
    }

    public TimetablePublication(
            Long sourceDraftId,
            long sourceDraftVersion,
            String name,
            String publishedBy
    ) {
        this.sourceDraftId = sourceDraftId;
        this.sourceDraftVersion = sourceDraftVersion;
        this.name = name;
        this.publishedBy = publishedBy;
        this.active = false;
    }

    @PrePersist
    void onCreate() {
        publishedAt = Instant.now();
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public Long getId() {
        return id;
    }

    public Long getSourceDraftId() {
        return sourceDraftId;
    }

    public long getSourceDraftVersion() {
        return sourceDraftVersion;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}