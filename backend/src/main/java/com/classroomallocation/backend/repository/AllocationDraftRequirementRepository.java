package com.classroomallocation.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomallocation.backend.entity.AllocationDraftRequirement;
import com.classroomallocation.backend.entity.AllocationDraftRequirementId;

public interface AllocationDraftRequirementRepository
        extends JpaRepository<
                AllocationDraftRequirement,
                AllocationDraftRequirementId
        > {

    List<AllocationDraftRequirement>
            findByDraftIdOrderByRequirementIdAsc(Long draftId);
}