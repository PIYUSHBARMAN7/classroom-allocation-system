package com.classroomallocation.backend.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.request.PublishTimetableRequest;
import com.classroomallocation.backend.dto.response.TimetablePublicationResponse;
import com.classroomallocation.backend.entity.PublishedTimetableSession;
import com.classroomallocation.backend.entity.TimetablePublication;
import com.classroomallocation.backend.exception.DraftPublicationValidationException;
import com.classroomallocation.backend.repository.AllocationDraftRepository;
import com.classroomallocation.backend.repository.AllocationDraftSessionRepository;
import com.classroomallocation.backend.repository.ClassRequirementRepository;
import com.classroomallocation.backend.repository.ClassroomRepository;
import com.classroomallocation.backend.repository.PublishedTimetableSessionRepository;
import com.classroomallocation.backend.repository.TimetablePublicationLock;
import com.classroomallocation.backend.repository.TimetablePublicationRepository;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TimetablePublicationService {

    private final TimetablePublicationLock publicationLock;
    private final TimetablePublicationRepository publicationRepository;
    private final PublishedTimetableSessionRepository publishedSessionRepository;
    private final AllocationDraftRepository draftRepository;
    private final AllocationDraftSessionRepository draftSessionRepository;
    private final ClassRequirementRepository requirementRepository;
    private final ClassroomRepository classroomRepository;
    private final AllocationDraftValidationService validationService;

    public TimetablePublicationService(
            TimetablePublicationLock publicationLock,
            TimetablePublicationRepository publicationRepository,
            PublishedTimetableSessionRepository publishedSessionRepository,
            AllocationDraftRepository draftRepository,
            AllocationDraftSessionRepository draftSessionRepository,
            ClassRequirementRepository requirementRepository,
            ClassroomRepository classroomRepository,
            AllocationDraftValidationService validationService
    ) {
        this.publicationLock = publicationLock;
        this.publicationRepository = publicationRepository;
        this.publishedSessionRepository = publishedSessionRepository;
        this.draftRepository = draftRepository;
        this.draftSessionRepository = draftSessionRepository;
        this.requirementRepository = requirementRepository;
        this.classroomRepository = classroomRepository;
        this.validationService = validationService;
    }

    @Transactional(readOnly = true)
    public TimetablePublicationResponse getActive() {
        return publicationRepository.findByActiveTrue()
                .map(publication -> toResponse(
                        publication,
                        publishedSessionRepository.countByPublicationId(
                                publication.getId()
                        )
                ))
                .orElse(null);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TimetablePublicationResponse publish(
            PublishTimetableRequest request,
            String authenticatedEmail
    ) {
        publicationLock.acquire();

        var draft = draftRepository.findByIdForPublication(
                request.draftId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Allocation draft not found."
        ));

        if (draft.getVersion() != request.draftVersion()) {
            throw conflict(
                    "The draft changed. Refresh it before publishing."
            );
        }

        var existingPublication = publicationRepository
                .findBySourceDraftIdAndSourceDraftVersion(
                        draft.getId(),
                        draft.getVersion()
                )
                .orElse(null);

        /*
         * Repeating a successful request for the currently active
         * draft returns the existing publication.
         */
        if (existingPublication != null) {
            if (existingPublication.isActive()) {
                return toResponse(
                        existingPublication,
                        publishedSessionRepository.countByPublicationId(
                                existingPublication.getId()
                        )
                );
            }

            throw conflict(
                    "This draft version was published previously. "
                            + "Create a new draft to replace the active timetable."
            );
        }

        var active = publicationRepository.findByActiveTrue()
                .orElse(null);

        Long activeId = active == null ? null : active.getId();

        if (!Objects.equals(
                activeId,
                request.expectedActivePublicationId()
        )) {
            throw conflict(
                    "The active timetable changed. Refresh before publishing."
            );
        }

        /*
         * Recheck against current records inside this transaction.
         * Never rely on a previous browser validation result.
         */
        var validation = validationService.validate(draft.getId());

        if (!validation.valid()) {
            throw new DraftPublicationValidationException(
                    validation.errors()
            );
        }

        var savedSessions = draftSessionRepository
                .findByDraftIdOrderByDayOfWeekAscStartTimeAscIdAsc(
                        draft.getId()
                );

        if (savedSessions.isEmpty()) {
            throw new DraftPublicationValidationException(
                    List.of("A timetable must contain allocated sessions.")
            );
        }

        var requirementIds = savedSessions.stream()
                .map(session -> session.getRequirementId())
                .distinct()
                .toList();

        var requirements = requirementRepository
                .findAllById(requirementIds)
                .stream()
                .collect(Collectors.toMap(
                        requirement -> requirement.getId(),
                        Function.identity()
                ));

        var roomIds = savedSessions.stream()
                .map(session -> session.getRoomId())
                .distinct()
                .toList();

        var rooms = classroomRepository.findAllById(roomIds)
                .stream()
                .collect(Collectors.toMap(
                        room -> room.getId(),
                        Function.identity()
                ));

        /*
         * Flush the old active flag before activating the replacement,
         * respecting the unique index for one active timetable.
         */
        if (active != null) {
            active.deactivate();
            publicationRepository.saveAndFlush(active);
        }

        var publication = publicationRepository.saveAndFlush(
                new TimetablePublication(
                        draft.getId(),
                        draft.getVersion(),
                        draft.getName(),
                        authenticatedEmail
                )
        );

        var publishedSessions = savedSessions.stream()
                .map(session -> {
                    var requirement = requirements.get(
                            session.getRequirementId()
                    );

                    var room = rooms.get(session.getRoomId());

                    if (requirement == null || room == null) {
                        throw conflict(
                                "Campus information changed. Refresh and try again."
                        );
                    }

                    if (session.getSessionNumber() < 1
                            || session.getSessionNumber()
                            > requirement.getSessionsPerWeek()) {
                        throw new DraftPublicationValidationException(
                                List.of(
                                        "A saved session number is invalid."
                                )
                        );
                    }

                    return new PublishedTimetableSession(
                            publication.getId(),
                            session,
                            requirement.getSubject().getName(),
                            room.getCode(),
                            requirement.getTeacher().getFullName(),
                            requirement.getSection().getCode()
                    );
                })
                .toList();

        publishedSessionRepository.saveAllAndFlush(publishedSessions);

        publication.activate();
        publicationRepository.saveAndFlush(publication);

        return toResponse(publication, publishedSessions.size());
    }

    private TimetablePublicationResponse toResponse(
            TimetablePublication publication,
            long sessionCount
    ) {
        return new TimetablePublicationResponse(
                publication.getId(),
                publication.getSourceDraftId(),
                publication.getSourceDraftVersion(),
                publication.getName(),
                publication.getPublishedAt(),
                sessionCount
        );
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                message
        );
    }
}