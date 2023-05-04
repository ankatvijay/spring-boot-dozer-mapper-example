package com.spring.crud.demo.mapper;

import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.model.SuperHero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Component(value = "superHeroMapper")
public class SuperHeroMapper implements Serializable {

    private final DozerBeanMapper dozerBeanMapper;

    public SuperHero convertFromDtoToEntity(SuperHeroDTO superHeroDTO) {
        return dozerBeanMapper.map(superHeroDTO, SuperHero.class);
    }

    public SuperHeroDTO convertFromEntityToDto(SuperHero superHero) {
        return dozerBeanMapper.map(superHero, SuperHeroDTO.class);
    }
}