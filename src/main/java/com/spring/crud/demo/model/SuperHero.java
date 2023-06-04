package com.spring.crud.demo.model;

import lombok.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@Getter
@Setter
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

    public SuperHero(String name, String superName, String profession, Integer age, Boolean canFly) {
        this.name = name;
        this.superName = superName;
        this.profession = profession;
        this.age = age;
        this.canFly = canFly;
    }
}