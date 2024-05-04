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
        return new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()),ex.getMessage());
    }

    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO notFoundException(NotFoundException ex) {
        return new ResponseDTO(HttpStatus.NOT_FOUND.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()), ex.getMessage());
    }

    @ExceptionHandler(value = {RecordFoundException.class})
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseDTO foundException(RecordFoundException ex) {
        return new ResponseDTO(HttpStatus.FOUND.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()), ex.getMessage());
    }

    @ExceptionHandler(value = {NullPointerException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO foundNullPointerException(NullPointerException ex) {
        return new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()), ex.getMessage());
    }
}
