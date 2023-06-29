package com.spring.crud.demo.model.emp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ADDRESS", uniqueConstraints = {@UniqueConstraint(columnNames = {"ID"})})
public class Address implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Integer id;

    @Column(name = "STREET_ADDRESS")
    private String streetAddress;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "POSTAL_ADDRESS")
    private String postalCode;

    @JsonBackReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "ID", nullable = false)
    private Employee employee;
}