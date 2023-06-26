package com.spring.crud.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.mapper.SuperHeroMapper;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.ISuperHeroService;
import com.spring.crud.demo.service.impl.SuperHeroService;
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
        Mockito.verify(superHeroService).findAllSuperHeros();
        superHeroes.forEach(superHero -> Mockito.verify(superHeroMapper).convertFromEntityToDto(superHero));
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
        Mockito.verify(superHeroMapper).convertFromEntityToDto(expectedSuperHero);
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
        Map map = new ObjectMapper().convertValue(expectedSuperHero, Map.class);

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
        Mockito.verify(superHeroMapper).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenRandomSuperHero_WhenFindSuperHerosByExample_ThenReturnRecords() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);
        Map map = objectMapper.convertValue(expectedSuperHero, Map.class);
        List<SuperHero> superHeroes = new ArrayList<>();

        // When
        Mockito.when(superHeroService.findSuperHerosByExample(expectedSuperHero)).thenReturn(superHeroes);
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, SuperHeroDTO.class));
        ResponseEntity<List<SuperHeroDTO>> actualSuperHeros = superHeroController.findSuperHerosByExample(map);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();

        Assertions.assertThat(actualSuperHeros.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHeros.getBody()).isNotNull();
        Assertions.assertThat(actualSuperHeros.getBody()).isEmpty();
        Assertions.assertThat(actualSuperHeros.getBody().size()).isEqualTo(0);
        Mockito.verify(superHeroService).findSuperHerosByExample(expectedSuperHero);
        Mockito.verify(superHeroMapper).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper).convertFromEntityToDto(Mockito.any());
    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.saveSuperHero(expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.saveSuperHero(superHeroMapper.convertFromEntityToDto(expectedSuperHero));

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertSuperHero(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).saveSuperHero(expectedSuperHero);
        Mockito.verify(superHeroMapper).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> superHeroController.saveSuperHero(superHeroMapper.convertFromEntityToDto(expectedSuperHero)))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedSuperHero.getId());

        // Then
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(true);
        Mockito.when(superHeroService.saveSuperHero(expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.updateSuperHero(expectedSuperHero.getId(), superHeroMapper.convertFromEntityToDto(expectedSuperHero));

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertSuperHero(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
        Mockito.verify(superHeroService).saveSuperHero(expectedSuperHero);
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroController.updateSuperHero(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    void testGivenSuperHeroAndIdDifferent_WhenUpdateSuperHero_ThenThrowError() throws IOException {
        // Given
        int id = RandomUtils.nextInt();
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroController.updateSuperHero(id, superHeroMapper.convertFromEntityToDto(expectedSuperHero)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedSuperHero.getId());
    }

    @Test
    void testGivenSuperHeroAndId_WhenUpdateSuperHero_ThenThrowError() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> superHeroController.updateSuperHero(expectedSuperHero.getId(), superHeroMapper.convertFromEntityToDto(expectedSuperHero)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedSuperHero.getId());
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(true);
        ResponseEntity<Boolean> flag = superHeroController.deleteSuperHero(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(flag.getBody()).isTrue();
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(id)).thenReturn(false);
        ResponseEntity<Boolean> flag = superHeroController.deleteSuperHero(id);

        // Then
        Assertions.assertThat(flag.getBody()).isFalse();
        Mockito.verify(superHeroService).existsBySuperHeroId(id);
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