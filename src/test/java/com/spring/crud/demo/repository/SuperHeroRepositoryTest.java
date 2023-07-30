package com.spring.crud.demo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SuperHeroRepositoryTest implements BaseRepositoryTest<SuperHero> {

    @Autowired
    private SuperHeroRepository superHeroRepository;
    public static File file = FileLoader.getFileFromResource("superheroes.json");
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static TypeFactory typeFactory = objectMapper.getTypeFactory();

    @BeforeEach
    void init() {
        superHeroRepository.deleteAll();
    }

    @Override
    @Test
    public void testGivenNon_WhenGetAllRecords_ThenReturnListRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        superHeroRepository.saveAll(superHeroes);
        Tuple[] expectedSuperHeros = superHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        List<SuperHero> actualSuperHeros = superHeroRepository.findAll();

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

    @Override
    @Test
    public void testGivenId_WhenGetRecordsById_ThenReturnRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        SuperHero actualSuperHero = superHeroRepository.findById(expectedSuperHero.getId()).orElseGet(SuperHero::new);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
    }

    @Override
    @Test
    public void testGivenRandomId_WhenGetRecordsById_ThenReturnEmpty() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Optional<SuperHero> actualSuperHero = superHeroRepository.findById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isEmpty();
    }

    @Override
    @Test
    public void testGivenId_WhenExistRecordById_ThenReturnTrue() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        Boolean actualSuperHero = superHeroRepository.existsById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isTrue();
    }

    @Override
    @Test
    public void testGivenRandomId_WhenExistRecordById_ThenReturnFalse() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Boolean actualSuperHero = superHeroRepository.existsById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isFalse();
    }

    @Override
    @Test
    public void testGivenExample_WhenGetAllRecordsByExample_ThenReturnListRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        Example<SuperHero> superHeroExample = Example.of(expectedSuperHero, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<SuperHero> actualSuperHeros = superHeroRepository.findAll(superHeroExample);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isNotEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(1);
        assertRecord(expectedSuperHero, actualSuperHeros.get(0));
    }

    @Override
    @Test
    public void testGivenRandomRecord_WhenGetAllRecordsByExample_ThenReturnEmptyListRecords() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);

        // When
        Example<SuperHero> superHeroExample = Example.of(expectedSuperHero, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<SuperHero> actualSuperHeros = superHeroRepository.findAll(superHeroExample);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isEmpty();
    }

    @Override
    @ParameterizedTest
    @MethodSource(value = "generateExample")
    public void testGivenMultipleExample_WhenGetAllRecordsByExample_ThenReturnListRecord(Example<SuperHero> superHeroExample, int count) throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        superHeroRepository.saveAll(superHeroes);
        Tuple[] expectedTupleSuperHeros = superHeroes.stream()
                .filter(superHero -> superHero.getCanFly().equals(superHeroExample.getProbe().getCanFly()))
                .map(superHero -> AssertionsForClassTypes.tuple(superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        List<SuperHero> actualSuperHeros = superHeroRepository.findAll(superHeroExample);

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

    @Override
    @Test
    public void testGivenRecord_WhenInsertRecord_ThenReturnInsertRecord() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Natasha", "Black Widow", "Agent", 35, false);

        // When
        SuperHero actualSuperHero = superHeroRepository.save(expectedSuperHero);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
    }

    @Override
    @Test
    public void testGivenExistingRecordAndUpdate_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);
        expectedSuperHero.setAge(50);

        // When
        SuperHero actualSuperHero = superHeroRepository.save(expectedSuperHero);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
    }

    @Override
    @Test
    public void testGivenIdAndUpdatedRecord_WhenUpdateRecord_ThenReturnUpdateRecord() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroRepository.save(superHero);

        // When
        SuperHero expectedSuperHero = superHeroRepository.findById(savedSuperHero.getId()).orElseGet(SuperHero::new);
        expectedSuperHero.setAge(18);
        SuperHero actualSuperHero = superHeroRepository.save(expectedSuperHero);

        // Then
        assertRecord(expectedSuperHero, actualSuperHero);
    }

    @Override
    @Test
    public void testGivenId_WhenDeleteRecord_ThenReturnFalse() throws IOException {
        // Given
        List<SuperHero> superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        superHeroRepository.deleteById(expectedSuperHero.getId());
        Boolean deletedSuperHero = superHeroRepository.existsById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(deletedSuperHero).isFalse();
    }

    @Override
    @Test
    public void testGivenRandomId_WhenDeleteRecord_ThenThrowException() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroRepository.deleteById(id))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessage(String.format("No class com.spring.crud.demo.model.SuperHero entity with id %d exists!", id));
    }

    @Override
    @Test
    public void testGivenNon_WhenGetAllRecords_ThenReturnEmptyListRecord() {
        // Given
        superHeroRepository.deleteAll();

        // When
        List<SuperHero> superHeros = superHeroRepository.findAll();

        // Then
        Assertions.assertThat(superHeros).isNotNull();
        Assertions.assertThat(superHeros.size()).isEqualTo(0);
    }

    static Stream<Arguments> generateExample() {
        SuperHero canFlySuperHeros = new SuperHero();
        canFlySuperHeros.setCanFly(true);

        SuperHero cannotFlySuperHeros = new SuperHero();
        cannotFlySuperHeros.setCanFly(false);

        return Stream.of(
                Arguments.of(Example.of(canFlySuperHeros, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 2),
                Arguments.of(Example.of(cannotFlySuperHeros, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 3)
        );
    }

    @Override
    public void assertRecord(SuperHero expectedRecord, SuperHero actualRecord) {
        Assertions.assertThat(actualRecord).isNotNull();
        Assertions.assertThat(actualRecord.getName()).isEqualTo(expectedRecord.getName());
        Assertions.assertThat(actualRecord.getSuperName()).isEqualTo(expectedRecord.getSuperName());
        Assertions.assertThat(actualRecord.getProfession()).isEqualTo(expectedRecord.getProfession());
        Assertions.assertThat(actualRecord.getAge()).isEqualTo(expectedRecord.getAge());
        Assertions.assertThat(actualRecord.getCanFly()).isEqualTo(expectedRecord.getCanFly());
    }
}