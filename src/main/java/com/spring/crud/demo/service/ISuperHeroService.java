package com.spring.crud.demo.service;

import com.spring.crud.demo.model.SuperHero;

import java.util.List;
import java.util.Optional;

public interface ISuperHeroService {

    List<SuperHero> findAllSuperHeros();

    Optional<SuperHero> findSuperHeroById(int id);

    List<SuperHero> findSuperHerosByExample(SuperHero superHero);

    Optional<SuperHero> saveSuperHero(SuperHero superHero);

    Optional<SuperHero> updateSuperHero(int id, SuperHero superHero);

    boolean deleteSuperHero(int id);

    void deleteAllSuperHero();
}
