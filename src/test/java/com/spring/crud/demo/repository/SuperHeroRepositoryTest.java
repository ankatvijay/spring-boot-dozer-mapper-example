package com.spring.crud.demo.repository;

import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.utils.HelperUtil;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SuperHeroRepositoryTest {

    @Autowired
    private SuperHeroRepository superHeroRepository;
    private static Tuple[] expectedSuperHeros = null;

    @BeforeAll
    static void init() {
        expectedSuperHeros = HelperUtil.superHeroesSupplier.get().stream()
                .map(superHero -> AssertionsForClassTypes.tuple(superHero.getName(),
                        superHero.getSuperName(),
                        superHero.getProfession(),
                        superHero.getAge(),
                        superHero.getCanFly()))
                .toArray(Tuple[]::new);
    }

    @Test
    void testGivenNon_WhenFindAll_ThenReturnAllRecord() {
        // Given

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
        Optional<SuperHero> optionalSpiderMan = superHeroRepository.findAll().stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst();
        SuperHero expectedSpiderMan = optionalSpiderMan.orElseGet(() -> SuperHero.builder().build());
        // When
        Optional<SuperHero> actualSuperHero = superHeroRepository.findById(expectedSpiderMan.getId());

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get()).isEqualTo(expectedSpiderMan);
    }

    @Test
    void testGivenId_WhenExistsById_ThenReturnRecord() {
        // Given
        Optional<SuperHero> optionalSpiderMan = superHeroRepository.findAll().stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst();
        SuperHero expectedSpiderMan = optionalSpiderMan.orElseGet(() -> SuperHero.builder().build());

        // When
        Boolean actualSuperHero = superHeroRepository.existsById(expectedSpiderMan.getId());

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
        Optional<SuperHero> optionalSpiderMan = superHeroRepository.findAll().stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst();
        SuperHero exampleSuperHero = optionalSpiderMan.orElseGet(() -> SuperHero.builder().build());

        // When
        Example<SuperHero> superHeroExample = Example.of(exampleSuperHero, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        List<SuperHero> actualSuperHeros = superHeroRepository.findAll(superHeroExample);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(1);
        Assertions.assertThat(actualSuperHeros.get(0)).isEqualTo(exampleSuperHero);
    }


    @ParameterizedTest
    @MethodSource(value = "generateExample")
    void testGivenExample_WhenFindByExample_ThenReturn2Record(Example<SuperHero> superHeroExample, int count) {
        // Given
        Tuple[] expectedTupleSuperHeros = HelperUtil.superHeroesSupplier.get().stream()
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
        SuperHero natasha = SuperHero.builder().name("Natasha").superName("Black Widow").profession("Agent").age(35).canFly(false).build();

        // When
        SuperHero superHero = superHeroRepository.save(natasha);

        // Then
        Assertions.assertThat(superHero).isNotNull();
        Assertions.assertThat(superHero.getName()).isEqualTo(natasha.getName());
        Assertions.assertThat(superHero.getSuperName()).isEqualTo(natasha.getSuperName());
        Assertions.assertThat(superHero.getProfession()).isEqualTo(natasha.getProfession());
        Assertions.assertThat(superHero.getAge()).isEqualTo(natasha.getAge());
        Assertions.assertThat(superHero.getCanFly()).isEqualTo(natasha.getCanFly());
    }

    @Test
    void testGivenId_WhenDeleteRecord_ThenReturnTrue() {
        // Given
        Optional<SuperHero> optionalSpiderMan = superHeroRepository.findAll().stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst();
        SuperHero expectedSpiderMan = optionalSpiderMan.orElseGet(() -> SuperHero.builder().build());

        // When
        superHeroRepository.deleteById(expectedSpiderMan.getId());
        Boolean deletedSuperHero = superHeroRepository.existsById(expectedSpiderMan.getId());

        // Then
        Assertions.assertThat(deletedSuperHero).isFalse();
    }

    @Test
    void testGivenId_WhenEditRecord_ThenReturnEditedRecord() {
        // Given
        Optional<SuperHero> optionalSpiderMan = superHeroRepository.findAll().stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst();
        SuperHero expectedSpiderMan = optionalSpiderMan.orElseGet(() -> SuperHero.builder().build());

        // When
        Optional<SuperHero> optionalSuperHero = superHeroRepository.findById(expectedSpiderMan.getId());
        SuperHero editSuperHero = optionalSuperHero.orElseGet(() -> SuperHero.builder().build());
        editSuperHero.setAge(18);
        SuperHero superHero = superHeroRepository.save(editSuperHero);

        // Then
        Assertions.assertThat(superHero).isNotNull();
        Assertions.assertThat(superHero.getId()).isEqualTo(editSuperHero.getId());
        Assertions.assertThat(superHero.getName()).isEqualTo(editSuperHero.getName());
        Assertions.assertThat(superHero.getSuperName()).isEqualTo(editSuperHero.getSuperName());
        Assertions.assertThat(superHero.getProfession()).isEqualTo(editSuperHero.getProfession());
        Assertions.assertThat(superHero.getAge()).isEqualTo(editSuperHero.getAge());
        Assertions.assertThat(superHero.getCanFly()).isEqualTo(editSuperHero.getCanFly());
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
        SuperHero canFlySuperHeros = SuperHero.builder().canFly(true).build();
        SuperHero cannotFlySuperHeros = SuperHero.builder().canFly(false).build();
        return Stream.of(
                Arguments.of(Example.of(canFlySuperHeros, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 2),
                Arguments.of(Example.of(cannotFlySuperHeros, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)), 3)
        );
    }
}