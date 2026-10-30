package com.classroomallocation.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class AllocationDraftRequirementId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long draftId;
    private Long requirementId;

    public AllocationDraftRequirementId() {
    }

    public AllocationDraftRequirementId(
            Long draftId,
            Long requirementId
    ) {
        this.draftId = draftId;
        this.requirementId = requirementId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof AllocationDraftRequirementId that)) {
            return false;
        }

        return Objects.equals(draftId, that.draftId)
                && Objects.equals(requirementId, that.requirementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(draftId, requirementId);
    }
}