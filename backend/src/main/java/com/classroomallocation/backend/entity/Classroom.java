package com.classroomallocation.backend.entity;

import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.classroomallocation.backend.enums.RoomType;

@Entity
@Table(name = "classrooms")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String building;

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @Column(name = "has_projector", nullable = false)
    private boolean hasProjector;

    @Column(nullable = false)
    private boolean accessible;

    @Column(nullable = false)
    private boolean enabled = true;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Classroom() {
        // Required by JPA.
    }

    public Classroom(
            String code,
            String name,
            String building,
            int floorNumber,
            int capacity,
            RoomType roomType,
            boolean hasProjector,
            boolean accessible
    ) {
        updateDetails(
                code,
                name,
                building,
                floorNumber,
                capacity,
                roomType,
                hasProjector,
                accessible
        );
    }

    public void updateDetails(
            String code,
            String name,
            String building,
            int floorNumber,
            int capacity,
            RoomType roomType,
            boolean hasProjector,
            boolean accessible
    ) {
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.building = building.trim();
        this.floorNumber = floorNumber;
        this.capacity = capacity;
        this.roomType = roomType;
        this.hasProjector = hasProjector;
        this.accessible = accessible;
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getBuilding() {
        return building;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public boolean isHasProjector() {
        return hasProjector;
    }

    public boolean isAccessible() {
        return accessible;
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