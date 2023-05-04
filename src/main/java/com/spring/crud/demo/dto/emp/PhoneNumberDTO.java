package com.spring.crud.demo.dto.emp;


import lombok.Data;
import org.dozer.Mapping;

import java.io.Serializable;

@Data
public class PhoneNumberDTO implements Serializable {

    @Mapping(value = "id")
    private Integer id;

    @Mapping(value = "type")
    private String type;

    @Mapping(value = "number")
    private String number;
}