package com.spring.crud.demo.mapper;

import com.spring.crud.demo.utils.Constant;

import java.time.format.DateTimeFormatter;

public interface BaseMapper<Entity, DTO> {

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(Constant.TIME_FORMAT);
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constant.DATE_FORMAT);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT);

    Entity convertFromDtoToEntity(DTO dto);

    DTO convertFromEntityToDto(Entity entity);
}
