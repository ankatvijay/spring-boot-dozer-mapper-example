package com.spring.crud.demo.model.emp;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"employee"})
@Entity
@Table(name = "PHONE_NUMBER", uniqueConstraints = {@UniqueConstraint(columnNames = {"ID"})})
public class PhoneNumber implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private int id;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "NUMBER")
    private String number;

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "ID")
    private Employee employee;
}