package com.classroomallocation.backend.exception;

import java.util.List;

public class DraftPublicationValidationException extends RuntimeException {

    private final List<String> errors;

    public DraftPublicationValidationException(List<String> errors) {
        super("The draft did not pass publication validation.");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}