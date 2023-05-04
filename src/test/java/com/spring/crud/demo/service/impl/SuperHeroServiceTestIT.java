package com.spring.crud.demo.service.impl;

import com.spring.crud.demo.model.SuperHero;
import com.spring.crud.demo.service.ISuperHeroService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(value = "SuperHeroServiceTestIT")
class SuperHeroServiceTestIT {

    @Autowired
    private ISuperHeroService superHeroService;

    @Test
    void findAllSuperHeros() {
        List<SuperHero> superHeroes =  superHeroService.findAllSuperHeros();
        Assertions.assertThat(superHeroes).isNotNull();
        Assertions.assertThat(superHeroes).isNotEmpty();

    }
}