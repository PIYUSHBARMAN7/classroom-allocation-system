package com.classroomallocation.backend.entity;

import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "employee_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String department;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "max_periods_per_day", nullable = false)
    private int maxPeriodsPerDay = 6;

    @Column(name = "max_periods_per_week", nullable = false)
    private int maxPeriodsPerWeek = 24;

    @Column(nullable = false)
    private boolean enabled = true;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Teacher() {
        // Required by JPA.
    }

    public Teacher(
            String employeeCode,
            String fullName,
            String department,
            int maxPeriodsPerDay,
            int maxPeriodsPerWeek
    ) {
        updateDetails(
                employeeCode,
                fullName,
                department,
                maxPeriodsPerDay,
                maxPeriodsPerWeek
        );
    }

    public void updateDetails(
            String employeeCode,
            String fullName,
            String department,
            int maxPeriodsPerDay,
            int maxPeriodsPerWeek
    ) {
        this.employeeCode = employeeCode
                .trim()
                .toUpperCase(Locale.ROOT);

        this.fullName = fullName.trim();
        this.department = department.trim();
        this.maxPeriodsPerDay = maxPeriodsPerDay;
        this.maxPeriodsPerWeek = maxPeriodsPerWeek;
    }

    public void linkUser(User user) {
        this.user = user;
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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public Long getUserId() {
        return user == null ? null : user.getId();
    }

    public int getMaxPeriodsPerDay() {
        return maxPeriodsPerDay;
    }

    public int getMaxPeriodsPerWeek() {
        return maxPeriodsPerWeek;
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