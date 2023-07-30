package com.spring.crud.demo.service.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.repository.SuperHeroRepository;
import com.spring.crud.demo.service.BaseServiceTest;
import com.spring.crud.demo.service.SuperHeroService;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SuperHeroServiceMockTest implements BaseServiceTest<SuperHero> {

    @Mock
    private SuperHeroRepository superHeroRepository;
    @InjectMocks
    private SuperHeroService superHeroService;
    private static Tuple[] expectedSuperHeros = null;
    private static List<SuperHero> superHeroes;

    @BeforeAll
    static public void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        File file = FileLoader.getFileFromResource("superheroes.json");
        superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
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
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() {
        // Given

        // When
        Mockito.when(superHeroRepository.findAll()).thenReturn(superHeroes);
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecords();

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros.size()).isGreaterThan(0);
        Assertions.assertThat(actualSuperHeros)
                .extracting(SuperHero::getName,
                        SuperHero::getSuperName,
                        SuperHero::getProfession,
                        SuperHero::getAge,
                        SuperHero::getCanFly)
                .containsExactly(expectedSuperHeros);
        Mockito.verify(superHeroRepository).findAll();
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() {
        // Given
        int id = 12;
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroRepository.findById(id)).thenReturn(Optional.of(expectedSuperHero));
        SuperHero actualSuperHero = superHeroService.getRecordsById(id).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
        Mockito.verify(superHeroRepository).findById(id);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Mockito.when(superHeroRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> superHeroService.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);
        Mockito.verify(superHeroRepository).findById(id);
    }

    @Test
    @Override
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(25);

        // When
        Mockito.when(superHeroRepository.existsById(expectedSuperHero.getId())).thenReturn(true);
        Boolean actualSuperHero = superHeroRepository.existsById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroRepository.existsById(id)).thenReturn(false);
        Boolean actualSuperHero = superHeroRepository.existsById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isFalse();
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroRepository.findAll((Example) Mockito.any())).thenReturn(List.of(expectedSuperHero));
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecordsByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isNotEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(1);
        assertRecord(actualSuperHeros.get(0), expectedSuperHero);
        Mockito.verify(superHeroRepository).findAll((Example) Mockito.any());
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);
        List<SuperHero> superHeroes = new ArrayList<>();

        // When
        Mockito.when(superHeroRepository.findAll((Example) Mockito.any())).thenReturn(superHeroes);
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecordsByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(0);
        Mockito.verify(superHeroRepository).findAll((Example) Mockito.any());
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<SuperHero> example, int count) {
        // Given
        List<SuperHero> expectedSuperHeroes = superHeroes.stream().filter(superHero -> superHero.getCanFly().equals(example.getProbe().getCanFly())).toList();
        Tuple[] expectedTupleSuperHeros = expectedSuperHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        Mockito.when(superHeroRepository.findAll((Example) Mockito.any())).thenReturn(expectedSuperHeroes);
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecordsByExample(example.getProbe());

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(count);
        Assertions.assertThat(actualSuperHeros)
                .extracting(SuperHero::getName,
                        SuperHero::getSuperName,
                        SuperHero::getProfession,
                        SuperHero::getAge,
                        SuperHero::getCanFly)
                .containsExactly(expectedTupleSuperHeros);
        Mockito.verify(superHeroRepository).findAll((Example) Mockito.any());
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);

        // When
        Mockito.when(superHeroRepository.save(expectedSuperHero)).thenReturn(expectedSuperHero);
        SuperHero actualSuperHero = superHeroService.insertRecord(expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
        Mockito.verify(superHeroRepository).save(expectedSuperHero);
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroRepository.existsById(expectedSuperHero.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> superHeroService.insertRecord(expectedSuperHero))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedSuperHero.getId());

        // Then
        Mockito.verify(superHeroRepository).existsById(expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroRepository.existsById(expectedSuperHero.getId())).thenReturn(true);
        Mockito.when(superHeroRepository.save(expectedSuperHero)).thenReturn(expectedSuperHero);
        SuperHero actualSuperHero = superHeroService.updateRecord(expectedSuperHero.getId(), expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
        Mockito.verify(superHeroRepository).existsById(expectedSuperHero.getId());
        Mockito.verify(superHeroRepository).save(expectedSuperHero);
    }

    @Test
    @Override
    public void testGivenRandomIdAndNullRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateRecord(id, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payload record id is null");
    }

    @Test
    @Override
    public void testGivenExistingRecordAndRandomId_WhenUpdateRecord_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateRecord(id, expectedSuperHero))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroRepository.existsById(expectedSuperHero.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateRecord(expectedSuperHero.getId(), expectedSuperHero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedSuperHero.getId());
        Mockito.verify(superHeroRepository).existsById(expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(superHero -> superHero.getSuperName().equals("Deadpool")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(15);

        // When
        Mockito.when(superHeroRepository.existsById(expectedSuperHero.getId())).thenReturn(true);
        Boolean flag = superHeroService.deleteRecordById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(superHeroRepository).existsById(expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroRepository.existsById(id)).thenReturn(false);
        Boolean flag = superHeroService.deleteRecordById(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(superHeroRepository).existsById(id);
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given

        // When
        Mockito.doNothing().when(superHeroRepository).deleteAll();
        superHeroService.deleteAllRecords();

        // Then
        Mockito.verify(superHeroRepository).deleteAll();
    }

    private static Stream<Arguments> generateExample() {
        SuperHero canFlySuperHeros = new SuperHero();
        canFlySuperHeros.setCanFly(true);

        SuperHero cannotFlySuperHeros = new SuperHero();
        cannotFlySuperHeros.setCanFly(false);

        return Stream.of(
                Arguments.of(Example.of(canFlySuperHeros, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 2),
                Arguments.of(Example.of(cannotFlySuperHeros, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 3)
        );
    }

    public void assertRecord(SuperHero expectedRecord, SuperHero actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getName()).isEqualTo(expectedRecord.getName());
        Assertions.assertThat(actualRecord.getSuperName()).isEqualTo(expectedRecord.getSuperName());
        Assertions.assertThat(actualRecord.getProfession()).isEqualTo(expectedRecord.getProfession());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getCanFly()).isEqualTo(expectedRecord.getCanFly());
    }
}