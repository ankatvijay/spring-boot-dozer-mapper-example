package com.spring.crud.demo.exception;

import com.spring.crud.demo.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = {InternalServerErrorException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO internalServerErrorException(InternalServerErrorException ex) {
        return ResponseDTO.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).currentDateTime(String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis())).message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO notFoundException(NotFoundException ex) {
        return ResponseDTO.builder().status(HttpStatus.NOT_FOUND.value()).currentDateTime(String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis())).message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = {RecordFoundException.class})
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseDTO foundException(RecordFoundException ex) {
        return ResponseDTO.builder().status(HttpStatus.FOUND.value()).currentDateTime(String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis())).message(ex.getMessage()).build();
    }

    @ExceptionHandler(value = {NullPointerException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO foundNullPointerException(NullPointerException ex) {
        return ResponseDTO.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).currentDateTime(String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis())).message(ex.getMessage()).build();
    }
}
