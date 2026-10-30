package com.classroomallocation.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.classroomallocation.backend.dto.response.MyTimetableResponse;
import com.classroomallocation.backend.dto.response.MyTimetableResponse.Status;
import com.classroomallocation.backend.entity.PublishedTimetableSession;
import com.classroomallocation.backend.entity.TimetablePublication;
import com.classroomallocation.backend.enums.Role;
import com.classroomallocation.backend.repository.PublishedTimetableSessionRepository;
import com.classroomallocation.backend.repository.StudentSectionAssignmentRepository;
import com.classroomallocation.backend.repository.TeacherRepository;
import com.classroomallocation.backend.repository.TimetablePublicationRepository;
import com.classroomallocation.backend.repository.UserRepository;

@Service
@PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
public class PersonalTimetableService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentSectionAssignmentRepository assignmentRepository;
    private final TimetablePublicationRepository publicationRepository;
    private final PublishedTimetableSessionRepository sessionRepository;

    public PersonalTimetableService(
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            StudentSectionAssignmentRepository assignmentRepository,
            TimetablePublicationRepository publicationRepository,
            PublishedTimetableSessionRepository sessionRepository
    ) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.assignmentRepository = assignmentRepository;
        this.publicationRepository = publicationRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )
    public MyTimetableResponse getMyTimetable(String authenticatedEmail) {
        var user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                ));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        boolean teacherView;
        Long scopeId;

        if (user.getRole() == Role.TEACHER) {
            teacherView = true;

            var teacher = teacherRepository.findByUser_Id(user.getId())
                    .orElse(null);

            if (teacher == null) {
                return response(
                        Status.LINK_REQUIRED,
                        "Your account is not linked to a teacher record. "
                                + "Contact your institution administrator.",
                        null,
                        List.of()
                );
            }

            if (!teacher.isEnabled()) {
                return response(
                        Status.PROFILE_DISABLED,
                        "Your teacher record is disabled. "
                                + "Contact your institution administrator.",
                        null,
                        List.of()
                );
            }

            scopeId = teacher.getId();

        } else if (user.getRole() == Role.STUDENT) {
            teacherView = false;

            var assignment = assignmentRepository.findById(user.getId())
                    .orElse(null);

            if (assignment == null) {
                return response(
                        Status.LINK_REQUIRED,
                        "Your account has no section assigned. "
                                + "Contact your institution administrator.",
                        null,
                        List.of()
                );
            }

            if (!assignment.getSection().isEnabled()) {
                return response(
                        Status.PROFILE_DISABLED,
                        "Your assigned section is disabled. "
                                + "Contact your institution administrator.",
                        null,
                        List.of()
                );
            }

            scopeId = assignment.getSection().getId();

        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        var publication = publicationRepository.findByActiveTrue()
                .orElse(null);

        if (publication == null) {
            return response(
                    Status.NO_PUBLICATION,
                    "No timetable has been published yet.",
                    null,
                    List.of()
            );
        }

        List<PublishedTimetableSession> sessions;

        if (teacherView) {
            sessions = sessionRepository
                    .findByPublicationIdAndTeacherIdOrderByDayOfWeekAscStartTimeAscIdAsc(
                            publication.getId(),
                            scopeId
                    );
        } else {
            sessions = sessionRepository
                    .findByPublicationIdAndSectionIdOrderByDayOfWeekAscStartTimeAscIdAsc(
                            publication.getId(),
                            scopeId
                    );
        }

        if (sessions.isEmpty()) {
            return response(
                    Status.NO_SESSIONS,
                    teacherView
                            ? "The active timetable has no classes assigned to you."
                            : "The active timetable has no classes for your section.",
                    publication,
                    List.of()
            );
        }

        return response(
                Status.AVAILABLE,
                "Your published weekly timetable.",
                publication,
                sessions
        );
    }

    private MyTimetableResponse response(
            Status status,
            String message,
            TimetablePublication publication,
            List<PublishedTimetableSession> sessions
    ) {
        MyTimetableResponse.PublicationSummary publicationSummary =
                publication == null
                        ? null
                        : new MyTimetableResponse.PublicationSummary(
                                publication.getId(),
                                publication.getName(),
                                publication.getPublishedAt()
                        );

        var sessionSummaries = sessions.stream()
                .map(session -> new MyTimetableResponse.SessionSummary(
                        session.getId(),
                        session.getSubjectName(),
                        session.getRoomCode(),
                        session.getTeacherName(),
                        session.getSectionCode(),
                        session.getDayOfWeek(),
                        session.getStartTime(),
                        session.getEndTime()
                ))
                .toList();

        return new MyTimetableResponse(
                status,
                message,
                publicationSummary,
                sessionSummaries
        );
    }
}