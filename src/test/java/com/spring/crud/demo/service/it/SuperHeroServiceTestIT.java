package com.spring.crud.demo.service.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.ISuperHeroService;
import com.spring.crud.demo.utils.FileLoader;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SpringBootTest(value = "SuperHeroServiceTestIT")
class SuperHeroServiceTestIT {

    @Autowired
    private ISuperHeroService superHeroService;
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
    void init(){
        superHeroService.deleteAllSuperHero();
    }

    @Test
    void testGivenNon_WhenFindAllSuperHeros_ThenReturnAllRecord() {
        // Given
        superHeroes.forEach(superHero -> superHeroService.saveSuperHero(superHero));

        // When
        List<SuperHero> actualSuperHeros = superHeroService.findAllSuperHeros();

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
    void testGivenId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.saveSuperHero(superHero).orElseGet(SuperHero::new);

        // When
        SuperHero actualSuperHero = superHeroService.findSuperHeroById(expectedSuperHero.getId()).orElseGet(SuperHero::new);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenThrowError() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Assertions.assertThatThrownBy(() -> superHeroService.findSuperHeroById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + id);

        // Then
    }

    @Test
    void testGivenSuperHero_WhenFindSuperHerosByExample_ThenReturnRecords() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.saveSuperHero(superHero).orElseGet(SuperHero::new);

        // When
        List<SuperHero> actualSuperHeros = superHeroService.findSuperHerosByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isNotEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(1);
        assertSuperHero(actualSuperHeros.get(0), expectedSuperHero);
    }

    @Test
    void testGivenRandomSuperHero_WhenFindSuperHerosByExample_ThenReturnRecords() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);

        // When
        List<SuperHero> actualSuperHeros = superHeroService.findSuperHerosByExample(expectedSuperHero);

        // Then
        Assertions.assertThat(actualSuperHeros).isNotNull();
        Assertions.assertThat(actualSuperHeros).isEmpty();
        Assertions.assertThat(actualSuperHeros.size()).isEqualTo(0);
    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() {
        // Given
        SuperHero expectedSuperHero = new SuperHero("Bruce Wayne", "Batman", "Business man", 35, true);

        // When
        SuperHero actualSuperHero = superHeroService.saveSuperHero(expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero expectedSuperHero = superHeroService.saveSuperHero(superHero).orElseGet(SuperHero::new);

        // When
        Assertions.assertThatThrownBy(() -> superHeroService.saveSuperHero(expectedSuperHero))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + expectedSuperHero.getId());

        // Then
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroService.saveSuperHero(superHero).orElseGet(SuperHero::new);

        // When
        SuperHero expectedSuperHero = superHeroService.findSuperHeroById(savedSuperHero.getId()).orElseGet(SuperHero::new);
        expectedSuperHero.setAge(18);
        SuperHero actualSuperHero = superHeroService.updateSuperHero(savedSuperHero.getId(),expectedSuperHero).orElseGet(SuperHero::new);

        // Then
        assertSuperHero(expectedSuperHero, actualSuperHero);
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
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroService.saveSuperHero(superHero).orElseGet(SuperHero::new);

        // When & Then
        SuperHero expectedSuperHero = superHeroService.findSuperHeroById(savedSuperHero.getId()).orElseGet(SuperHero::new);
        expectedSuperHero.setAge(18);
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(id, expectedSuperHero))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + expectedSuperHero.getId());
    }

    @Test
    void testGivenSuperHeroAndId_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        SuperHero expectedSuperHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        expectedSuperHero.setId(25);

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(expectedSuperHero.getId(), expectedSuperHero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + expectedSuperHero.getId());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() {
        // Given
        SuperHero superHero = superHeroes.stream().filter(s -> s.getSuperName().equals("Spider Man")).findFirst().orElseGet(SuperHero::new);
        SuperHero savedSuperHero = superHeroService.saveSuperHero(superHero).orElseGet(SuperHero::new);

        // When
        Boolean flag = superHeroService.deleteSuperHero(savedSuperHero.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Boolean flag = superHeroService.deleteSuperHero(id);

        // Then
        Assertions.assertThat(flag).isFalse();
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