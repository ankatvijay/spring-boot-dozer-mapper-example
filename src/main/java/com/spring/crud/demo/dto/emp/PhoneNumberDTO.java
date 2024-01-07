package com.spring.crud.demo.dto.emp;


import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.io.Serializable;

@Getter
@Setter
public class PhoneNumberDTO implements Serializable {

    @Mapping(value = "id")
    private Integer id;

    @Mapping(value = "type")
    private String type;

    @Mapping(value = "number")
    private String number;
}