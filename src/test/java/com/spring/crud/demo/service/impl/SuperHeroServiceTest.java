package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.repository.SuperHeroRepository;
import com.spring.crud.demo.utils.HelperUtil;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SuperHeroServiceTest {

    @Mock
    private SuperHeroRepository superHeroRepository;

    @InjectMocks
    private SuperHeroService superHeroService;

    private static Tuple[] expectedSuperHeros = null;

    @BeforeAll
    static void init() {
        expectedSuperHeros = HelperUtil.superHeroesSupplier.get().stream()
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
        Mockito.when(superHeroRepository.findAll()).thenReturn(HelperUtil.superHeroesSupplier.get());
        List<SuperHero> superHeros = superHeroService.findAllSuperHeros();

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
        Mockito.verify(superHeroRepository).findAll();
    }

    @Test
    void testGivenId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        int id = 12;
        Optional<SuperHero> optionalSpiderMan = HelperUtil.superHeroesSupplier.get().stream().filter(superHero -> superHero.getSuperName().equals("Spider Man")).findFirst();

        // When
        Mockito.when(superHeroRepository.findById(id)).thenReturn(optionalSpiderMan);
        Optional<SuperHero> actualSuperHero = superHeroService.findSuperHeroById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get()).isEqualTo(optionalSpiderMan.get());
        Mockito.verify(superHeroRepository).findById(id);
    }

    @Test
    void testGivenRandomId_WhenFindSuperHeroById_ThenReturnRecord() {
        // Given
        Integer id = RandomUtils.nextInt();
        Optional<SuperHero> optionalSpiderMan = Optional.empty();

        // When
        Mockito.when(superHeroRepository.findById(id)).thenReturn(optionalSpiderMan);
        Optional<SuperHero> actualSuperHero = superHeroService.findSuperHeroById(id);

        // Then
        Assertions.assertThat(actualSuperHero).isEmpty();
        Mockito.verify(superHeroRepository).findById(id);
    }

    @Test
    void testGivenExample_WhenFindSuperHerosByExample_ThenReturnRecords() {

    }

    @Test
    void testGivenSuperHero_WhenSaveSuperHero_ThenReturnNewSuperHero() {
        // Given
        SuperHero superHero = SuperHero.builder().name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build();

        // When
        Mockito.when(superHeroRepository.save(superHero)).thenReturn(superHero);
        Optional<SuperHero> actualSuperHero = superHeroService.saveSuperHero(superHero);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get().getName()).isEqualTo("Wade");
        Assertions.assertThat(actualSuperHero.get().getSuperName()).isEqualTo("Deadpool");
        Assertions.assertThat(actualSuperHero.get().getProfession()).isEqualTo("Street fighter");
        Assertions.assertThat(actualSuperHero.get().getAge()).isEqualTo(28);
        Assertions.assertThat(actualSuperHero.get().getCanFly()).isFalse();
        Mockito.verify(superHeroRepository).save(superHero);
    }

    @Test
    void testGivenExistingSuperHero_WhenSaveSuperHero_ThenThrowError() {
        // Given
        SuperHero superHero = SuperHero.builder().id(15).name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build();

        // When
        Mockito.when(superHeroRepository.existsById(superHero.getId())).thenReturn(true);
        Assertions.assertThatThrownBy(() -> superHeroService.saveSuperHero(superHero))
                .isInstanceOf(RecordFoundException.class)
                .hasMessage("Record already found with id " + superHero.getId());

        // Then
        Mockito.verify(superHeroRepository).existsById(superHero.getId());
    }

    @Test
    void testGivenExistingSuperHero_WhenUpdateSuperHero_ThenReturnUpdatedSuperHero() {
        // Given
        SuperHero superHero = SuperHero.builder().id(15).name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build();

        // When
        Mockito.when(superHeroRepository.existsById(superHero.getId())).thenReturn(true);
        Mockito.when(superHeroRepository.save(superHero)).thenReturn(superHero);
        Optional<SuperHero> actualSuperHero = superHeroService.updateSuperHero(superHero.getId(), superHero);

        // Then
        Assertions.assertThat(actualSuperHero).isNotNull();
        Assertions.assertThat(actualSuperHero).isNotEmpty();
        Assertions.assertThat(actualSuperHero.get().getId()).isEqualTo(15);
        Assertions.assertThat(actualSuperHero.get().getName()).isEqualTo("Wade");
        Assertions.assertThat(actualSuperHero.get().getSuperName()).isEqualTo("Deadpool");
        Assertions.assertThat(actualSuperHero.get().getProfession()).isEqualTo("Street fighter");
        Assertions.assertThat(actualSuperHero.get().getAge()).isEqualTo(28);
        Assertions.assertThat(actualSuperHero.get().getCanFly()).isFalse();
        Mockito.verify(superHeroRepository).existsById(superHero.getId());
        Mockito.verify(superHeroRepository).save(superHero);
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
        SuperHero superHero = SuperHero.builder().id(15).name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build();

        // When & Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(id, superHero))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessage("Update Record id: " + id + " not equal to payload id: " + superHero.getId());
    }

    @Test
    void testGivenSuperHeroAndId_WhenUpdateSuperHero_ThenThrowError() {
        // Given
        SuperHero superHero = SuperHero.builder().id(15).name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build();

        // When
        Mockito.when(superHeroRepository.existsById(superHero.getId())).thenReturn(false);

        // Then
        Assertions.assertThatThrownBy(() -> superHeroService.updateSuperHero(superHero.getId(), superHero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No record found with id " + superHero.getId());
        Mockito.verify(superHeroRepository).existsById(superHero.getId());
    }

    @Test
    void testGiveId_WhenDeleteSuperHero_ThenReturnTrue() {
        // Given
        SuperHero superHero = SuperHero.builder().id(15).name("Wade").superName("Deadpool").profession("Street fighter").age(28).canFly(false).build();

        // When
        Mockito.when(superHeroRepository.findById(superHero.getId())).thenReturn(Optional.of(superHero));
        Boolean flag = superHeroService.deleteSuperHero(superHero.getId());

        // Then
        Assertions.assertThat(flag).isTrue();
        Mockito.verify(superHeroRepository).findById(superHero.getId());
    }

    @Test
    void testGiveRandomId_WhenDeleteSuperHero_ThenReturnFalse() {
        // Given
        int id = RandomUtils.nextInt();

        // When
        Mockito.when(superHeroRepository.findById(id)).thenReturn(Optional.empty());
        Boolean flag = superHeroService.deleteSuperHero(id);

        // Then
        Assertions.assertThat(flag).isFalse();
        Mockito.verify(superHeroRepository).findById(id);
    }
}