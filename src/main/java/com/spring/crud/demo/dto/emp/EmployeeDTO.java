package com.spring.crud.demo.dto.emp;

import lombok.Data;
import org.dozer.Mapping;

import java.io.Serializable;
import java.util.List;

@Data
public class EmployeeDTO implements Serializable {

    @Mapping(value = "id")
    private int id;

    @Mapping(value = "firstName")
    private String firstName;

    @Mapping(value = "lastName")
    private String lastName;

    @Mapping(value = "age")
    private int age;

    @Mapping(value = "noOfChildrens")
    private int noOfChildrens;

    @Mapping(value = "spouse")
    private boolean spouse;

    @Mapping(value = "this")
    private String dateOfJoining;

    @Mapping(value = "address")
    private AddressDTO address;

    @Mapping(value = "phoneNumbers")
    private List<PhoneNumberDTO> phoneNumbers;
}

