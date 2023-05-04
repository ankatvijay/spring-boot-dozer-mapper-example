package com.spring.crud.demo.dto;

import lombok.Data;
import org.dozer.Mapping;

import java.io.Serializable;

@Data
public class SuperHeroDTO implements Serializable {

    @Mapping(value = "id")
    private Integer id;

    @Mapping(value = "name")
    private String name;

    @Mapping(value = "superName")
    private String superName;

    @Mapping(value = "profession")
    private String profession;

    @Mapping(value = "age")
    private Integer age;

    @Mapping(value = "canFly")
    private Boolean canFly;
}