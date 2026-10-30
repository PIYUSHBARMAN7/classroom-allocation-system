package com.classroomallocation.backend.entity;

import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "sections",
        uniqueConstraints = @UniqueConstraint(
                name = "sections_identity_unique",
                columnNames = {
                        "program_code",
                        "academic_year_start",
                        "semester",
                        "code"
                }
        )
)
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "program_code", nullable = false, length = 30)
    private String programCode;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private int semester;

    @Column(name = "academic_year_start", nullable = false)
    private int academicYearStart;

    @Column(name = "student_count", nullable = false)
    private int studentCount;

    @Column(nullable = false)
    private boolean enabled = true;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Section() {
        // Required by JPA.
    }

    public Section(
            String code,
            String name,
            String programCode,
            String department,
            int semester,
            int academicYearStart,
            int studentCount
    ) {
        updateDetails(
                code,
                name,
                programCode,
                department,
                semester,
                academicYearStart,
                studentCount
        );
    }

    public void updateDetails(
            String code,
            String name,
            String programCode,
            String department,
            int semester,
            int academicYearStart,
            int studentCount
    ) {
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.programCode = programCode.trim().toUpperCase(Locale.ROOT);
        this.department = department.trim();
        this.semester = semester;
        this.academicYearStart = academicYearStart;
        this.studentCount = studentCount;
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

    public String getProgramCode() {
        return programCode;
    }

    public String getDepartment() {
        return department;
    }

    public int getSemester() {
        return semester;
    }

    public int getAcademicYearStart() {
        return academicYearStart;
    }

    public int getStudentCount() {
        return studentCount;
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