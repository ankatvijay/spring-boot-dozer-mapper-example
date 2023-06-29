package com.spring.crud.demo.exception;

import com.spring.crud.demo.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestControllerAdvice
public class RestExceptionHandler {

    private final DateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");

    @ExceptionHandler(value = {InternalServerErrorException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO internalServerErrorException(InternalServerErrorException ex) {
        return new ErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), String.format("%1$TH:%1$TM:%1$TS", System.currentTimeMillis()), ex.getMessage());
    }

    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO notFoundException(NotFoundException ex) {
        return new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), dateFormat.format(new Date(System.currentTimeMillis())), ex.getMessage());
    }

    @ExceptionHandler(value = {RecordFoundException.class})
    @ResponseStatus(HttpStatus.FOUND)
    public ErrorResponseDTO foundException(RecordFoundException ex) {
        return new ErrorResponseDTO(HttpStatus.FOUND.value(), dateFormat.format(new Date(System.currentTimeMillis())), ex.getMessage());
    }

}
