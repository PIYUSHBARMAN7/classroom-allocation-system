package com.classroomallocation.backend.exception;

import java.util.List;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.classroomallocation.backend.controller.TimetablePublicationController;

@RestControllerAdvice(
        assignableTypes = TimetablePublicationController.class
)
public class TimetablePublicationExceptionHandler {

    public record PublicationError(
            String message,
            List<String> errors
    ) {
    }

    @ExceptionHandler(DraftPublicationValidationException.class)
    public ResponseEntity<PublicationError> invalidDraft(
            DraftPublicationValidationException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .cacheControl(CacheControl.noStore())
                .body(new PublicationError(
                        "The draft cannot be published.",
                        exception.getErrors()
                ));
    }

    @ExceptionHandler({
            ConcurrencyFailureException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<PublicationError> publicationConflict(
            RuntimeException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .cacheControl(CacheControl.noStore())
                .body(new PublicationError(
                        "Publication could not be completed. "
                                + "Refresh the timetable information and try again.",
                        List.of()
                ));
    }
}