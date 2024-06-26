package com.spring.crud.demo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.io.Serializable;

@Getter
@Setter
public class StudentDTO implements Serializable {

	@Mapping(value = "id")
	private Integer id;

	@Mapping(value = "rollNo")
	private Integer rollNo;

	@Mapping(value = "firstName")
	private String firstName;

	@Mapping(value = "lastName")
	private String lastName;

	@Mapping(value = "this")
	private String dateOfBirth;

	@Mapping(value = "marks")
	private Float marks;
}
