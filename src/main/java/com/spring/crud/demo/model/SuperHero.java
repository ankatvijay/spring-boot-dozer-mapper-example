package com.spring.crud.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SUPER_HERO")
public class SuperHero implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SUPER_NAME")
    private String superName;

    @Column(name = "PROFESSION")
    private String profession;

    @Column(name = "AGE")
    private Integer age;

    @Column(name = "CAN_FLY")
    private Boolean canFly;
}