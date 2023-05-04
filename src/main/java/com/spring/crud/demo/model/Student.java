package com.spring.crud.demo.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spring.crud.demo.jakson.LocalDateDeserializer;
import com.spring.crud.demo.jakson.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDate;

@XmlRootElement
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "STUDENT")
public class Student implements Serializable {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Integer id;

	@Column(name = "ROLL_NO")
	private Integer rollNo;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	@Column(name = "DATE_OF_BIRTH")
	private LocalDate dateOfBirth;

	@Column(name = "MARKS")
	private Float marks;
	
}
