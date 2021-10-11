package com.spring.crud.demo.model.emp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spring.crud.demo.jakson.LocalDateTimeDeserializer;
import com.spring.crud.demo.jakson.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EMPLOYEE")
public class Employee implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private int id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "AGE")
    private int age;

    @Column(name = "NO_OF_CHILDRENS")
    private int noOfChildrens;

    @Column(name = "SPOUSE")
    private boolean spouse;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = "DATE_OF_JOINING")
    private LocalDateTime dateOfJoining;

    @JsonManagedReference
    @OneToOne(cascade = { 
        		CascadeType.MERGE,
   	    		CascadeType.PERSIST,
   	    		CascadeType.REMOVE
    })
    @JoinColumn(name="ADDRESS_ID")
    private Address address;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee", 
	    cascade = { 
	    		CascadeType.MERGE,
	    		CascadeType.PERSIST,
	    		CascadeType.REMOVE
    })
    private List<PhoneNumber> phoneNumbers;

    @ElementCollection
    @CollectionTable(name="HOBBIES", joinColumns = @JoinColumn(name="ID"))
    @Column(name="HOBBY")
    private List<String> hobbies = new ArrayList<>();

}

