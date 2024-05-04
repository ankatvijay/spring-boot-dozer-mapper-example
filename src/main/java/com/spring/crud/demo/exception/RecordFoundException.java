package com.spring.crud.demo.exception;

import java.io.Serial;

public class RecordFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RecordFoundException(String message) {
        super(message);
    }
}
