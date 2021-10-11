package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.model.Student;
import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.model.emp.Employee;
import com.spring.crud.demo.repository.SuperHeroRepository;
import com.spring.crud.demo.service.ISuperHeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
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
        return superHeroRepository.findById(id);
    }

    @Override
    public List<SuperHero> findSuperHerosByExample(SuperHero superHero) {
        Example<SuperHero> superHeroExample = Example.of(superHero, ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return superHeroRepository.findAll(superHeroExample);
    }

    @Override
    public Optional<SuperHero> saveSuperHero(SuperHero superHero) {
        return Optional.of(superHeroRepository.save(superHero));
    }

    @Override
    public Optional<SuperHero> updateSuperHero(int id, SuperHero superHero) {
        return (superHeroRepository.existsById(id)) ? Optional.of(superHeroRepository.save(superHero)) : Optional.empty();

    }

    @Override
    public boolean deleteSuperHero(int id) {
        Optional<SuperHero> optionalSuperHero = findSuperHeroById(id);
        if (optionalSuperHero.isPresent()) {
            superHeroRepository.delete(optionalSuperHero.get());
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
