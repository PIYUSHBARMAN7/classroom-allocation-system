CREATE TABLE student_section_assignments (
    user_id BIGINT PRIMARY KEY,

    section_id BIGINT NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    assigned_by VARCHAR(254) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT student_section_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT student_section_section_fk
        FOREIGN KEY (section_id)
        REFERENCES sections(id)
        ON DELETE RESTRICT,

    CONSTRAINT student_section_version_valid
        CHECK (version >= 0),

    CONSTRAINT student_section_assigned_by_not_blank
        CHECK (LENGTH(TRIM(assigned_by)) > 0)
);

CREATE INDEX student_section_assignments_section_idx
    ON student_section_assignments (section_id);