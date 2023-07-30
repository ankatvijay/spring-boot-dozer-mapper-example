package com.spring.crud.demo.service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.BaseServiceTest;
import com.spring.crud.demo.service.SuperHeroService;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest(value = "SuperHeroServiceITTest")
class SuperHeroServiceITTest implements BaseServiceTest<SuperHero> {

    @Autowired
    private SuperHeroService superHeroService;
    private static Tuple[] expectedSuperHeros = null;
    private static List<SuperHero> superHeroes;

    @BeforeAll
    static void initOnce() throws IOException {
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

    @BeforeEach
    void init() {
        superHeroService.deleteAllRecords();
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() {
        // Given
        superHeroes.forEach(superHero -> superHeroService.insertRecord(superHero));

        // When
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
    }

    @Test
    @Override
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When
        SuperHero actualSuperHero = superHeroService.getRecordsById(expectedSuperHero.getId()).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
    }

    @Test
    @Override
    public void testGivenRandomId_WhenGetRecordsById_ThenThrowException() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Assertions.assertThatThrownBy(() -> superHeroService.getRecordsById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
    }

    @Test
    @Override
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When
        Boolean actualSuperHero = superHeroService.existRecordById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean actualSuperHero = superHeroService.existRecordById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isFalse();
    }

    @Test
    @Override
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecordsByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isNotEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(1);
        assertRecord(actualSuperHeros.get(0), expectedSuperHero);
    }

    @Test
    @Override
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);

        // When
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecordsByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isEmpty();
    }

    @ParameterizedTest
    @MethodSource(value = "generateExample")
    @Override
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<SuperHero> example, int count) throws IOException {
        // Given
        superHeroService.insertBulkRecords(superHeroes);
        List<SuperHero> expectedSuperHeroes = superHeroes.stream().filter(superHero -> superHero.getCanFly().equals(example.getProbe().getCanFly())).toList();
        Tuple[] expectedTupleSuperHeros = expectedSuperHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
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
    }

    @Test
    @Override
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);

        // When
        SuperHero actualSuperHero = superHeroService.insertRecord(expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
    }

    @Test
    @Override
    public void testGivenExistingRecord_WhenInsertRecord_ThenThrowException() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When
        Assertions.assertThatThrownBy(() -> superHeroService.insertRecord(expectedSuperHero))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedSuperHero.getId());

        // Then
    }

    @Test
    @Override
    public void testGivenExistingRecordAndExistingRecordId_WhenUpdateRecord_ThenReturnUpdateRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When
        SuperHero expectedSuperHero = superHeroService.getRecordsById(savedSuperHero.getId()).orElseGet(SuperHero::new);
        expectedSuperHero.setAge(18);
        SuperHero actualSuperHero = superHeroService.updateRecord(savedSuperHero.getId(), expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
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
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When & Then
        SuperHero expectedSuperHero = superHeroService.getRecordsById(savedSuperHero.getId()).orElseGet(SuperHero::new);
        expectedSuperHero.setAge(18);
        Assertions.assertThatThrownBy(() -> superHeroService.updateRecord(id, expectedSuperHero))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenRecordIdAndRecord_WhenUpdateRecord_ThenThrowException() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateRecord(expectedSuperHero.getId(), expectedSuperHero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedSuperHero.getId());
    }

    @Test
    @Override
    public void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroService.insertRecord(superHero).orElseGet(SuperHero::new);

        // When
        Boolean flag = superHeroService.deleteRecordById(savedSuperHero.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
    }

    @Test
    @Override
    public void testGivenRandomId_WhenDeleteRecord_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean flag = superHeroService.deleteRecordById(id);

        // Then
        Assertions.assertThat(flag).isFalse();
    }

    @Test
    @Override
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given & When
        superHeroService.deleteAllRecords();
        List<SuperHero> actualSuperHeros = superHeroService.getAllRecords();

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isEmpty();
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