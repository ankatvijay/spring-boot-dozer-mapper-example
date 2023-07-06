package com.spring.crud.demo.dto.emp;

import lombok.Data;
import org.dozer.Mapping;

import java.io.Serializable;
import java.util.List;

@Data
public class EmployeeDTO implements Serializable {

    @Mapping(value = "id")
    private Integer id;

    @Mapping(value = "firstName")
    private String firstName;

    @Mapping(value = "lastName")
    private String lastName;

    @Mapping(value = "age")
    private Integer age;

    @Mapping(value = "noOfChildrens")
    private Integer noOfChildrens;

    @Mapping(value = "spouse")
    private Boolean spouse;

    @Mapping(value = "this")
    private String dateOfJoining;

    @Mapping(value = "hobbies")
    private List<String> hobbies;

    @Mapping(value = "address")
    private AddressDTO address;

    @Mapping(value = "phoneNumbers")
    private List<PhoneNumberDTO> phoneNumbers;
}

