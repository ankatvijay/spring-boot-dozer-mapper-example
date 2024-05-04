package com.spring.crud.demo.mapper;

import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.model.SuperHero;
import lombok.RequiredArgsConstructor;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component(value = "superHeroMapper")
public class SuperHeroMapper implements BaseMapper<SuperHero, SuperHeroDTO> {

    private final DozerBeanMapper dozerBeanMapper;

    @Override
    public SuperHero convertFromDtoToEntity(SuperHeroDTO superHeroDTO) {
        return dozerBeanMapper.map(superHeroDTO, SuperHero.class);
    }

    @Override
    public SuperHeroDTO convertFromEntityToDto(SuperHero superHero) {
        return dozerBeanMapper.map(superHero, SuperHeroDTO.class);
    }
}