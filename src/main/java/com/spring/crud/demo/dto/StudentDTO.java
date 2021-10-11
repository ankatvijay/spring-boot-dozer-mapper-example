package com.spring.crud.demo.dto;

import lombok.Data;
import org.dozer.Mapping;

import java.io.Serializable;

@Data
public class StudentDTO implements Serializable {

	@Mapping(value = "id")
	private int id;

	@Mapping(value = "rollNo")
	private int rollNo;

	@Mapping(value = "firstName")
	private String firstName;

	@Mapping(value = "lastName")
	private String lastName;

	@Mapping(value = "this")
	private String dateOfBirth;

	@Mapping(value = "marks")
	private float marks;
}
