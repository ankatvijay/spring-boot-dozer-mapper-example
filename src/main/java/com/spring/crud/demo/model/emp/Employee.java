package com.spring.crud.demo.model.emp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spring.crud.demo.jakson.LocalDateTimeDeserializer;
import com.spring.crud.demo.jakson.LocalDateTimeSerializer;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@XmlRootElement
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EMPLOYEE", uniqueConstraints = {@UniqueConstraint(columnNames = {"ID"})})
public class Employee implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Integer id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "AGE")
    private Integer age;

    @Column(name = "NO_OF_CHILDRENS")
    private Integer noOfChildrens;

    @Column(name = "SPOUSE")
    private Boolean spouse;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Column(name = "DATE_OF_JOINING")
    private LocalDateTime dateOfJoining;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "HOBBIES", joinColumns = @JoinColumn(name = "ID"))
    @Column(name = "HOBBY")
    private List<String> hobbies;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "employee")
    private Address address;

    @JsonManagedReference
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "employee")
    private List<PhoneNumber> phoneNumbers;


}

