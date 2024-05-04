package com.spring.crud.demo.dto.emp;

import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.io.Serializable;

@Getter
@Setter
public class AddressDTO implements Serializable {

    @Mapping(value = "id")
    private Integer id;

    @Mapping(value = "streetAddress")
    private String streetAddress;

    @Mapping(value = "city")
    private String city;

    @Mapping(value = "state")
    private String state;

    @Mapping(value = "country")
    private String country;

    @Mapping(value = "postalCode")
    private String postalCode;
}