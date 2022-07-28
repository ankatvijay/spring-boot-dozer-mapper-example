package com.spring.crud.demo.model.emp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spring.crud.demo.jakson.LocalDateTimeDeserializer;
import com.spring.crud.demo.jakson.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "EMPLOYEE", uniqueConstraints = {@UniqueConstraint(columnNames = {"ID"})})
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

    @ElementCollection
    @CollectionTable(name = "HOBBIES", joinColumns = @JoinColumn(name = "ID"))
    @Column(name = "HOBBY")
    private List<String> hobbies = new ArrayList<>();

    @JsonManagedReference
    @OneToOne(cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.REMOVE
    })
    @JoinColumn(name = "ADDRESS_ID")
    private Address address;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employee",
            orphanRemoval = true,
            cascade = {
                    CascadeType.MERGE,
                    CascadeType.PERSIST,
                    CascadeType.REMOVE
            })
    private List<PhoneNumber> phoneNumbers;


}

