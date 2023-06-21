package com.spring.crud.demo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.model.SuperHero;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SuperHeroRepositoryTest {

    @Autowired
    private SuperHeroRepository superHeroRepository;

    private static List<SuperHero> superHeroes;

    @BeforeAll
    static void initOnce() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = FileLoader.getFileFromResource("superheroes.json");
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        superHeroes = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, SuperHero.class));
    }

    @BeforeEach
    void init() {
        superHeroRepository.deleteAll();
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnAllRecord() {
        // Given
        superHeroRepository.saveAll(superHeroes);
        Tuple[] expectedSuperHeros = superHeroes.stream()
                .map(superHero -> AssertionsForClassTypes.tuple(superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);

        // When
        List<SuperHero> superHeros = superHeroRepository.findAll();

        // Then
        Assertions.assertThat(superHeros).isNotNull();
        Assertions.assertThat(superHeros.size()).isGreaterThan(0);
        Assertions.assertThat(superHeros)
                .extracting(SuperHero::getName,
                        SuperHero::getSuperName,
                        SuperHero::getProfession,
                        SuperHero::getAge,
                        SuperHero::getCanFly)
                .containsExactly(expectedSuperHeros);
    }

    @Test
    void testGivenId_WhenFindById_ThenReturnRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        SuperHero actualSuperHero = superHeroRepository.findById(expectedSuperHero.getId()).orElseGet(SuperHero::new);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
    }

    @Test
    void testGivenId_WhenExistsById_ThenReturnRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        Boolean actualSuperHero = superHeroRepository.existsById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isTrue();
    }

    @Test
    void testGivenRandomId_WhenExistsById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Boolean actualSuperHero = superHeroRepository.existsById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isFalse();
    }

    @Test
    void testGivenExample_WhenFindByExample_ThenReturn1Record() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        Example<SuperHero> superHeroExample = Example.of(expectedSuperHero, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<SuperHero> actualSuperHeros = superHeroRepository.findAll(superHeroExample);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isNotEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(1);
        assertSuperHero(expectedSuperHero, actualSuperHeros.get(0));
    }


    @ParameterizedTest
    @MethodSource(value = "generateExample")
    void testGivenExample_WhenFindByExample_ThenReturn2Record(Example<SuperHero> superHeroExample, int count) {
        // Given
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

    @Test
    void test_saveGivenSuperHero_WhenSave_ThenReturnSuperHero() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Natasha", "Black Widow", "Agent", 35, false);

        // When
        SuperHero actualSuperHero = superHeroRepository.save(expectedSuperHero);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
    }

    @Test
    void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroRepository.save(superHero);

        // When
        superHeroRepository.deleteById(expectedSuperHero.getId());
        Boolean deletedSuperHero = superHeroRepository.existsById(expectedSuperHero.getId());

        // Then
        Assertions.assertThat(deletedSuperHero).isFalse();
    }

    @Test
    void testGivenId_WhenEditRecord_ThenReturnEditedRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroRepository.save(superHero);

        // When
        SuperHero expectedSuperHero = superHeroRepository.findById(savedSuperHero.getId()).orElseGet(SuperHero::new);
        expectedSuperHero.setAge(18);
        SuperHero actualSuperHero = superHeroRepository.save(expectedSuperHero);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnEmptyRecord() {
        // Given
        superHeroRepository.deleteAll();

        // When
        List<SuperHero> superHeros = superHeroRepository.findAll();

        // Then
        Assertions.assertThat(superHeros).isNotNull();
        Assertions.assertThat(superHeros.size()).isEqualTo(0);
    }

    @Test
    void testGivenId_WhenDeleteId_ThenThrowException() {
        // Given
        Integer id = RandomUtils.nextInt();

        // When
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(EmptyResultDataAccessException.class, () -> superHeroRepository.deleteById(id));

        // Then
        Assertions.assertThat(exception).isInstanceOf(EmptyResultDataAccessException.class);
        Assertions.assertThat(exception.getMessage()).isEqualTo(String.format("No class com.spring.crud.demo.model.SuperHero entity with id %d exists!", id));
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

    private void assertSuperHero(SuperHero expectedSuperHero, SuperHero actualSuperHero) {
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero.getName()).isEqualTo(expectedSuperHero.getName());
        Assertions.assertThat(actualSuperHero.getSuperName()).isEqualTo(expectedSuperHero.getSuperName());
        Assertions.assertThat(actualSuperHero.getProfession()).isEqualTo(expectedSuperHero.getProfession());
        Assertions.assertThat(actualSuperHero.getAge()).isEqualTo(expectedSuperHero.getAge());
        Assertions.assertThat(actualSuperHero.getCanFly()).isEqualTo(expectedSuperHero.getCanFly());
    }
}