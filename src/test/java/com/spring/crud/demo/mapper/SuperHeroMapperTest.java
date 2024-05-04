package com.spring.crud.demo.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.utils.FileLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SpringBootTest
class SuperHeroMapperTest {

    @Autowired
    private SuperHeroMapper superHeroMapper;
    private static final File file = FileLoader.getFileFromResource("superheroes.json");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();
    private static SuperHero superHero;
    private static SuperHeroDTO superHeroDTO;


    @BeforeAll
    static void init() throws IOException {
        List<SuperHero> superHeros = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        superHero = superHeros.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);

        List<SuperHeroDTO> superHeroDTOs = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        superHeroDTO = superHeroDTOs.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHeroDTO::new);
    }

    @Test
    void convertFromDtoToEntity() {
        // Given

        // When
        SuperHero actualSuperHero = superHeroMapper.convertFromDtoToEntity(superHeroDTO);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero.getName()).isEqualTo(superHeroDTO.getName());
        Assertions.assertThat(actualSuperHero.getSuperName()).isEqualTo(superHeroDTO.getSuperName());
        Assertions.assertThat(actualSuperHero.getProfession()).isEqualTo(superHeroDTO.getProfession());
        Assertions.assertThat(actualSuperHero.getAge()).isEqualTo(superHeroDTO.getAge());
        Assertions.assertThat(actualSuperHero.getCanFly()).isEqualTo(superHeroDTO.getCanFly());
    }

    @Test
    void convertFromEntityToDto() {
        // Given

        // When
        SuperHeroDTO actualSuperHeroDTO = superHeroMapper.convertFromEntityToDto(superHero);

        // Then
        Assertions.assertThat(actualSuperHeroDTO).isNotNull();
        Assertions.assertThat(actualSuperHeroDTO.getName()).isEqualTo(superHero.getName());
        Assertions.assertThat(actualSuperHeroDTO.getSuperName()).isEqualTo(superHero.getSuperName());
        Assertions.assertThat(actualSuperHeroDTO.getProfession()).isEqualTo(superHero.getProfession());
        Assertions.assertThat(actualSuperHeroDTO.getAge()).isEqualTo(superHero.getAge());
        Assertions.assertThat(actualSuperHeroDTO.getCanFly()).isEqualTo(superHero.getCanFly());
    }
}