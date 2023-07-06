package com.spring.crud.demo.controller.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.SuperHeroController;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.SuperHeroMapper;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.ISuperHeroService;
import com.spring.crud.demo.service.mock.SuperHeroService;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SuperHeroControllerTest {

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private static SuperHeroMapper superHeroMapper;
    private static ISuperHeroService superHeroService;
    private static SuperHeroController superHeroController;

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        typeFactory = objectMapper.getTypeFactory();
        file = FileLoader.getFileFromResource("superheroes.json");

        superHeroMapper = Mockito.mock(SuperHeroMapper.class);
        superHeroService = Mockito.mock(SuperHeroService.class);
        superHeroController = new SuperHeroController(superHeroService, superHeroMapper, objectMapper);
    }

    @Test
    void testGivenNon_WhenFindAllSuperHeros_ThenReturnAllRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        Tuple[] expectedSuperHeros = superHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(
                        superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(superHeroService.findAllSuperHeros()).thenReturn(superHeroes);
        superHeroes.forEach(superHero -> Mockito.when(superHeroMapper.convertFromEntityToDto(superHero)).thenReturn(objectMapper.convertValue(superHero, SuperHeroDTO.class)));
        ResponseEntity<List<SuperHeroDTO>> actualSuperHeros = superHeroController.findAllSuperHeros();

        // Then
        Assertions.assertThat(actualSuperHeros.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHeros.getBody()).isNotNull();
        Assertions.assertThat(actualSuperHeros.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(actualSuperHeros.getBody())
                .extracting(SuperHeroDTO::getName,
                        SuperHeroDTO::getSuperName,
                        SuperHeroDTO::getProfession,
                        SuperHeroDTO::getAge,
                        SuperHeroDTO::getCanFly)
                .containsExactly(expectedSuperHeros);
        Mockito.verify(superHeroService, Mockito.atLeastOnce()).findAllSuperHeros();
        superHeroes.forEach(superHero -> Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(superHero));
    }

    @Test
    void testGivenNon_WhenFindAllSuperHeros_ThenReturnError() {
        // Given
        List<SuperHero> superHeroList = new ArrayList<>();

        // When & Then
        Mockito.when(superHeroService.findAllSuperHeros()).thenReturn(superHeroList);
        Assertions.assertThatThrownBy(() -> superHeroController.findAllSuperHeros())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found");
        Mockito.verify(superHeroService).findAllSuperHeros();
    }

    @Test
    void testGivenId_WhenFindSuperHeroById_ThenReturnRecord() throws IOException {
        // Given
        int id = 12;
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.findSuperHeroById(id)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromEntityToDto(expectedSuperHero)).thenReturn(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.findSuperHeroById(id);

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertSuperHero(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).findSuperHeroById(id);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(expectedSuperHero);
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(superHeroService.findSuperHeroById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> superHeroController.findSuperHeroById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
    }

    @Test
    void testGivenSuperHero_WhenFindSuperHerosByExample_ThenReturnRecords() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        Map<String, Object> map = new ObjectMapper().convertValue(expectedSuperHero, Map.class);

        // When
        Mockito.when(superHeroService.findSuperHerosByExample(expectedSuperHero)).thenReturn(List.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, SuperHeroDTO.class));
        ResponseEntity<List<SuperHeroDTO>> actualSuperHeros = superHeroController.findSuperHerosByExample(map);

        // Then
        Assertions.assertThat(actualSuperHeros.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHeros.getBody()).isNotNull();
        Assertions.assertThat(actualSuperHeros.getBody().size()).isGreaterThan(0);
        assertSuperHero(expectedSuperHero, actualSuperHeros.getBody().get(0));
        Mockito.verify(superHeroService).findSuperHerosByExample(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenRandomSuperHero_WhenFindSuperHerosByExample_ThenReturnError() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);
        Map<String, Object> map = objectMapper.convertValue(expectedSuperHero, Map.class);
        List<SuperHero> superHeroes = new ArrayList<>();

        // When & Then
        Mockito.when(superHeroService.findSuperHerosByExample(expectedSuperHero)).thenReturn(superHeroes);
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Assertions.assertThatThrownBy(() -> superHeroController.findSuperHerosByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + map);


        Mockito.verify(superHeroService).findSuperHerosByExample(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.saveSuperHero(expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.saveSuperHero(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertSuperHero(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).saveSuperHero(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.saveSuperHero(expectedSuperHero)).thenReturn(Optional.empty());
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Assertions.assertThatThrownBy(() -> superHeroController.saveSuperHero(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");

        // Then
        Mockito.verify(superHeroService).saveSuperHero(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.updateSuperHero(expectedSuperHero.getId(), expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.updateSuperHero(expectedSuperHero.getId(), objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertSuperHero(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).updateSuperHero(expectedSuperHero.getId(), expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(superHeroService.updateSuperHero(id, null)).thenReturn(Optional.empty());
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> superHeroController.updateSuperHero(id, null))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");
        Mockito.verify(superHeroService).updateSuperHero(id, null);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.deleteSuperHero(expectedSuperHero.getId())).thenReturn(true);
        ResponseEntity<Boolean> flag = superHeroController.deleteSuperHero(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(flag.getBody()).isTrue();
        Mockito.verify(superHeroService).deleteSuperHero(expectedSuperHero.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroService.deleteSuperHero(id)).thenReturn(false);
        ResponseEntity<Boolean> flag = superHeroController.deleteSuperHero(id);

        // Then
        Assertions.assertThat(flag.getBody()).isFalse();
        Mockito.verify(superHeroService).deleteSuperHero(id);
    }


    private void assertSuperHero(SuperHero expectedSuperHero, SuperHeroDTO actualSuperHero) {
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero.getName()).isEqualTo(expectedSuperHero.getName());
        Assertions.assertThat(actualSuperHero.getSuperName()).isEqualTo(expectedSuperHero.getSuperName());
        Assertions.assertThat(actualSuperHero.getProfession()).isEqualTo(expectedSuperHero.getProfession());
        Assertions.assertThat(actualSuperHero.getAge()).isEqualTo(expectedSuperHero.getAge());
        Assertions.assertThat(actualSuperHero.getCanFly()).isEqualTo(expectedSuperHero.getCanFly());
    }
}