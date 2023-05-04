package com.spring.crud.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorResponseDTO {
    private Integer status;
    private String currentDateTime;
    private String message;
}
