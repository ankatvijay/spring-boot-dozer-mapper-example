package com.spring.crud.demo.service.mock;

import com.spring.crud.demo.exception.InternalServerErrorException;
import com.spring.crud.demo.exception.NotFoundException;
import com.spring.crud.demo.exception.RecordFoundException;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.repository.SuperHeroRepository;
import com.spring.crud.demo.service.ISuperHeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service(value = "superHeroServiceImpl")
public class SuperHeroService implements ISuperHeroService {

    private final SuperHeroRepository superHeroRepository;

    @Override
    public List<SuperHero> findAllSuperHeros() {
        return superHeroRepository.findAll();
    }

    @Override
    public Optional<SuperHero> findSuperHeroById(int id) {
        Optional<SuperHero> optionalSuperHero = superHeroRepository.findById(id);
        if (optionalSuperHero.isEmpty()) {
            throw new NotFoundException("No record found with id " + id);
        }
        return optionalSuperHero;
    }

    @Override
    public boolean existsBySuperHeroId(int id) {
        return superHeroRepository.existsById(id);
    }

    @Override
    public List<SuperHero> findSuperHerosByExample(SuperHero superHero) {
        Example<SuperHero> superHeroExample = Example.of(superHero, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return superHeroRepository.findAll(superHeroExample);
    }

    @Override
    public Optional<SuperHero> saveSuperHero(SuperHero superHero) {
        if (Objects.nonNull(superHero) && Objects.nonNull(superHero.getId()) && superHeroRepository.existsById(superHero.getId())) {
            throw new RecordFoundException("Record already found with id " + superHero.getId());
        }
        return Optional.of(superHeroRepository.save(superHero));
    }

    @Override
    public Optional<SuperHero> updateSuperHero(int id, SuperHero superHero) {
        if (id > 0 && Objects.nonNull(superHero) && Objects.nonNull(superHero.getId())) {
            if (id == superHero.getId()) {
                if (superHeroRepository.existsById(id)) {
                    return Optional.of(superHeroRepository.save(superHero));
                }
                throw new NotFoundException("No record found with id " + id);
            } else {
                throw new InternalServerErrorException("Update Record id: " + id + " not equal to payload id: " + superHero.getId());
            }
        } else {
            throw new NullPointerException("Payload record id is null");
        }
    }

    @Override
    public boolean deleteSuperHero(int id) {
        if (existsBySuperHeroId(id)) {
            superHeroRepository.deleteById(id);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public void deleteAllSuperHero() {
        superHeroRepository.deleteAll();
    }
}
