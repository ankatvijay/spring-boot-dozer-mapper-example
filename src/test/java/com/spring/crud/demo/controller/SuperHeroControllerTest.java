package com.spring.crud.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import net.bytebuddy.agent.VirtualMachine;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.dozer.DozerBeanMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SuperHeroControllerTest {


    private static ISuperHeroService superHeroService;

    private static SuperHeroController superHeroController;

    private static Tuple[] expectedSuperHeros = null;
    private static List<SuperHeroDTO> superHeroes;

    @BeforeAll
    static void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        File file = FileLoader.getFileFromResource("superheroes.json");

        DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        SuperHeroMapper superHeroMapper = new SuperHeroMapper(dozerBeanMapper);

        superHeroService = Mockito.mock(SuperHeroService.class);
        superHeroController = new SuperHeroController(superHeroService, superHeroMapper, objectMapper);

        superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHeroDTO.class));
        expectedSuperHeros = superHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(
                        superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);
    }

    @Test
    void testGivenNon_WhenFindAllSuperHeros_ThenReturnAllRecord() {
        // Given

        // When
        Mockito.when(superHeroService.findAllSuperHeros()).thenReturn(superHeroes);
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
    }

    @Test
    void testGivenId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        int id = 12;
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.findSuperHeroById(id)).thenReturn(Optional.of(expectedSuperHero));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.findSuperHeroById(id);

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertSuperHero(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).findSuperHeroById(id);
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(superHeroService.findSuperHeroById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> superHeroService.findSuperHeroById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
    }

    @Test
    void testGivenSuperHero_WhenFindSuperHerosByExample_ThenReturnRecords() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        Map<String, Object> map = new ObjectMapper().convertValue(expectedSuperHero, Map.class);

        // When
        Mockito.when(superHeroService.findSuperHerosByExample(expectedSuperHero)).thenReturn(List.of(expectedSuperHero));
        ResponseEntity<List<SuperHeroDTO>> actualSuperHeros = superHeroController.findSuperHerosByExample(map);

        // Then
        Assertions.assertThat(actualSuperHeros.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHeros.getBody()).isNotNull();
        Assertions.assertThat(actualSuperHeros.getBody().size()).isGreaterThan(0);
        assertSuperHero(expectedSuperHero, actualSuperHeros.getBody().get(0));
        Mockito.verify(superHeroService).findSuperHerosByExample(expectedSuperHero);
    }

    @Test
    void testGivenRandomSuperHero_WhenFindSuperHerosByExample_ThenReturnRecords() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);
        List<SuperHero> superHeroes = new ArrayList<>();

        // When
        Mockito.when(superHeroService.findSuperHerosByExample(expectedSuperHero)).thenReturn(superHeroes);
        List<SuperHero> actualSuperHeros = superHeroService.findSuperHerosByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(0);
        Mockito.verify(superHeroService).findSuperHerosByExample(expectedSuperHero);
    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.saveSuperHero(expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.saveSuperHero(expectedSuperHero);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
        Mockito.verify(superHeroService).saveSuperHero(expectedSuperHero);
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> superHeroService.saveSuperHero(expectedSuperHero))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedSuperHero.getId());

        // Then
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(true);
        Mockito.when(superHeroService.saveSuperHero(expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        SuperHero actualSuperHero = superHeroService.updateSuperHero(expectedSuperHero.getId(), expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
        Mockito.verify(superHeroService).saveSuperHero(expectedSuperHero);
    }

    @Test
    void testGivenNull_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    void testGivenSuperHeroAndIdDifferent_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(id, expectedSuperHero))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedSuperHero.getId());
    }

    @Test
    void testGivenSuperHeroAndId_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(expectedSuperHero.getId(), expectedSuperHero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedSuperHero.getId());
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(expectedSuperHero.getId())).thenReturn(true);
        Boolean flag = superHeroService.deleteSuperHero(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(superHeroService).existsBySuperHeroId(expectedSuperHero.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroService.existsBySuperHeroId(id)).thenReturn(false);
        Boolean flag = superHeroService.deleteSuperHero(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(superHeroService).existsBySuperHeroId(id);
    }

    @Test
    void testGiveNon_WhenDeleteAllSuperHero_ThenReturnNon() {
        // Given

        // When
        Mockito.doNothing().when(superHeroService).deleteAllSuperHero();
        superHeroService.deleteAllSuperHero();

        // Then
        Mockito.verify(superHeroService).deleteAllSuperHero();
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