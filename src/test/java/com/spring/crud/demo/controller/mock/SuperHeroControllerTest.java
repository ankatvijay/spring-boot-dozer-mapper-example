package com.spring.crud.demo.controller.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.controller.BaseControllerTest;
import com.spring.crud.demo.controller.SuperHeroController;
import com.spring.crud.demo.dto.ResponseDTO;
import com.spring.crud.demo.dto.SuperHeroDTO;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.mapper.SuperHeroMapper;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.SuperHeroService;
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
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SuperHeroControllerTest implements BaseControllerTest<SuperHero, SuperHeroDTO> {

    private static File file;
    private static ObjectMapper objectMapper;
    private static TypeFactory typeFactory;
    private static SuperHeroMapper superHeroMapper;
    private static SuperHeroService superHeroService;
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
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        Tuple[] expectedSuperHeroes = superHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(
                        superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(superHeroService.getAllRecords()).thenReturn(superHeroes);
        superHeroes.forEach(superHero -> Mockito.when(superHeroMapper.convertFromEntityToDto(superHero)).thenReturn(objectMapper.convertValue(superHero, SuperHeroDTO.class)));
        ResponseEntity<List<SuperHeroDTO>> actualSuperHeroes = superHeroController.getAllRecords();

        // Then
        Assertions.assertThat(actualSuperHeroes.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHeroes.getBody()).isNotNull();
        Assertions.assertThat(actualSuperHeroes.getBody().size()).isGreaterThan(0);
        Assertions.assertThat(actualSuperHeroes.getBody())
                .extracting(SuperHeroDTO::getName,
                        SuperHeroDTO::getSuperName,
                        SuperHeroDTO::getProfession,
                        SuperHeroDTO::getAge,
                        SuperHeroDTO::getCanFly)
                .containsExactly(expectedSuperHeroes);
        Mockito.verify(superHeroService, Mockito.atLeastOnce()).getAllRecords();
        superHeroes.forEach(superHero -> Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(superHero));
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenThrowException() {
        // Given
        List<SuperHero> superHeroList = new ArrayList<>();

        // When & Then
        Mockito.when(superHeroService.getAllRecords()).thenReturn(superHeroList);
        Assertions.assertThatThrownBy(() -> superHeroController.getAllRecords())
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found");
        Mockito.verify(superHeroService, Mockito.atLeastOnce()).getAllRecords();
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        int id = 12;
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.getRecordsById(id)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromEntityToDto(expectedSuperHero)).thenReturn(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.getRecordsById(id);

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertRecord(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).getRecordsById(id);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(expectedSuperHero);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        Optional<SuperHero> superHeroList = Optional.empty();

        // When & Then
        Mockito.when(superHeroService.getRecordsById(id)).thenReturn(superHeroList);
        Assertions.assertThatThrownBy(() -> superHeroController.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
        Mockito.verify(superHeroService).getRecordsById(id);
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        SuperHeroDTO map = new ObjectMapper().convertValue(expectedSuperHero, SuperHeroDTO.class);

        // When
        Mockito.when(superHeroService.getAllRecordsByExample(expectedSuperHero)).thenReturn(List.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(map, SuperHeroDTO.class));
        ResponseEntity<List<SuperHeroDTO>> actualSuperHeroes = superHeroController.getAllRecordsByExample(map);

        // Then
        Assertions.assertThat(actualSuperHeroes.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualSuperHeroes.getBody()).isNotNull();
        Assertions.assertThat(actualSuperHeroes.getBody().size()).isGreaterThan(0);
        assertRecord(expectedSuperHero, actualSuperHeroes.getBody().get(0));
        Mockito.verify(superHeroService).getAllRecordsByExample(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenThrowException() throws JsonProcessingException {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);
        SuperHeroDTO map = objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class);
        List<SuperHero> superHeroes = new ArrayList<>();

        // When & Then
        Mockito.when(superHeroService.getAllRecordsByExample(expectedSuperHero)).thenReturn(superHeroes);
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Assertions.assertThatThrownBy(() -> superHeroController.getAllRecordsByExample(map))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with map " + objectMapper.writeValueAsString(map));


        Mockito.verify(superHeroService).getAllRecordsByExample(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroService.insertRecord(expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.insertRecord(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertRecord(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).insertRecord(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.insertRecord(expectedSuperHero)).thenReturn(Optional.empty());
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Assertions.assertThatThrownBy(() -> superHeroController.insertRecord(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");

        // Then
        Mockito.verify(superHeroService).insertRecord(expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.updateRecord(expectedSuperHero.getId(), expectedSuperHero)).thenReturn(Optional.of(expectedSuperHero));
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(expectedSuperHero);
        Mockito.when(superHeroMapper.convertFromEntityToDto(Mockito.any())).thenReturn(objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));
        ResponseEntity<SuperHeroDTO> actualSuperHero = superHeroController.updateRecord(expectedSuperHero.getId(), objectMapper.convertValue(expectedSuperHero, SuperHeroDTO.class));

        // Then
        Assertions.assertThat(actualSuperHero.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(actualSuperHero.getBody()).isNotNull();
        assertRecord(expectedSuperHero, actualSuperHero.getBody());
        Mockito.verify(superHeroService).updateRecord(expectedSuperHero.getId(), expectedSuperHero);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromEntityToDto(Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(superHeroService.updateRecord(id, null)).thenReturn(Optional.empty());
        Mockito.when(superHeroMapper.convertFromDtoToEntity(Mockito.any())).thenReturn(null);
        Assertions.assertThatThrownBy(() -> superHeroController.updateRecord(id, null))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Something went wrong");
        Mockito.verify(superHeroService).updateRecord(id, null);
        Mockito.verify(superHeroMapper, Mockito.atLeastOnce()).convertFromDtoToEntity(Mockito.any());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroService.deleteRecordById(expectedSuperHero.getId())).thenReturn(true);
        ResponseEntity<ResponseDTO> response = superHeroController.deleteRecordById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().message()).isEqualTo("Record deleted with id " + expectedSuperHero.getId());
        Mockito.verify(superHeroService).deleteRecordById(expectedSuperHero.getId());

    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroService.deleteRecordById(id)).thenReturn(false);
        Assertions.assertThatThrownBy(() -> superHeroController.deleteRecordById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
        Mockito.verify(superHeroService).deleteRecordById(id);
    }


    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        Mockito.doNothing().when(superHeroService).deleteAllRecords();

        // Then
        ResponseEntity<Void> response = superHeroController.deleteAllRecords();

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    public void assertRecord(SuperHero expectedRecord, SuperHeroDTO actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getName()).isEqualTo(expectedRecord.getName());
        Assertions.assertThat(actualRecord.getSuperName()).isEqualTo(expectedRecord.getSuperName());
        Assertions.assertThat(actualRecord.getProfession()).isEqualTo(expectedRecord.getProfession());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getCanFly()).isEqualTo(expectedRecord.getCanFly());
    }
}